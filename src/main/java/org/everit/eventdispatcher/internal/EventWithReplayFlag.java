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
package org.everit.eventdispatcher.internal;

/**
 * Internal class that holds an event object and the flag that shows if the event is already
 * converted to replay format.
 *
 * @param <E>
 *          The type of the event.
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
    return this.event;
  }

  public boolean isReplay() {
    return this.replay;
  }

  public void setEvent(final E event) {
    this.event = event;
  }

  public void setReplay(final boolean replay) {
    this.replay = replay;
  }

}
