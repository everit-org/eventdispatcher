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

import java.util.ArrayList;
import java.util.List;

import org.everit.eventdispatcher.EventDispatcher;
import org.everit.eventdispatcher.EventDispatcherImpl;
import org.everit.eventdispatcher.EventUtil;
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

        final List<ListenerWithEventEntry> collectedEvents = new ArrayList<ListenerWithEventEntry>();

        Listener<Integer> listener1 = new TestListener(collectedEvents);

        eventDispatcher.dispatchEvent(1);
        eventDispatcher.dispatchEvent(2);
        eventDispatcher.dispatchEvent(3);
        eventDispatcher.removeEvent(2);

        eventDispatcher.addListener(listener1, listener1);
        eventDispatcher.dispatchEvent(4);

        Assert.assertEquals(Integer.valueOf(-1), collectedEvents.get(0).getEvent());
        Assert.assertEquals(Integer.valueOf(-3), collectedEvents.get(1).getEvent());
        Assert.assertEquals(Integer.valueOf(4), collectedEvents.get(2).getEvent());

        eventDispatcher.dispatchAndRemoveEvent(1);
        Assert.assertEquals(Integer.valueOf(1), collectedEvents.get(3).getEvent());

        TestListener listener2 = new TestListener(collectedEvents);
        eventDispatcher.addListener(listener2, listener2);

        Assert.assertEquals(Integer.valueOf(-3), collectedEvents.get(4).getEvent());
        Assert.assertEquals(Integer.valueOf(-4), collectedEvents.get(5).getEvent());

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

        eventDispatcher.removeListener(listener1);

        collectedEvents.clear();

        eventDispatcher.dispatchEvent(8);
        Assert.assertEquals(1, collectedEvents.size());
        Assert.assertEquals(8, collectedEvents.get(0).getEvent().intValue());

        Assert.assertEquals(true, eventDispatcher.removeEvent(8));
        Assert.assertEquals(false, eventDispatcher.removeEvent(8));

        collectedEvents.clear();
        eventDispatcher.removeEvent(3);
        eventDispatcher.removeEvent(1);
        eventDispatcher.removeEvent(4);

        Listener<Integer> thirdFailListener = new Listener<Integer>() {

            private int counter = 0;

            @Override
            public void receiveEvent(Integer event) {
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
        
        eventDispatcher.dispatchEvent(1);
        
        Assert.assertEquals(1, collectedEvents.size());
        eventDispatcher.dispatchEvent(2);
        Assert.assertEquals(2, collectedEvents.size());
    }
}
