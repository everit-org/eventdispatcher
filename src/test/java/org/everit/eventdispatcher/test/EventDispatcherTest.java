package org.everit.eventdispatcher.test;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import org.everit.eventdispatcher.EventDispatcher;
import org.everit.eventdispatcher.EventDispatcherImpl;
import org.everit.eventdispatcher.EventUtil;
import org.everit.eventdispatcher.ListenerAlreadyRegisteredException;
import org.junit.Assert;
import org.junit.Test;

public class EventDispatcherTest {

    /**
     * Testing functionality on one thread.
     */
    @Test
    public void testOneThread() {
        EventUtil<Integer, Integer, Listener<Integer>> eventUtil =
                new TestEventUtil();

        EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> eventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil);

        Assert.assertEquals("Default listener call timeout should be set if no timeout is specified",
                EventDispatcher.DEFAULT_LISTENER_CALL_TIMEOUT, eventDispatcher.getListenerCallTimeout());

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

        // Testing the blacklisting functionality in case the listener throws an exception. The listener will get a
        // replay event so after the second event is dropped it should be blacklisted.
        Listener<Integer> thirdFailListener = new Listener<Integer>() {

            private int counter = 0;

            @Override
            public void receiveEvent(final Integer event) {
                counter++;
                if (counter == 3) {
                    throw new RuntimeException("Test exception");
                }
                collectedEvents.add(new ListenerWithEventEntry(this, event));
            }
        };

        eventDispatcher.addListener(thirdFailListener, thirdFailListener);
        Assert.assertEquals(-5, collectedEvents.get(0).getEvent().intValue());
        eventDispatcher.dispatchAndRemoveEvent(5);
        l1WithEvent = collectedEvents.get(1);
        l2WithEvent = collectedEvents.get(2);

        Assert.assertEquals(listener2, l1WithEvent.getListener());
        Assert.assertEquals(Integer.valueOf(5), l1WithEvent.getEvent());

        Assert.assertEquals(thirdFailListener, l2WithEvent.getListener());
        Assert.assertEquals(Integer.valueOf(5), l1WithEvent.getEvent());

        collectedEvents.clear();

        // Failing event on thirdFailListener so listener will be blacklisted.
        eventDispatcher.dispatchEvent(1);

        Assert.assertEquals(1, collectedEvents.size());
        eventDispatcher.dispatchEvent(2);
        Assert.assertEquals(2, collectedEvents.size());

        // Testing if adding an already registered listener really throws the expected exception.
        try {
            eventDispatcher.addListener(listener2, listener2);
            Assert.fail("Adding a listener twice should have thrown an exception");
        } catch (ListenerAlreadyRegisteredException e) {
            // Do nothing as this is the right behavior
        }

        // Removing a listener returns true
        Assert.assertTrue(eventDispatcher.removeListener(listener2));
        // Removing a listener when it is not registered returns false
        Assert.assertFalse(eventDispatcher.removeListener(listener2));

        eventDispatcher.close();
        try {
            eventDispatcher.dispatchEvent(5);
            Assert.fail("Closing a closed event dispatcher must throw an IllegalStateException");
        } catch (IllegalStateException e) {
            // Good behavior
        }
    }

    @Test
    public void testTimeout() {
        EventUtil<Integer, Integer, Listener<Integer>> eventUtil =
                new TestEventUtil();

        try {
            new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil, -1);
            Assert.fail("Only positive numbers are supported as timeout values");
        } catch (IllegalArgumentException e) {
            // Right behavior
        }

        EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> eventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil, 50);

        // Testing timeout with active event
        Listener<Integer> waitingListener = new Listener<Integer>() {

            @Override
            public void receiveEvent(final Integer event) {
                try {
                    Thread.sleep(100);
                    // Assert.fail("This listener should have been interrupted.");
                } catch (InterruptedException e) {
                    // Cool as this exception must have been thrown
                }
            }
        };
        eventDispatcher.addListener(waitingListener, waitingListener);
        eventDispatcher.dispatchEvent(1);

        // As the listener is blacklisted the isListenerBlacklisted should return true

        Assert.assertTrue("Listener should be blacklisted", eventDispatcher.isListenerBlacklisted(waitingListener));
        eventDispatcher.removeListener(waitingListener);

        // Now isListenerBlacklisted should return false as the listener is not maintained by the eventDispatcher
        Assert.assertFalse(eventDispatcher.isListenerBlacklisted(waitingListener));

        // Adding listener again but it should be blacklisted as soon as it is registered as the timeout happens on the
        // already existing replay event.
        eventDispatcher.addListener(waitingListener, waitingListener);
        Assert.assertTrue(eventDispatcher.isListenerBlacklisted(waitingListener));
        eventDispatcher.close();

        final EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> foreverWaitingEventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil, 0);

        Listener<Integer> forewerWaitingListener = new Listener<Integer>() {

            @Override
            public void receiveEvent(Integer event) {
                try {
                    Thread.sleep(500000);
                    Assert.fail("This thread should be interrupted.");
                } catch (InterruptedException e) {
                    // Good behavior
                }
            }
        };

        foreverWaitingEventDispatcher.addListener(forewerWaitingListener, forewerWaitingListener);

        Thread forewerWaitingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                foreverWaitingEventDispatcher.dispatchAndRemoveEvent(1);
            }
        });
        forewerWaitingThread.start();
        while (forewerWaitingThread.getState() != State.TIMED_WAITING
                && forewerWaitingThread.getState() != State.TERMINATED) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Assert.fail(e.getMessage());
            }
        }
        forewerWaitingThread.interrupt();
        foreverWaitingEventDispatcher.close();
    }
}
