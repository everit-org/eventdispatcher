package org.everit.eventdispatcher.internal;

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
 * Internal class that holds an event object and the flag that shows if the event is already converted to replay format.
 * 
 * @param <E>
 *            The type of the event.
 */
public class EventWithReplayFlag<E> {

    /**
     * A flag that shows if the event is converted to replay format.
     */
    private boolean replay = false;

    /**
     * The original event object or the replay format.
     */
    private E event;

    public EventWithReplayFlag(final E event) {
        this.event = event;
    }

    public E getEvent() {
        return event;
    }

    public boolean isReplay() {
        return replay;
    }

    public void setEvent(final E event) {
        this.event = event;
    }

    public void setReplay(final boolean replay) {
        this.replay = replay;
    }

}
