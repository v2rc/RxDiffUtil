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

import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView.Adapter;

import java.lang.ref.WeakReference;

import berlin.volders.rxdiff.RxDiffUtil.Callback;
import rx.Observable;
import rx.Subscriber;

class OnCalculateDiff<A extends Adapter, T> implements Observable.Operator<OnCalculateDiffResult<A, T>, T> {

    @VisibleForTesting
    final WeakReference<A> adapter;
    @VisibleForTesting
    final Callback<A, T> cb;
    @VisibleForTesting
    final boolean dm;

    OnCalculateDiff(WeakReference<A> adapter, Callback<A, T> cb, boolean dm) {
        this.adapter = adapter;
        this.cb = cb;
        this.dm = dm;
    }

    @Override
    public OnCalculateDiffSubscriber<A, T> call(Subscriber<? super OnCalculateDiffResult<A, T>> subscriber) {
        return new OnCalculateDiffSubscriber<>(subscriber, adapter, cb, dm);
    }
}
