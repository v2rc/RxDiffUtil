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

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.ref.WeakReference;

import berlin.volders.rxdiff.RxDiffUtil.Callback;
import berlin.volders.rxdiff.RxDiffUtil.Callback2;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class RxDiffUtilTest {

    @Mock
    Adapter adapter;
    @Mock
    Callback callback;
    @Mock
    Callback2 callback2;
    @Mock
    Func1 function;
    @Mock
    DiffUtil.Callback cb;

    TestSubscriber subscriber;
    PublishSubject emitter;
    RxDiffUtil rxDiffUtil;

    @BeforeClass
    public static void init() {
        AndroidSchedulersTestHook.innit();
    }

    @Before
    public void setup() {
        doReturn(cb).when(callback).diffUtilCallback(any(Adapter.class), any());
        doReturn(cb).when(callback2).diffUtilCallback(any(), any());
        subscriber = TestSubscriber.create();
        emitter = PublishSubject.create();
        rxDiffUtil = new RxDiffUtil(new WeakReference(adapter), emitter);
    }

    @Test
    public void calculateDiff_callback() {
        Object o = 1;

        RxDiffResult rxDiffResult = rxDiffUtil.calculateDiff(callback);
        rxDiffResult.o.subscribe(subscriber);
        emitter.onNext(o);

        OnCalculateDiffResult result = (OnCalculateDiffResult) subscriber.getOnNextEvents().get(0);
        assertThat(result.adapter.get(), is((Object) adapter));
        assertThat(result.o, is(o));
        assertThat(result.o, is(o));
        verify(callback).diffUtilCallback(any(Adapter.class), any());
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
        subscriber.assertValueCount(1);
    }

    @Test
    public void calculateDiff_callback_detectMoves() {
        Object o = 1;

        RxDiffResult rxDiffResult = rxDiffUtil.calculateDiff(callback, false);
        rxDiffResult.o.subscribe(subscriber);
        emitter.onNext(o);

        OnCalculateDiffResult result = (OnCalculateDiffResult) subscriber.getOnNextEvents().get(0);
        assertThat(result.adapter.get(), is((Object) adapter));
        assertThat(result.o, is(o));
        assertThat(result.o, is(o));
        verify(callback).diffUtilCallback(any(Adapter.class), any());
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
        subscriber.assertValueCount(1);
    }

    @Test
    public void calculateDiff_callback2() {
        Object o = 1;

        RxDiffResult rxDiffResult = rxDiffUtil.calculateDiff(function, callback2);
        rxDiffResult.o.subscribe(subscriber);
        emitter.onNext(o);

        OnCalculateDiffResult result = (OnCalculateDiffResult) subscriber.getOnNextEvents().get(0);
        assertThat(result.adapter.get(), is((Object) adapter));
        assertThat(result.o, is(o));
        assertThat(result.o, is(o));
        verify(callback2).diffUtilCallback(any(), any());
        verify(function).call(any());
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
        subscriber.assertValueCount(1);
    }

    @Test
    public void calculateDiff_callback2_detectMoves() {
        Object o = 1;

        RxDiffResult rxDiffResult = rxDiffUtil.calculateDiff(function, callback2, false);
        rxDiffResult.o.subscribe(subscriber);
        emitter.onNext(o);

        OnCalculateDiffResult result = (OnCalculateDiffResult) subscriber.getOnNextEvents().get(0);
        assertThat(result.adapter.get(), is((Object) adapter));
        assertThat(result.o, is(o));
        assertThat(result.o, is(o));
        verify(callback2).diffUtilCallback(any(), any());
        verify(function).call(any());
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
        subscriber.assertValueCount(1);
    }

    @Test
    public void with() {
        assertThat(RxDiffUtil.with(adapter), instanceOf(ToRxDiffUtil.class));
    }
}
