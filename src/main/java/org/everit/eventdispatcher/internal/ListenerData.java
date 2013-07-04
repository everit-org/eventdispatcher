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

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Each listener must have an own fair read-write locker assigned to be able to ensure event queuing and thread safety.
 * This is an internal helper class to be able to store the listeners with their lockers.
 * 
 * @param <L>
 */
public class ListenerData<L> {

    /**
     * The listener object.
     */
    private L listener;

    /**
     * The locker that belongs to the listener. A listener object can be registered with different key objects. When
     * this happens there are multiple locker instances as well.
     */
    private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock(true);

    /**
     * Constructor.
     * 
     * @param listener
     *            The listener object.
     */
    public ListenerData(final L listener) {
        this.listener = listener;
    }

    public L getListener() {
        return listener;
    }

    public ReentrantReadWriteLock getLocker() {
        return locker;
    }

}
