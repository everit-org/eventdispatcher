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
package org.everit.eventdispatcher.test;

import java.util.List;

final class TestListener implements Listener<Integer> {

    private final List<ListenerWithEventEntry> collectedEvents;

    TestListener(final List<ListenerWithEventEntry> collectedEvents) {
        this.collectedEvents = collectedEvents;
    }

    @Override
    public void receiveEvent(final Integer event) {
        collectedEvents.add(new ListenerWithEventEntry(this, event));
    }
}
