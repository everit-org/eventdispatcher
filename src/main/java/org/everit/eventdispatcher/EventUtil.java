package org.everit.eventdispatcher;

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

/**
 * In case a listener service is registered after the event is dispatched it can still know about the event later with
 * the replay flag.
 * 
 * @param <E>
 *            The type of the event.
 */
public interface EventUtil<E, K, L> {

    /**
     * Creating a new replay event based on the original event. Please note that event implementations should always be
     * immutable classes. If the implementing technology does not support event replays the result will be the same
     * object as the incoming parameter. In case the function returns null an Exception will be thrown by the caller.
     * 
     * @param originalEvent
     *            The event that was originally dispatched.
     * @return The event that is in replay state.
     */
    E createReplayEvent(E originalEvent);

    /**
     * The function should return or generate a key object that identifies the event by it's {@link #hashCode()} and
     * {@link #equals(Object)} function. Based on that are replayed will override the previous event.
     * 
     * @param event The event that contains the key.
     * @return The key of the event.
     */
    K getEventKey(E event);

    /**
     * A minimalistic function that simply solves the calling of the listener with the event object. The implemented
     * function should do nothing else just call forwarding.
     * 
     * @param listener
     *            The listener that is waiting for events.
     * @param event
     *            The original or the replayed event object.
     */
    void callListener(L listener, E event);
}
