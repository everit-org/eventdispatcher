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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.everit.eventdispatcher.internal.EventWithReplayFlag;
import org.everit.eventdispatcher.internal.ListenerCallMeta;
import org.everit.eventdispatcher.internal.ListenerData;
import org.everit.eventdispatcher.internal.TimeoutCallback;
import org.everit.eventdispatcher.internal.TimeoutCheckerThread;

/**
 * Helper class to dispatch events to listeners. The dispatcher calls every listener that are already registered with
 * the newly registered events. In case a listener is registered it receives all the events that were passed in the past
 * with the optional replay flag. The class is implemented in the way that the event and listener types are generic. The
 * programmer must implement the {@link EventUtil} interface and pass it to this class in order to have full
 * functionality. <br />
 * <br />
 * The class uses three different {@link ReentrantReadWriteLock}s. By using these locks the purpose was to have as less
 * locking during event dispatching as it is possible. The three locks are:
 * 
 * <ul>
 * <li>l+: A lock on the listener instance. The plus sign means that each listener has it's own locker object.</li>
 * <li>etr: Locking on the {@link #eventsToReplay} collection.</li>
 * <li>ls: Locking on the collection of listeners</li>
 * </ul>
 * 
 * In the following part read and write locks are marked with (r) and (w) expressions. For example ls(r) means that
 * there
 * is a read lock on the listener instance while ls(w) means a write lock. <br />
 * <br />
 * 
 * The following list shows the functions in this class and the locks that are used in the function calls. The scope of
 * the lock is the same as the scope of the list entry:
 * 
 * <ul>
 * <li><b>l(w)+</b></li>
 * <ul>
 * <li>{@link #addListener(Object, Object)}</li>
 * <ul>
 * <li><b>ls(w)</b>, <b>etr(r)</b></li>
 * <ul>
 * <li>{@link #listeners}.add()</li>
 * <li>{@link #eventsToReplay}.clone()</li>
 * </ul>
 * <li>clonedeventsToReplay.iterate</li>
 * <ul>
 * <li>{@link #callListener(Object, ListenerData, Object)} <b>l(r)+</b></li>
 * </ul>
 * </ul>
 * </ul> <li>{@link #dispatchEvent(Object)}</li>
 * <ul>
 * <li><b>etr(w)</b>: Modify {@link #eventsToReplay} (remove and put if necessary).</li>
 * <li><b>ls(r)</b>: {@link #listeners}.clone()</li>
 * <li>clonedListeners.iterate()</li>
 * <ul>
 * <li>{@link #callListener(Object, ListenerData, Object)} <b>l(r)+</b></li>
 * </ul>
 * </ul> <li><b>etr(w)</b>: {@link #removeEvent(Object)}</li> <li><b>ls(w)</b>: {@link #removeListener(Object)}</li><li>
 * <b>l(r)+</b>: {@link #callListener(Object, ListenerData, Object)}</li> </ul>
 * 
 * Please note that {@link #callListener(Object, ListenerData, Object)} is called from two places. The mentioned l(r)+
 * lock is placed into the function call. In the {@link #callListener(Object, ListenerData, Object)} it is also checked
 * with a ls(r) lock if the listener is still active to avoid the possibility of calling a listener that was removed
 * until and event is dispatched to other listeners in the queue. <h2>Usage</h2> To use this implementation the
 * programmer must implement the {@link EventUtil} interface and pass it to the constructor of this class. After that
 * listeners can be registered and events can be dispatched via the {@link EventDispatcher} interface. For more
 * information please see the documentation of the mentioned interfaces.
 * 
 * @param <E>
 *            The type of the events.
 * @param <EK>
 *            The type of the event keys that identify the events based on the {@link Object#hashCode()} and
 *            {@link Object#equals(Object)} functions. Events are overridden by new events based on their key.
 * @param <L>
 *            The type of the listeners.
 * @param <LK>
 *            The type of the listener keys that identify the listener based on the {@link Object#hashCode()} and
 *            {@link Object#equals(Object)} functions.
 */
public class EventDispatcherImpl<E, EK, L, LK> implements EventDispatcher<E, EK, L, LK> {

    /**
     * The map of events that should be replayed in case of a new listener registration. The map contains both
     */
    private LinkedHashMap<EK, EventWithReplayFlag<E>> eventsToReplay = new LinkedHashMap<EK, EventWithReplayFlag<E>>();

