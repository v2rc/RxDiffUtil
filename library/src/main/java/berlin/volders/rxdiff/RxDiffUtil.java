/*
 * Copyright (C) 2017 volders GmbH with <3 in Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package berlin.volders.rxdiff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.lang.ref.WeakReference;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * {@code RxDiffUtil} calculates and applies the diff between new and old data.
 * The {@code RxDiffUtil} instance should be applied to an observable with the
 * {@link Observable#to(Func1)} method. After chaining all actions, this also
 * transforms the {@link Observable} into a shared {@link Completable}.
 * <pre>
 * service.observeData()
 *        .compose(transformer)
 *        .onBackpressureLatest()
 *        .to(RxDiffUtil.with(adapter))
 *        .calculateDiff(callback)
 *        .applyDiff(adapter::setUnsafe)
 *        .subscribe();
 * </pre>
 *
 * @param <T> type of the data set
 * @param <A> type of the adapter
 */
@SuppressWarnings("WeakerAccess")
public class RxDiffUtil<A extends Adapter, T> {

    final WeakReference<A> adapter;
    private final Observable<T> o;

    RxDiffUtil(WeakReference<A> adapter, Observable<T> o) {
        this.adapter = adapter;
        this.o = o;
    }

    /**
     * @param cb callback to provide the {@link DiffUtil.Callback} to calculate the diff
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    public RxDiffResult<A, T> calculateDiff(@NonNull Callback<A, T> cb) {
        return calculateDiff(cb, true);
    }

    /**
     * @param cb callback to provide the {@link DiffUtil.Callback} to calculate the diff
     * @param dm should try to detect moved items
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    public RxDiffResult<A, T> calculateDiff(@NonNull Callback<A, T> cb, boolean dm) {
        return new RxDiffResult<>(o.lift(new OnCalculateDiff<>(adapter, cb, dm)));
    }

    /**
     * @param o  old data to use to calculate the diff
     * @param cb callback to provide the {@link DiffUtil.Callback} to calculate the diff
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    public RxDiffResult<A, T> calculateDiff(@NonNull Func1<? super A, ? extends T> o, @NonNull Callback2<T> cb) {
        return calculateDiff(o, cb, true);
    }

    /**
     * @param o  old data to use to calculate the diff
     * @param cb callback to provide the {@link DiffUtil.Callback} to calculate the diff
     * @param dm should try to detect moved items
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    public RxDiffResult<A, T> calculateDiff(@NonNull Func1<? super A, ? extends T> o, @NonNull Callback2<T> cb, boolean dm) {
        return calculateDiff(new RxDiffUtilCallback1<>(o, cb), dm);
    }

    /**
     * @param adapter the adapter to apply the diff to
     * @param <T>     type of the data set
     * @param <A>     type of the adapter
     * @return a transformer function to use with {@link Observable#to(Func1)}
     */
    public static <A extends Adapter, T> Func1<Observable<T>, RxDiffUtil<A, T>> with(final A adapter) {
        return new ToRxDiffUtil<>(adapter);
    }

    /**
     * A callback to create a {@link DiffUtil.Callback} from new data only to calculate the diff.
     * The callback has to keep track of the current data set of the adapter itself.
     *
     * @param <T> type of the data set
     */
    public interface Callback<A extends Adapter, T> {

        /**
         * @param newData data to update the adapter with
         * @return a {@link DiffUtil.Callback} for {@code newData}
         */
        @NonNull
        DiffUtil.Callback diffUtilCallback(@NonNull A adapter, @Nullable T newData);
    }

    /**
     * A callback to create a {@link DiffUtil.Callback} from current and new data
     * to calculate the diff.
     *
     * @param <T> type of the data set
     */
    public interface Callback2<T> {

        /**
         * @param oldData data currently displayed in the adapter
         * @param newData data to update the adapter with
         * @return a {@link DiffUtil.Callback} for {@code oldData} and {@code newData}
         */
        @NonNull
        DiffUtil.Callback diffUtilCallback(@Nullable T oldData, @Nullable T newData);
    }

    /**
     * Exception thrown if the {@link Observable} modified with {@code RxDiffUtil}
     * is still active after the adapter was cleared.
     */
    public static class SubscriptionLeak extends IllegalStateException {

        SubscriptionLeak() {
        }
    }
}
