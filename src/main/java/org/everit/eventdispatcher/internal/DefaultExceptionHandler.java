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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.everit.eventdispatcher.ExceptionHandler;

/**
 * Default implementation of {@link ExceptionHandler} that writes the the stacktrace of all
 * exceptions to the system error stream.
 *
 * @param <LK>
 *          The type of the listener keys that identify the listener based on the
 *          {@link Object#hashCode()} and {@link Object#equals(Object)} functions.
 * @param <E>
 *          The type of the events.
 */
public class DefaultExceptionHandler<LK, E> implements ExceptionHandler<LK, E> {

  @Override
  public void handleException(final LK listenerKey, final E event, final Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    stringWriter.write(
        "Exception during calling listener: [listener key='" + listenerKey.toString() + "', event='"
            + event.toString() + "']\n");
    e.printStackTrace(printWriter);
    System.err.println(stringWriter.toString());
  }

}
