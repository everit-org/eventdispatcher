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

    private final class TestEventUtil implements EventUtil<Integer, Integer, Listener<Integer>> {
        @Override
        public Integer createReplayEvent(Integer originalEvent) {
            return originalEvent * (-1);
        }

        @Override
        public Integer getEventKey(Integer event) {
            if (event > 0) {
                return event;
            } else {
                return event * (-1);
            }
        }

        @Override
        public void callListener(Listener<Integer> listener, Integer event) {
            listener.receiveEvent(event);
        }
    }

    private final class TestListner implements Listener<Integer> {
        private final List<Integer> collectedEvents;

        private TestListner(List<Integer> collectedEvents) {
            this.collectedEvents = collectedEvents;
        }

        @Override
        public void receiveEvent(Integer event) {
            collectedEvents.add(event);
        }
    }

    /**
     * Testing one listener where normal events are positive numbers while the replay ones are their negative
     * representation.
     */
    @Test
    public void testOneListener() {
        EventUtil<Integer, Integer, Listener<Integer>> eventUtil =
                new TestEventUtil();

        EventDispatcher<Integer, Integer, Listener<Integer>, Listener<Integer>> eventDispatcher =
                new EventDispatcherImpl<Integer, Integer, Listener<Integer>, Listener<Integer>>(eventUtil);

        final List<Integer> collectedEvents = new ArrayList<Integer>();
        
        Listener<Integer> listener1 = new TestListner(collectedEvents);

        eventDispatcher.dispatchEvent(1);
        eventDispatcher.dispatchEvent(2);
        eventDispatcher.dispatchEvent(3);
        eventDispatcher.removeEvent(2);
        
        eventDispatcher.addListener(listener1, listener1);
        eventDispatcher.dispatchEvent(4);
        
        Assert.assertEquals(Integer.valueOf(-1), collectedEvents.get(0));
        Assert.assertEquals(Integer.valueOf(-3), collectedEvents.get(1));
        Assert.assertEquals(Integer.valueOf(4), collectedEvents.get(2));
        
        
    }
}
