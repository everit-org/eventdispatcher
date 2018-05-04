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
package org.everit.eventdispatcher.test;

public class ListenerWithEventEntry {

  private final Integer event;

  private final Listener<Integer> listener;

  public ListenerWithEventEntry(final Listener<Integer> listener, final Integer event) {
    this.listener = listener;
    this.event = event;
  }

  public Integer getEvent() {
    return this.event;
  }

  public Listener<Integer> getListener() {
    return this.listener;
  }

  @Override
  public String toString() {
    return "ListenerWithEventEntry [listener=" + this.listener + ", event=" + this.event + "]";
  }

}
