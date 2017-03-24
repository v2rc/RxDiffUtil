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
import android.support.v7.widget.RecyclerView;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.functions.Func1;

class ToRxDiffUtil<A extends RecyclerView.Adapter, T> implements Func1<Observable<T>, RxDiffUtil<A, T>> {

    @VisibleForTesting
    final WeakReference<A> adapter;

    ToRxDiffUtil(A adapter) {
        this.adapter = new WeakReference<>(adapter);
    }

    @Override
    public RxDiffUtil<A, T> call(Observable<T> o) {
        return new RxDiffUtil<>(adapter, o);
    }
}
