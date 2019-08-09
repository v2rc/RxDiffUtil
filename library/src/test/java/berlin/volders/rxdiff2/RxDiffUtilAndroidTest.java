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
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import berlin.volders.rxdiff2.test.AndroidTestAdapter;
import berlin.volders.rxdiff2.test.AndroidTestFunction;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

import static io.reactivex.Flowable.fromIterable;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(RobolectricTestRunner.class)
public class RxDiffUtilAndroidTest {

    static final List<List<String>> values = Arrays.asList(
            Collections.emptyList(),
            Collections.singletonList("single"),
            Arrays.asList("one", "two"),
            Collections.emptyList(),
            Arrays.asList("one", "two", "three")
    );

    AndroidTestAdapter<List<String>> adapter;
    Function<Flowable<List<String>>, Completable> rxDiff;

    @Before
    public void setup() {
        adapter = new AndroidTestAdapter<>(l -> l == null ? 0 : l.size(), values.get(0));
        rxDiff = new AndroidTestFunction<>(adapter, adapter);
    }

    @Test
    public void applyDiff_empty() {
        TestObserver observer = adapter.test();
        TestObserver subscriber = fromIterable(values)
                .take(1)
                .to(rxDiff)
                .test();
        subscriber.awaitTerminalEvent();

        subscriber.assertSubscribed()
                .assertNoErrors()
                .assertComplete()
                .assertNoValues();
        observer.assertValue(values.get(0));
    }

    @Test
    public void applyDiff_full() {
        TestObserver observer = adapter.test();
        TestObserver subscriber = fromIterable(values)
                .skip(1)
                .take(1)
                .to(rxDiff)
                .test();
        subscriber.awaitTerminalEvent();

        subscriber.assertSubscribed()
                .assertNoErrors()
                .assertComplete()
                .assertNoValues();
        observer.assertValue(values.get(1));
    }

    @Test
    public void applyDiff_full_empty() {
        TestObserver observer = adapter.test();
        TestObserver subscriber = fromIterable(values)
                .skip(2)
                .take(2)
                .to(rxDiff)
                .test();
        subscriber.awaitTerminalEvent();

        subscriber.assertSubscribed()
                .assertNoErrors()
                .assertComplete()
                .assertNoValues();
        observer.assertValues(values.get(2), values.get(3));
    }

    @Test
    public void applyDiff_full_full() {
        TestObserver observer = adapter.test();
        TestObserver subscriber = fromIterable(values)
                .skip(1)
                .take(2)
                .to(rxDiff)
                .test();
        subscriber.awaitTerminalEvent();

        subscriber.assertSubscribed()
                .assertNoErrors()
                .assertComplete()
                .assertNoValues();
        observer.assertValues(values.get(1), values.get(2));
    }

    @Test
    public void applyDiff_stream() {
        TestObserver observer = adapter.test();
        TestObserver subscriber = fromIterable(values)
                .to(rxDiff)
                .test();
        subscriber.awaitTerminalEvent();

        subscriber.assertSubscribed()
                .assertNoErrors()
                .assertComplete()
                .assertNoValues();
        observer.assertValues(values.toArray(new List[0]));
    }

    @Test
    public void applyDiff_concurrently() {
        rxDiff = new AndroidTestFunction<>(adapter, adapter.notifyOnGet());

        TestObserver observer = adapter.test();
        TestObserver subscriber = fromIterable(values)
                .skip(1)
                .to(rxDiff)
                .test();
        subscriber.awaitTerminalEvent();

        subscriber.assertSubscribed()
                .assertError(ConcurrentModificationException.class)
                .assertNotComplete()
                .assertNoValues();
        observer.assertNoValues();
    }
}