    /**
     * The util class that must be implemented by the programmer who uses the {@link EventDispatcher} functionality.
     */
    private final EventUtil<E, EK, L> eventUtil;

    /**
     * Fair read-write locker for the listener collection.
     */
    private final ReentrantReadWriteLock listenersLocker = new ReentrantReadWriteLock(true);

    /**
     * Fair read-write locker of the events that should be replayed in case a new listener is registered.
     */
    private final ReentrantReadWriteLock etrLocker = new ReentrantReadWriteLock(true);

    private boolean stopped = false;

    /**
     * Listeners that are blacklisted due to a timeout or exception.
     */
    private final Map<LK, Boolean> blackListedListeners = new ConcurrentHashMap<LK, Boolean>();

    private final TimeoutCheckerThread<LK> timeoutChecker;

    /**
     * Listeners based on their key that are currently registered in registration order.
     */
    private Map<LK, ListenerData<L>> listeners = new LinkedHashMap<LK, ListenerData<L>>();

    /**
     * See {@link EventDispatcher#getListenerCallTimeout()}.
     */
    private final long listenerCallTimeout;

    /**
     * Simpler constructor that sets the {@link EventDispatcher#DEFAULT_LISTENER_CALL_TIMEOUT} as the timeout for event
     * processing.
     * 
     * @param eventUtil
     *            The object that must be implemented and passed by the programmer to be able to use this library.
     */
    public EventDispatcherImpl(final EventUtil<E, EK, L> eventUtil) {
        this(eventUtil, EventDispatcher.DEFAULT_LISTENER_CALL_TIMEOUT);
    }

    /**
     * Constructor.
     * 
     * @param eventUtil
     *            The object that must be implemented and passed by the programmer to be able to use this library.
     * 
     * @param listenerCallTimeout
     *            See {@link #getListenerCallTimeout()}.
     */
    public EventDispatcherImpl(final EventUtil<E, EK, L> eventUtil, final long listenerCallTimeout) {
        this.eventUtil = eventUtil;
        this.listenerCallTimeout = listenerCallTimeout;
        if (listenerCallTimeout > 0) {
            this.timeoutChecker = new TimeoutCheckerThread<LK>(listenerCallTimeout, new TimeoutCallback<LK>() {

                @Override
                public void takeListenerToBlacklist(final LK listenerKey) {
                    markListenerBlackListed(listenerKey, null);
                }
            });
            new Thread(timeoutChecker).start();
        } else {
            this.timeoutChecker = null;
            if (listenerCallTimeout < 0) {
                throw new IllegalArgumentException("Listener call timeout must be zero or a positive number.");
            }
        }
    }

    @Override
    public void addListener(final LK listenerKey, final L listener) throws ListenerAlreadyRegisteredException {
        ListenerData<L> listenerData = new ListenerData<L>(listener);
        ReentrantReadWriteLock locker = listenerData.getLocker();
        WriteLock listenerWriteLock = locker.writeLock();
        listenerWriteLock.lock();

        WriteLock listenersWriteLock = listenersLocker.writeLock();
        listenersWriteLock.lock();
        ReadLock etrReadLock = etrLocker.readLock();
        etrReadLock.lock();

        ListenerData<L> alreadyRegisteredListener = listeners.put(listenerKey, listenerData);
        if (alreadyRegisteredListener != null) {
            etrReadLock.unlock();
            listenersWriteLock.unlock();
            listenerWriteLock.unlock();
            throw new ListenerAlreadyRegisteredException("Listener with key " + listenerKey.toString()
                    + " is already registered");
        }
        Collection<E> cloneOfCurrentReplayEvents = getCloneOfCurrentReplayEvents(listenerKey, listener);

        etrReadLock.unlock();
        listenersWriteLock.unlock();

        for (E event : cloneOfCurrentReplayEvents) {
            callListener(listenerKey, listenerData, event);
        }

        listenerWriteLock.unlock();

    }

