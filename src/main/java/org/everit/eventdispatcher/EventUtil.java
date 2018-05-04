/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.eventdispatcher;

/**
 * In case a listener service is registered after the event is dispatched it can still know about
 * the event later with the replay flag.
 *
 * @param <E>
 *          The type of the event.
 * @param <EK>
 *          The type of the event keys that identify the events based on the
 *          {@link Object#hashCode()} and {@link Object#equals(Object)} functions. Events are
 *          overridden by new events based on their key.
 * @param <L>
 *          The type of the listeners.
 */
public interface EventUtil<E, EK, L> {

  /**
   * A minimalist function that simply solves the calling of the listener with the event object. The
   * implemented function should do nothing else just call forwarding.
   *
   * @param listener
   *          The listener that is waiting for events.
   * @param event
   *          The original or the replayed event object.
   */
  void callListener(L listener, E event);

  /**
   * Creating a new replay event based on the original event. Please note that event implementations
   * should always be immutable classes. If the implementing technology does not make a difference
   * between replayed and currently happening events, the result should be the same object as the
   * incoming parameter. In case the function returns null an Exception will be thrown by the
   * caller.
   *
   * @param originalEvent
   *          The event that was originally dispatched.
   * @return The event that is in replay state.
   */
  E createReplayEvent(E originalEvent);

  /**
   * The function should return or generate a key object that identifies the event by it's
   * {@link #hashCode()} and {@link #equals(Object)} function. Based on that are replayed will
   * override the previous event.
   *
   * @param event
   *          The event that contains the key.
   * @return The key of the event.
   */
  EK getEventKey(E event);
}
