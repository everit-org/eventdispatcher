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
package org.everit.eventdispatcher.internal;

/**
 * Internal class that holds an event object and the flag that shows if the event is already converted to replay format.
 *
 * @param <E>
 *            The type of the event.
 */
public class EventWithReplayFlag<E> {

    /**
     * The original event object or the replay format.
     */
    private E event;

    /**
     * A flag that shows if the event is converted to replay format.
     */
    private boolean replay = false;

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
