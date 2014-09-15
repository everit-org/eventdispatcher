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
 * Exception handler that catches exceptions thrown by Listeners.
 *
 * @param <LK>
 *            Type of listener key.
 * @param <E>
 *            Type of the events.
 */
public interface ExceptionHandler<LK, E> {

    /**
     * Catches an exception that was thrown by a listener.
     *
     * @param listenerKey
     *            The key of the listener.
     * @param event
     *            The event instance.
     * @param e
     *            The throwable. Please note, that all throwable are passed to the exception handler and it is the
     *            decision of the developer of ExceptionHandler implementation to re-throw {@link Error}s or not. In
     *            case a {@link RuntimeException} is thrown from this function, it is logged to the standard error
     *            stream. If an {@link Error} is thrown, it is not caught by the implementation of
     *            {@link EventDispatcher}.
     */
    void handleException(LK listenerKey, E event, Throwable e);
}
