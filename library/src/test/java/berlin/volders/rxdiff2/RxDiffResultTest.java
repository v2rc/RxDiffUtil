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

import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.lang.ref.WeakReference;
import java.util.ConcurrentModificationException;

import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.observers.TestObserver;
import io.reactivex.processors.PublishProcessor;

import static androidx.recyclerview.widget.DiffUtil.Callback;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(RobolectricTestRunner.class)
public class RxDiffResultTest {

    @Mock
    BiConsumer action;
    @Mock
    Adapter adapter;
    @Mock
    BiFunction callback;
    @Mock
    Action producer;
    @Mock
    Callback cb;

    RxDiffResult rxDiffResult;
    PublishProcessor emitter;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        doReturn(cb).when(callback).apply(any(Adapter.class), any());
        emitter = PublishProcessor.create();
        rxDiffResult = new RxDiffResult(emitter);
    }

    @Test
    public void applyDiff() throws Exception {
        TestObserver subscriber = rxDiffResult.applyDiff(action).test();

        emitResult(1);
        emitResult(2);

        verify(action).accept(adapter, 1);
        verify(action).accept(adapter, 2);
        subscriber.assertSubscribed()
                .assertNoErrors()
                .assertNotComplete()
                .assertNoValues();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void applyDiff_concurrently() throws Exception {
        rxDiffResult.applyDiff(action).test();
        TestObserver subscriber = rxDiffResult.applyDiff(action).test();

        emitResult(1);

        verify(action).accept(adapter, 1);
        subscriber.assertSubscribed()
                .assertError(ConcurrentModificationException.class)
                .assertNotComplete()
                .assertNoValues();
    }

    void emitResult(int i) throws Exception {
        emitter.onNext(new OnCalculateDiffResult(new WeakReference(adapter), i, callback, true, producer));
    }
}
