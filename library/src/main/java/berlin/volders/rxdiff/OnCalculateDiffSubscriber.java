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

import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.lang.ref.WeakReference;

import berlin.volders.rxdiff.RxDiffUtil.Callback;
import rx.Observer;
import rx.Producer;
import rx.Subscriber;

class OnCalculateDiffSubscriber<A extends Adapter, T> extends Subscriber<T> {

    private final Producer p = new Producer() {
        @Override
        public void request(long n) {
            OnCalculateDiffSubscriber.this.request(n);
        }
    };

    private final Observer<? super OnCalculateDiffResult<A, T>> observer;
    @VisibleForTesting
    final WeakReference<A> adapter;
    @VisibleForTesting
    final Callback<A, T> cb;
    @VisibleForTesting
    final boolean dm;

    OnCalculateDiffSubscriber(Subscriber<? super OnCalculateDiffResult<A, T>> subscriber,
                              WeakReference<A> adapter, Callback<A, T> cb, boolean dm) {
        super(subscriber);
        this.observer = subscriber;
        this.adapter = adapter;
        this.cb = cb;
        this.dm = dm;
    }

    @Override
    public void onStart() {
        request(1);
    }

    @Override
    public void onNext(T o) {
        if (!isUnsubscribed()) {
            try {
                OnCalculateDiffResult<A, T> result = new OnCalculateDiffResult<>(adapter, o, cb, dm, p);
                if (!isUnsubscribed()) {
                    observer.onNext(result);
                }
            } catch (Throwable throwable) {
                onError(throwable);
            }
        }
    }

    @Override
    public void onCompleted() {
        if (!isUnsubscribed()) {
            observer.onCompleted();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (!isUnsubscribed()) {
            observer.onError(e);
        }
    }
}
