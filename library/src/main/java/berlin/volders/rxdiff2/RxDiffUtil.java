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

package berlin.volders.rxdiff2;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.lang.ref.WeakReference;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static androidx.recyclerview.widget.DiffUtil.Callback;
import static androidx.recyclerview.widget.RecyclerView.Adapter;

/**
 * {@code RxDiffUtil} calculates and applies the diff between new and old data.
 * The {@code RxDiffUtil} instance should be applied to an flowable with the
 * {@link Flowable#to(Function)} method. After chaining all actions, this also
 * transforms the {@link Flowable} into a shared {@link Completable}.
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

    @VisibleForTesting
    final WeakReference<A> adapter;
    @VisibleForTesting
    final Flowable<T> o;

    RxDiffUtil(WeakReference<A> adapter, Flowable<T> o) {
        this.adapter = adapter;
        this.o = o;
    }

    /**
     * @param cb callback to provide the {@link Callback} to calculate the diff
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    @NonNull
    public RxDiffResult<A, T> calculateDiff(@NonNull BiFunction<A, T, Callback> cb) {
        return calculateDiff(cb, true);
    }

    /**
     * @param cb callback to provide the {@link Callback} to calculate the diff
     * @param dm should try to detect moved items
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    @NonNull
    public RxDiffResult<A, T> calculateDiff(@NonNull BiFunction<A, T, Callback> cb, boolean dm) {
        return new RxDiffResult<>(o.lift(s -> new OnCalculateDiffSubscriber<>(s, adapter, cb, dm)));
    }

    /**
     * @param o  old data to use to calculate the diff
     * @param cb callback to provide the {@link Callback} to calculate the diff
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    @NonNull
    public RxDiffResult<A, T> calculateDiff(@NonNull Function<? super A, ? extends T> o,
                                            @NonNull BiFunction<T, T, Callback> cb) {
        return calculateDiff(o, cb, true);
    }

    /**
     * @param o  old data to use to calculate the diff
     * @param cb callback to provide the {@link Callback} to calculate the diff
     * @param dm should try to detect moved items
     * @return an {@link RxDiffResult} to apply to the adapter
     */
    @NonNull
    public RxDiffResult<A, T> calculateDiff(@NonNull Function<? super A, ? extends T> o,
                                            @NonNull BiFunction<T, T, Callback> cb, boolean dm) {
        return calculateDiff((a, n) -> cb.apply(o.apply(a), n), dm);
    }

    /**
     * @param adapter the adapter to apply the diff to
     * @param <T>     type of the data set
     * @param <A>     type of the adapter
     * @return a transformer function to use with {@link Flowable#to(Function)}
     */
    @NonNull
    public static <A extends Adapter, T> Function<Flowable<T>, RxDiffUtil<A, T>>
    with(final A adapter) {
        WeakReference<A> adapterReference = new WeakReference<>(adapter);
        return o -> new RxDiffUtil<>(adapterReference, o);
    }

    /**
     * Exception thrown if the {@link Flowable} modified with {@code RxDiffUtil}
     * is still active after the adapter was cleared.
     */
    public static class SubscriptionLeak extends IllegalStateException {
        SubscriptionLeak() {
        }
    }
}
