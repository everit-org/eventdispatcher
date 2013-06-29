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

public class ListenerWithEventEntry {

    private Listener<Integer> listener;

    private Integer event;

    public ListenerWithEventEntry(final Listener<Integer> listener, final Integer event) {
        this.listener = listener;
        this.event = event;
    }

    public Listener<Integer> getListener() {
        return listener;
    }

    public Integer getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "ListenerWithEventEntry [listener=" + listener + ", event=" + event + "]";
    }

}