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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.lang.ref.WeakReference;

import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static androidx.recyclerview.widget.DiffUtil.Callback;
import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static io.reactivex.Flowable.empty;
import static io.reactivex.Flowable.never;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(RobolectricTestRunner.class)
public class RxDiffUtilTest {

    @Mock
    Adapter adapter;
    @Mock
    BiFunction callback;
    @Mock
    Function function;
    @Mock
    Callback cb;

    RxDiffUtil rxDiffUtil;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        doReturn(cb).when(callback).apply(any(), any());
        rxDiffUtil = new RxDiffUtil(new WeakReference(adapter), never().startWith(1));
    }

    @Test
    public void calculateDiff_callback() throws Exception {
        OnCalculateDiffResult result = (OnCalculateDiffResult) rxDiffUtil
                .calculateDiff(callback)
                .o.test()
                .assertSubscribed()
                .assertNoErrors()
                .assertNotComplete()
                .assertValueCount(1)
                .values().get(0);

        assertThat(result.adapter.get(), is(adapter));
        assertThat(result.o, is(1));
        verify(callback).apply(any(Adapter.class), any());
    }

    @Test
    public void calculateDiff_callback_detectMoves() throws Exception {
        OnCalculateDiffResult result = (OnCalculateDiffResult) rxDiffUtil
                .calculateDiff(callback, false)
                .o.test()
                .assertSubscribed()
                .assertNoErrors()
                .assertNotComplete()
                .assertValueCount(1)
                .values().get(0);

        assertThat(result.adapter.get(), is(adapter));
        assertThat(result.o, is(1));
        verify(callback).apply(any(Adapter.class), any());
    }

    @Test
    public void calculateDiff_callback2() throws Exception {
        OnCalculateDiffResult result = (OnCalculateDiffResult) rxDiffUtil
                .calculateDiff(function, callback)
                .o.test()
                .assertSubscribed()
                .assertNoErrors()
                .assertNotComplete()
                .assertValueCount(1)
                .values().get(0);

        assertThat(result.adapter.get(), is(adapter));
        assertThat(result.o, is(1));
        verify(callback).apply(any(), any());
        verify(function).apply(any());
    }

    @Test
    public void calculateDiff_callback2_detectMoves() throws Exception {
        OnCalculateDiffResult result = (OnCalculateDiffResult) rxDiffUtil
                .calculateDiff(function, callback, false)
                .o.test()
                .assertSubscribed()
                .assertNoErrors()
                .assertNotComplete()
                .assertValueCount(1)
                .values().get(0);

        assertThat(result.adapter.get(), is(adapter));
        assertThat(result.o, is(1));
        verify(callback).apply(any(), any());
        verify(function).apply(any());
    }

    @Test
    public void with() throws Exception {
        Function<Flowable<Object>, RxDiffUtil<Adapter, Object>> factory = RxDiffUtil.with(adapter);
        Flowable<Object> flowable = empty();
        rxDiffUtil = factory.apply(flowable);

        assertThat(rxDiffUtil.adapter.get(), is(adapter));
        assertThat(rxDiffUtil.o, is(flowable));
    }
}