    /**
     * Calling a listener with an event. In case there is any exception or a timeout the listener will be removed from
     * the listeners collection and no more events will be passed.
     * 
     * @param listenerKey
     *            The reference of the listener OSGi service.
     * @param listener
     *            The listener object.
     * @param event
     *            The event.
     * @return true if the call was successful, false if the listener was blacklisted due to an exception or timeout.
     */
    private boolean callListener(final LK listenerKey, final ListenerData<L> listenerData, final E event) {

        ReentrantReadWriteLock listenerLocker = listenerData.getLocker();
        ReadLock listenerReadLock = listenerLocker.readLock();
        listenerReadLock.lock();
        boolean result = true;

        if (isListenerActive(listenerKey)) {
            ListenerCallMeta<LK> listenerCallMeta = null;
            if (timeoutChecker != null) {
                listenerCallMeta = timeoutChecker.startCall(listenerKey);
            }

            try {
                eventUtil.callListener(listenerData.getListener(), event);
            } catch (RuntimeException e) {
                markListenerBlackListed(listenerKey, e);
                result = false;
            }
            if (timeoutChecker != null) {
                timeoutChecker.callEnded(listenerCallMeta);
            }
        }

        listenerReadLock.unlock();
        return result;

    }

    @Override
    public void close() {
        stopped = true;
        if (timeoutChecker != null) {
            timeoutChecker.shutdown();
        }

    }

    @Override
    public void dispatchAndRemoveEvent(final E event) {
        dispatchEventInternal(event, true);
    }

    @Override
    public void dispatchEvent(final E event) {
        dispatchEventInternal(event, false);
    }

    private void dispatchEventInternal(final E event, final boolean removeAfterDispatch) {
        if (stopped) {
            throw new IllegalStateException("Event dispatcher is already stopped");
        }

        EK eventKey = eventUtil.getEventKey(event);

        WriteLock etrWriteLock = etrLocker.writeLock();
        etrWriteLock.lock();

        if (removeAfterDispatch) {
            eventsToReplay.remove(eventKey);
        } else {
            eventsToReplay.remove(eventKey);
            eventsToReplay.put(eventKey, new EventWithReplayFlag<E>(event));
        }

        etrWriteLock.unlock();

        ReadLock listenersReadLock = listenersLocker.readLock();
        listenersReadLock.lock();

        Collection<Entry<LK, ListenerData<L>>> clonedListneners = new ArrayList<Map.Entry<LK, ListenerData<L>>>(
                listeners.entrySet());

        listenersReadLock.unlock();

        for (Entry<LK, ListenerData<L>> listenerEntry : clonedListneners) {
            LK listenerKey = listenerEntry.getKey();
            ListenerData<L> listenerData = listenerEntry.getValue();
            callListener(listenerKey, listenerData, event);
        }

    }

    private Collection<E> getCloneOfCurrentReplayEvents(final LK listenerKey, final L listener) {
        Collection<E> result = new ArrayList<E>();

        for (EventWithReplayFlag<E> eventWithReplayFlag : eventsToReplay.values()) {
            if (!eventWithReplayFlag.isReplay()) {
                eventWithReplayFlag.setEvent(eventUtil.createReplayEvent(eventWithReplayFlag.getEvent()));
                eventWithReplayFlag.setReplay(true);
            }
            result.add(eventWithReplayFlag.getEvent());
        }
        return result;
    }

    @Override
    public long getListenerCallTimeout() {
        return this.listenerCallTimeout;
    }

    private boolean isListenerActive(final LK listenerKey) {
        if (isListenerBlacklisted(listenerKey)) {
            return false;
        }

        ReadLock listenersReadLock = listenersLocker.readLock();
        listenersReadLock.lock();
        boolean result = listeners.containsKey(listenerKey);
        listenersReadLock.unlock();

        return result;
    }

    @Override
    public boolean isListenerBlacklisted(final LK listenerKey) {
        return blackListedListeners.containsKey(listenerKey);
    }

    private void markListenerBlackListed(final LK listenerKey, final Exception e) {
        blackListedListeners.put(listenerKey, true);
        System.out.println("Listener got blacklisted: " + listenerKey.toString() + ". Reason: "
                + ((e == null) ? "timeout." : e.getMessage()));
        // TODO log blacklisting with cause.
    }

    @Override
    public boolean removeEvent(final EK eventKey) {
        WriteLock etrWriteLock = etrLocker.writeLock();
        etrWriteLock.lock();

        boolean result = eventsToReplay.remove(eventKey) != null;

        etrWriteLock.unlock();
        return result;
    }

    @Override
    public boolean removeListener(final LK listenerKey) {
        WriteLock listenersWriteLock = listenersLocker.writeLock();
        listenersWriteLock.lock();

        boolean result = listeners.remove(listenerKey) != null;
        blackListedListeners.remove(listenerKey);

        listenersWriteLock.unlock();
        return result;
    }
}
