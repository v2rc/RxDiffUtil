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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.ref.WeakReference;

import io.reactivex.functions.BiFunction;

import static androidx.recyclerview.widget.DiffUtil.Callback;
import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class OnCalculateDiffSubscriberTest {

    @Captor
    ArgumentCaptor<OnCalculateDiffResult<Adapter, String>> result;

    @Mock
    Adapter adapter;
    @Mock
    BiFunction callback;
    @Mock
    Callback cb;
    @Mock
    Subscriber subscriber;
    @Mock
    Subscription subscription;

    OnCalculateDiffSubscriber diff;

    @Before
    public void setup() throws Exception {
        doReturn(cb).when(callback).apply(any(Adapter.class), any());
        diff = new OnCalculateDiffSubscriber(subscriber, new WeakReference(adapter), callback, false);
        diff.onSubscribe(subscription);
    }

    @Test
    public void onSubscribe() {
        verify(subscriber).onSubscribe(diff);
        verify(subscription).request(1);
    }

    @Test
    public void onNext() {
        String value = "foobar";

        diff.onNext(value);

        verify(subscriber, never()).onError(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onNext(result.capture());
        OnCalculateDiffResult<Adapter, String> actual = result.getValue();
        assertThat(actual.adapter.get(), is(diff.adapter.get()));
        assertThat(actual.o, is(value));
        assertThat(actual.diff, notNullValue());
    }

    @Test
    public void onNext_error() throws Exception {
        RuntimeException e = new RuntimeException();
        doThrow(e).when(callback).apply(any(), any());


        diff.onNext("");

        verify(subscriber).onError(e);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void onCompleted() {
        diff.onComplete();

        verify(subscriber, never()).onError(any());
        verify(subscriber).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void onError() {
        Throwable e = new Throwable();

        diff.onError(e);

        verify(subscriber).onError(e);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void cancel() {
        diff.cancel();

        verify(subscription).cancel();
    }

    @Test
    public void run() {
        diff.run();

        verify(subscription, times(2)).request(1);
    }
}
