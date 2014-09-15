/**
 * This file is part of Everit - Event dispatcher.
 *
 * Everit - Event dispatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Event dispatcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Event dispatcher.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.eventdispatcher.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.everit.eventdispatcher.EventDispatcher;
import org.everit.eventdispatcher.EventDispatcherImpl;
import org.everit.eventdispatcher.EventUtil;
import org.everit.eventdispatcher.ExceptionHandler;
import org.everit.eventdispatcher.ListenerAlreadyRegisteredException;
import org.junit.Assert;
import org.junit.Test;

public class EventDispatcherTest {

    @Test
    public void testExceptionHandler() {
        final AtomicReference<Throwable> caughedException = new AtomicReference<Throwable>();

        ExceptionHandler<Listener<Integer>, Integer> exceptionHandler =
                new ExceptionHandler<Listener<Integer>, Integer>() {

                    @Override
                    public void handleException(Listener<Integer> listenerKey, Integer event, Throwable e) {
                        caughedException.set(e);
                    }
                };

        EventUtil<Integer, Integer, Listener<Integer>> eventUtil =
                new TestEventUtil();

        EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> eventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil,
                        exceptionHandler);

        Listener<Integer> alwaysThrowsExceptionListener = new Listener<Integer>() {

            @Override
            public void receiveEvent(Integer event) {
                throw new RuntimeException("Dropped");
            }
        };

        eventDispatcher.addListener(alwaysThrowsExceptionListener, alwaysThrowsExceptionListener);

        eventDispatcher.dispatchEvent(0);

        Assert.assertNotNull(caughedException.get());
        Assert.assertEquals("Dropped", caughedException.get().getMessage());

    }

    @Test
    public void testExceptionHandlerThrowsException() {
        ExceptionHandler<Listener<Integer>, Integer> exceptionHandler =
                new ExceptionHandler<Listener<Integer>, Integer>() {

                    @Override
                    public void handleException(Listener<Integer> listenerKey, Integer event, Throwable e) {
                        throw new RuntimeException("Dropped from exception handler");
                    }
                };

        EventUtil<Integer, Integer, Listener<Integer>> eventUtil =
                new TestEventUtil();

        EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> eventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil,
                        exceptionHandler);

        Listener<Integer> alwaysThrowsExceptionListener = new Listener<Integer>() {

            @Override
            public void receiveEvent(Integer event) {
                throw new RuntimeException("Dropped from listener");
            }
        };

        eventDispatcher.addListener(alwaysThrowsExceptionListener, alwaysThrowsExceptionListener);

        eventDispatcher.dispatchEvent(0);
    }

    @Test
    public void testListenerAddedTwice() {
        EventUtil<Integer, Integer, Listener<Integer>> eventUtil =
                new TestEventUtil();

        EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> eventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil);

        Listener<Integer> listener = new TestListener(new ArrayList<ListenerWithEventEntry>());
        eventDispatcher.addListener(listener, listener);

        try {
            eventDispatcher.addListener(listener, listener);
            Assert.fail();
        } catch (ListenerAlreadyRegisteredException e) {

        }
    }

    @Test
    public void testOneThread() {
        EventUtil<Integer, Integer, Listener<Integer>> eventUtil =
                new TestEventUtil();

        EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> eventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil);

        final List<ListenerWithEventEntry> collectedEvents = new ArrayList<ListenerWithEventEntry>();

        Listener<Integer> listener1 = new TestListener(collectedEvents);

        eventDispatcher.dispatchEvent(1);
        eventDispatcher.dispatchEvent(2);
        eventDispatcher.dispatchEvent(3);
        eventDispatcher.removeEvent(2);

        eventDispatcher.addListener(listener1, listener1);
        eventDispatcher.dispatchEvent(4);

        // Checking two replay events and one event that is passed after the listener is registered.
        Assert.assertEquals(Integer.valueOf(-1), collectedEvents.get(0).getEvent());
        Assert.assertEquals(Integer.valueOf(-3), collectedEvents.get(1).getEvent());
        Assert.assertEquals(Integer.valueOf(4), collectedEvents.get(2).getEvent());

        // Testing the dispatchAndRemove functionality in two steps. First an event is dispatched than a new listener is
        // registered. The new listener should not catch the removed event.
        eventDispatcher.dispatchAndRemoveEvent(1);
        Assert.assertEquals(Integer.valueOf(1), collectedEvents.get(3).getEvent());

        // Second step of checking dispatchAndRemove functionality.
        TestListener listener2 = new TestListener(collectedEvents);
        eventDispatcher.addListener(listener2, listener2);

        Assert.assertEquals(Integer.valueOf(-3), collectedEvents.get(4).getEvent());
        Assert.assertEquals(Integer.valueOf(-4), collectedEvents.get(5).getEvent());

        // Testing if the event dispatcher works with two active listeners.
        eventDispatcher.dispatchEvent(5);

        ListenerWithEventEntry l1WithEvent = collectedEvents.get(6);
        ListenerWithEventEntry l2WithEvent = collectedEvents.get(7);

        Assert.assertEquals(listener1, l1WithEvent.getListener());
        Assert.assertEquals(Integer.valueOf(5), l1WithEvent.getEvent());

        Assert.assertEquals(listener2, l2WithEvent.getListener());
        Assert.assertEquals(Integer.valueOf(5), l2WithEvent.getEvent());

        collectedEvents.clear();

        eventDispatcher.dispatchEvent(1);

        l1WithEvent = collectedEvents.get(0);
        l2WithEvent = collectedEvents.get(1);

        Assert.assertEquals(listener1, l1WithEvent.getListener());
        Assert.assertEquals(Integer.valueOf(1), l1WithEvent.getEvent());

        Assert.assertEquals(listener2, l2WithEvent.getListener());
        Assert.assertEquals(Integer.valueOf(1), l2WithEvent.getEvent());

        // Testing the removal of a listener. It should not fill collectedEvents anymore.
        collectedEvents.clear();
        eventDispatcher.removeListener(listener1);

        eventDispatcher.dispatchEvent(8);
        Assert.assertEquals(1, collectedEvents.size());
        Assert.assertEquals(8, collectedEvents.get(0).getEvent().intValue());

        // Testing the removeEvent functionality. First it should return true as the event is removed second call must
        // be false as the event is not even in the queue.
        Assert.assertEquals(true, eventDispatcher.removeEvent(8));
        Assert.assertEquals(false, eventDispatcher.removeEvent(8));

        // Cleaning a bit to be able to test.
        collectedEvents.clear();
        eventDispatcher.removeEvent(3);
        eventDispatcher.removeEvent(1);
        eventDispatcher.removeEvent(4);

        // Removing a listener returns true
        Assert.assertTrue(eventDispatcher.removeListener(listener2));
        // Removing a listener when it is not registered returns false
        Assert.assertFalse(eventDispatcher.removeListener(listener2));
    }
}
