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
 * Thrown when there is a registration attempt of a listener to an {@link EventDispatcher} that is
 * already registered.
 */
public class ListenerAlreadyRegisteredException extends RuntimeException {

  private static final long serialVersionUID = 1762270486998573494L;

  /**
   * Constructor.
   *
   * @param message
   *          The message that contains the cause and the string representation of key of the
   *          listener.
   */
  ListenerAlreadyRegisteredException(final String message) {
    super(message);
  }
}
