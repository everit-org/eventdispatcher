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
package org.everit.eventdispatcher;


/**
 *
 * @param <E>
 *            The type of the events.
 * @param <EK>
 *            The type of the event keys that identify the events based on the {@link Object#hashCode()} and
 *            {@link Object#equals(Object)} functions. Events are overridden by new events based on their key.
 * @param <L>
 *            The type of the listeners.
 * @param <LK>
 *            The type of the listener keys that identify the listener based on the {@link Object#hashCode()} and
 *            {@link Object#equals(Object)} functions.
 */
public interface EventDispatcher<E, EK, L, LK> {

    /**
     * Adding a new listener. The new listener will first receive all the replay events in the same order as they
     * arrived synchronously. After the replay events were received the listener can get the new events. If a new event
     * arrives while the listener processes the replay events, the event will wait to arrive (without the replay flag).
     *
     * @param listenerKey
     *            The key of the listener that identifies the listener object based on it's
     *            {@link Object#equals(Object)} and {@link Object#hashCode()} functions.
     * @param listener
     *            The listener object.
     * @throws ListenerAlreadyRegisteredException
     *             if the listener with the key was already registered.
     */
    void addListener(LK listenerKey, L listener) throws ListenerAlreadyRegisteredException;

    /**
     * Dispatching an event in the way that the passed event or the one that already exists with the same key will not
     * be replayed to any new listeners.
     *
     * @param event
     *            The event object.
     * @throws IllegalStateException
     *             if the dipatcher is already closed.
     */
    void dispatchAndRemoveEvent(E event) throws IllegalStateException;

    /**
     * Dispatching a new event to the listeners. The event is dispatched to the listeners that are already registered
     * synchronously. Based on the key of the event any prevoius event will be removed from the replay queue. New
     * listeners will get the replay type of this event later.
     *
     * @param event
     *            The event object.
     * @throws IllegalStateException
     *             if the dipatcher is already closed.
     */
    void dispatchEvent(E event) throws IllegalStateException;

    /**
     * Removing an event based on it's key from the replay queue so it will not be passed to newly registered listeners.
     *
     * @param eventKey
     *            The key of the event.
     * @return True if the event was removed based on the key, false if the event was not even in the queue.
     */
    boolean removeEvent(EK eventKey);

    /**
     * Removing a listener based on it's key from the registration.
     *
     * @param listenerKey
     *            The key of the listener that identifies the listener with it's {@link Object#equals(Object)} and
     *            {@link Object#hashCode()} functions.
     * @return True if the removal was done false if the listener was not even in the queue.
     */
    boolean removeListener(LK listenerKey);
}
