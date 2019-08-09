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

package berlin.volders.rxdiff2.test;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.ReplaySubject;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static io.reactivex.subjects.ReplaySubject.create;

public class AndroidTestAdapter<T> extends Adapter<ViewHolder> implements
        Function<AndroidTestAdapter<T>, T>, BiConsumer<AndroidTestAdapter<T>, T>,
        BiFunction<T, T, DiffUtil.Callback> {

    private final ReplaySubject<T> ts = create();
    private final Function<T, Integer> sizeOf;

    public AndroidTestAdapter(Function<T, Integer> sizeOf, T emptyState) {
        this.ts.onNext(emptyState);
        this.sizeOf = sizeOf;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new View(getApplicationContext())) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        try {
            return sizeOf.apply(apply(this));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void accept(AndroidTestAdapter<T> adapter, T t) {
        adapter.ts.onNext(t);
    }

    @Override
    public T apply(AndroidTestAdapter<T> adapter) {
        int last = adapter.ts.getValues().length - 1;
        return adapter.ts.skip(last).blockingFirst();
    }

    @NonNull
    @Override
    public DiffUtil.Callback apply(@Nullable T oldData, @Nullable T newData) {
        return new AndroidTestDiffUtilCallback<>(oldData, newData, sizeOf);
    }

    public TestObserver<T> test() {
        return ts.skip(1).test();
    }

    public Function<AndroidTestAdapter<T>, T> notifyOnGet() {
        return adapter -> {
            adapter.notifyDataSetChanged();
            return adapter.apply(adapter);
        };
    }
}
