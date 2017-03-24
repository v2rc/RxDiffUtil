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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

public class RxDiffUtilAndroidTest {

    static final Func1<List<String>, Integer> listSize = new Func1<List<String>, Integer>() {
        @Override
        public Integer call(List<String> list) {
            return list == null ? 0 : list.size();
        }
    };

    static final List<List<String>> values = Arrays.asList(
            Collections.<String>emptyList(),
            Collections.singletonList("single"),
            Arrays.asList("one", "two"),
            Collections.<String>emptyList(),
            Arrays.asList("one", "two", "three")
    );

    AndroidTestAdapter<List<String>> adapter;
    Func1<Observable<List<String>>, Completable> rxDiff;

    TestSubscriber<List<String>> subscriber;

    @Before
    public void setup() {
        adapter = new AndroidTestAdapter<>(listSize);
        rxDiff = applyDiff(adapter, adapter);
        subscriber = TestSubscriber.create();
    }

    @Test
    public void applyDiff_empty() throws Exception {
        adapter.subscribe(subscriber);
        Observable.from(values)
                .first()
                .to(rxDiff)
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        subscriber.assertValue(values.get(0));
    }

    @Test
    public void applyDiff_full() throws Exception {
        adapter.subscribe(subscriber);
        Observable.from(values)
                .skip(1)
                .first()
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        subscriber.assertValue(values.get(1));
    }

    @Test
    public void applyDiff_full_full() throws Exception {
        adapter.subscribe(subscriber);
        Observable.from(values)
                .skip(1)
                .take(2)
                .to(rxDiff)
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        subscriber.assertValues(values.get(1), values.get(2));
    }

    @Test
    public void applyDiff_full_empty() throws Exception {
        adapter.subscribe(subscriber);
        Observable.from(values)
                .skip(2)
                .take(2)
                .to(rxDiff)
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        subscriber.assertValues(values.get(2), values.get(3));
    }

    @Test
    public void applyDiff_stream() throws Exception {
        adapter.subscribe(subscriber);
        Observable.from(values)
                .to(rxDiff)
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        subscriber.assertValues(values.toArray(new List[values.size()]));
    }

    @Test
    public void applyDiff_concurrently() throws Exception {
        rxDiff = applyDiff(adapter, adapter.notifyOnGet());

        adapter.subscribe(subscriber);
        Observable.from(values)
                .skip(1)
                .to(rxDiff)
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber.assertError(ConcurrentModificationException.class);
        subscriber.assertNotCompleted();
        subscriber.assertNoValues();
    }

    Func1<Observable<List<String>>, Completable>
    applyDiff(final AndroidTestAdapter<List<String>> adapter,
              final Func1<AndroidTestAdapter<List<String>>, List<String>> object) {
        return new AndroidTestFunction<>(adapter, object);
    }
}
