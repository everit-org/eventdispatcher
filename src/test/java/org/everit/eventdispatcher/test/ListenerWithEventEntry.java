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

public class ListenerWithEventEntry {

    private final Integer event;

    private final Listener<Integer> listener;

    public ListenerWithEventEntry(final Listener<Integer> listener, final Integer event) {
        this.listener = listener;
        this.event = event;
    }

    public Integer getEvent() {
        return event;
    }

    public Listener<Integer> getListener() {
        return listener;
    }

    @Override
    public String toString() {
        return "ListenerWithEventEntry [listener=" + listener + ", event=" + event + "]";
    }

}
