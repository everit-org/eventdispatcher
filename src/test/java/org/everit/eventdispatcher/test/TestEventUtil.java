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

import org.everit.eventdispatcher.EventUtil;

final class TestEventUtil implements EventUtil<Integer, Integer, Listener<Integer>> {
  @Override
  public void callListener(final Listener<Integer> listener, final Integer event) {
    listener.receiveEvent(event);
  }

  @Override
  public Integer createReplayEvent(final Integer originalEvent) {
    return originalEvent * -1;
  }

  @Override
  public Integer getEventKey(final Integer event) {
    if (event > 0) {
      return event;
    } else {
      return event * -1;
    }
  }
}
