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

package berlin.volders.rxdiff.test;

import androidx.recyclerview.widget.DiffUtil;

import rx.functions.Func1;

class AndroidTestDiffUtilCallback<T> extends DiffUtil.Callback {

    private final T oldData;
    private final T newData;
    private final Func1<T, Integer> sizeOf;

    AndroidTestDiffUtilCallback(T oldData, T newData, Func1<T, Integer> sizeOf) {
        this.oldData = oldData;
        this.newData = newData;
        this.sizeOf = sizeOf;
    }

    @Override
    public int getOldListSize() {
        return sizeOf.call(oldData);
    }

    @Override
    public int getNewListSize() {
        return sizeOf.call(newData);
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return true;
    }
}
