/*
 * Copyright (C) 2018 Christian Schmitz
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

import androidx.annotation.VisibleForTesting;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.ref.WeakReference;

import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;

import static androidx.recyclerview.widget.DiffUtil.Callback;
import static androidx.recyclerview.widget.RecyclerView.Adapter;

class OnCalculateDiffSubscriber<A extends Adapter, T>
        implements Action, Subscriber<T>, Subscription {

    private final Subscriber<? super OnCalculateDiffResult<A, T>> observer;
    @VisibleForTesting
    final WeakReference<A> adapter;
    private final BiFunction<A, T, Callback> cb;
    private final boolean dm;

    private Subscription s;

    OnCalculateDiffSubscriber(Subscriber<? super OnCalculateDiffResult<A, T>> subscriber,
                              WeakReference<A> adapter, BiFunction<A, T, Callback> cb, boolean dm) {
        super();
        this.observer = subscriber;
        this.adapter = adapter;
        this.cb = cb;
        this.dm = dm;
    }

    @Override
    public final void onSubscribe(Subscription s) {
        this.s = s;
        observer.onSubscribe(this);
        s.request(1);
    }

    @Override
    public void onNext(T o) {
        try {
            observer.onNext(new OnCalculateDiffResult<>(adapter, o, cb, dm, this));
        } catch (Exception error) {
            onError(error);
        }
    }

    @Override
    public void onComplete() {
        observer.onComplete();
    }

    @Override
    public void onError(Throwable e) {
        observer.onError(e);
    }

    @Override
    public void cancel() {
        s.cancel();
    }

    @Override
    public void request(long n) {
    }

    @Override
    public void run() {
        s.request(1);
    }
}
