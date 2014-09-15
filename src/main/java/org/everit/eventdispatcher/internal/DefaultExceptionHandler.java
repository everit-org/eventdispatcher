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
package org.everit.eventdispatcher.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.everit.eventdispatcher.ExceptionHandler;

public class DefaultExceptionHandler<LK, E> implements ExceptionHandler<LK, E> {

    @Override
    public void handleException(LK listenerKey, E event, Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        stringWriter.write("Exception during calling listener: [listener key='" + listenerKey.toString() + "', event='"
                + event.toString() + "']\n");
        e.printStackTrace(printWriter);
        System.err.println(stringWriter.toString());
    }

}
