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

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Each listener must have an own fair read-write locker assigned to be able to ensure event queuing
 * and thread safety. This is an internal helper class to be able to store the listeners with their
 * lockers.
 *
 * @param <L>
 *          The type of the listeners.
 */
public class ListenerData<L> {

  /**
   * The listener object.
   */
  private final L listener;

  /**
   * The locker that belongs to the listener. A listener object can be registered with different key
   * objects. When this happens there are multiple locker instances as well.
   */
  private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock(true);

  /**
   * Constructor.
   *
   * @param listener
   *          The listener object.
   */
  public ListenerData(final L listener) {
    this.listener = listener;
  }

  public L getListener() {
    return this.listener;
  }

  public ReentrantReadWriteLock getLocker() {
    return this.locker;
  }

}
