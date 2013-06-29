package org.everit.eventdispatcher;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.everit.eventdispatcher.internal.EventWithReplayFlag;

// addListener                l(w)+
//   listeners.add             ls(w) etr(r)
//     eventsToReplay.clone
//   eventsToReplayClone.iter  ---- below this l(w)+ is extended
//     listener.call          
// dispatchEvent             
//   eventsToReplay.modify    etr(w)
//   listeners.iterate        ls(r)
//     listener.call           l(r)+

// removeListener
//   listeners.remove         ls(w)

/**
 * Helper class to dispatch events to listeners. The dispatcher calls every listener that are already registered. </ul>
 * 
 * @param <E>
 *            The type of the events.
 * @param <EK>
 *            The type of the event keys.
 * @param <L>
 *            The type of the listeners.
 */
public class EventDispatcherImpl<E, EK, L, LK> implements EventDispatcher<E, EK, L, LK> {

    private LinkedHashMap<EK, EventWithReplayFlag<E>> eventsToReplay = new LinkedHashMap<EK, EventWithReplayFlag<E>>();

    private ReentrantReadWriteLock eventsToReplayLocker = new ReentrantReadWriteLock(false);

    private final EventUtil<E, EK, L> eventUtil;

    private Map<LK, L> listeners =
            new LinkedHashMap<LK, L>();

    public EventDispatcherImpl(EventUtil<E, EK, L> eventUtil) {
        this.eventUtil = eventUtil;
    }

    /* (non-Javadoc)
     * @see org.everit.osgi.eventdispatcher.core.ManagableEventDispatcher#addListener(LK, L)
     */
    @Override
    public void addListener(LK listenerKey, L listener) {
        doReplaysForListener(listenerKey, listener);
        listeners.put(listenerKey, listener);
    }

    /**
     * Calling a listener with an event. In case there is any exception or a timeout the listener will be removed from
     * the listeners collection and no more events will be passed.
     * 
     * @param reference
     *            The reference of the listener OSGi service.
     * @param listener
     *            The listener object.
     * @param event
     *            The event.
     * @return true if the call was successful, false if the listener was blacklisted due to an exception or timeout.
     */
    private boolean callListener(LK reference, L listener, E event) {
        try {
            eventUtil.callListener(listener, event);
            return true;
        } catch (RuntimeException e) {
            listeners.remove(reference);
            // TODO log that listener is blacklisted

            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.everit.osgi.eventdispatcher.core.ManagableEventDispatcher#dispatchAndRemoveEvent(E)
     */
    @Override
    public void dispatchAndRemoveEvent(E event) {
        dispatchEventInternal(event, true);
    }

    /* (non-Javadoc)
     * @see org.everit.osgi.eventdispatcher.core.ManagableEventDispatcher#dispatchEvent(E)
     */
    @Override
    public void dispatchEvent(E event) {
        dispatchEventInternal(event, false);
    }

    private void dispatchEventInternal(E event, boolean removeAfterDispatch) {
        EK eventKey = eventUtil.getEventKey(event);
        WriteLock eventsToReplayWriteLocker = eventsToReplayLocker.writeLock();
        if (removeAfterDispatch) {
            eventsToReplayWriteLocker.lock();
            eventsToReplay.remove(eventKey);
            eventsToReplayWriteLocker.unlock();

        } else {
            eventsToReplayWriteLocker.lock();
            eventsToReplay.remove(eventKey);
            eventsToReplay.put(eventKey, new EventWithReplayFlag<E>(event));
            eventsToReplayWriteLocker.unlock();
        }

        for (Entry<LK, L> listenerEntry : listeners.entrySet()) {
            LK reference = listenerEntry.getKey();
            L listener = listenerEntry.getValue();
            callListener(reference, listener, event);
        }
    }

    /**
     * Doing the replay events for a new listener. This method is called within a write lock.
     * 
     * @param listenerKey
     *            The key of the listener.
     * @param listener
     *            The listener that has just been registered.
     */
    private void doReplaysForListener(LK listenerKey, L listener) {
        Collection<EventWithReplayFlag<E>> eventsWithReplayFlag = null;
        ReadLock readLock = eventsToReplayLocker.readLock();
        readLock.lock();
        try {
            eventsWithReplayFlag = new ArrayList<EventWithReplayFlag<E>>(eventsToReplay.values());
        } finally {
            readLock.unlock();
        }

        for (EventWithReplayFlag<E> eventWithReplayFlag : eventsWithReplayFlag) {
            if (!eventWithReplayFlag.isReplay()) {
                eventWithReplayFlag.setEvent(eventUtil.createReplayEvent(eventWithReplayFlag.getEvent()));
                eventWithReplayFlag.setReplay(true);
            }
            E event = eventWithReplayFlag.getEvent();
            callListener(listenerKey, listener, event);
        }
    }
    
    @Override
    public boolean removeEvent(EK eventKey) {
        return eventsToReplay.remove(eventKey) != null;
    }

    @Override
    public void removeListener(LK listenerKey) {
        listeners.remove(listenerKey);
    }
}
