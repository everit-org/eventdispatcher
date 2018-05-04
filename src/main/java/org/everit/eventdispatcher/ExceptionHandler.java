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
 * Exception handler that catches exceptions thrown by Listeners.
 *
 * @param <LK>
 *          Type of listener key.
 * @param <E>
 *          Type of the events.
 */
public interface ExceptionHandler<LK, E> {

  /**
   * Catches an exception that was thrown by a listener.
   *
   * @param listenerKey
   *          The key of the listener.
   * @param event
   *          The event instance.
   * @param e
   *          The throwable. Please note, that all throwable are passed to the exception handler and
   *          it is the decision of the developer of ExceptionHandler implementation to re-throw
   *          {@link Error}s or not. In case a {@link RuntimeException} is thrown from this
   *          function, it is logged to the standard error stream. If an {@link Error} is thrown, it
   *          is not caught by the implementation of {@link EventDispatcher}.
   */
  void handleException(LK listenerKey, E event, Throwable e);
}
