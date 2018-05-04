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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.everit.eventdispatcher.internal.DefaultExceptionHandler;
import org.everit.eventdispatcher.internal.EventWithReplayFlag;
import org.everit.eventdispatcher.internal.ListenerData;

/**
 * Helper class to dispatch events to listeners. The dispatcher calls every listener that are
 * already registered with the newly registered events. In case a listener is registered it receives
 * all the events that were passed in the past with the optional replay flag. The class is
 * implemented in the way that the event and listener types are generic. The programmer must
 * implement the {@link EventUtil} interface and pass it to this class in order to have full
 * functionality. <br />
 * <br />
 * The class uses three different {@link ReentrantReadWriteLock}s. By using these locks the purpose
 * was to have as less locking during event dispatching as it is possible. The three locks are:
 *
 * <ul>
 * <li>l+: A lock on the listener instance. The plus sign means that each listener has it's own
 * locker object.</li>
 * <li>etr: Locking on the {@link #eventsToReplay} collection.</li>
 * <li>ls: Locking on the collection of listeners</li>
 * </ul>
 *
 * In the following part read and write locks are marked with (r) and (w) expressions. For example
 * ls(r) means that there is a read lock on the listener instance while ls(w) means a write lock.
 * <br />
 * <br />
 *
 * The following list shows the functions in this class and the locks that are used in the function
 * calls. The scope of the lock is the same as the scope of the list entry:
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
 * </ul>
 * <li>{@link #dispatchEvent(Object)}</li>
 * <ul>
 * <li><b>etr(w)</b>: Modify {@link #eventsToReplay} (remove and put if necessary).</li>
 * <li><b>ls(r)</b>: {@link #listeners}.clone()</li>
 * <li>clonedListeners.iterate()</li>
 * <ul>
 * <li>{@link #callListener(Object, ListenerData, Object)} <b>l(r)+</b></li>
 * </ul>
 * </ul>
 * <li><b>etr(w)</b>: {@link #removeEvent(Object)}</li>
 * <li><b>ls(w)</b>: {@link #removeListener(Object)}</li>
 * <li><b>l(r)+</b>: {@link #callListener(Object, ListenerData, Object)}</li>
 * </ul>
 *
 * Please note that {@link #callListener(Object, ListenerData, Object)} is called from two places.
 * The mentioned l(r)+ lock is placed into the function call. In the
 * {@link #callListener(Object, ListenerData, Object)} it is also checked with a ls(r) lock if the
 * listener is still active to avoid the possibility of calling a listener that was removed until
 * and event is dispatched to other listeners in the queue.
 * <h2>Usage</h2> To use this implementation the programmer must implement the {@link EventUtil}
 * interface and pass it to the constructor of this class. After that listeners can be registered
 * and events can be dispatched via the {@link EventDispatcher} interface. For more information
 * please see the documentation of the mentioned interfaces.
 *
 * @param <E>
 *          The type of the events.
 * @param <EK>
 *          The type of the event keys that identify the events based on the
 *          {@link Object#hashCode()} and {@link Object#equals(Object)} functions. Events are
 *          overridden by new events based on their key.
 * @param <L>
 *          The type of the listeners.
 * @param <LK>
 *          The type of the listener keys that identify the listener based on the
 *          {@link Object#hashCode()} and {@link Object#equals(Object)} functions.
 */
public class EventDispatcherImpl<E, EK, L, LK> implements EventDispatcher<E, EK, L, LK> {

  /**
   * Fair read-write locker of the events that should be replayed in case a new listener is
   * registered.
   */
  private final ReentrantReadWriteLock etrLocker = new ReentrantReadWriteLock(true);

  /**
   * The map of events that should be replayed in case of a new listener registration. The map
   * contains both
   */
  private final LinkedHashMap<EK, EventWithReplayFlag<E>> eventsToReplay =
      new LinkedHashMap<>();

  /**
   * The util class that must be implemented by the programmer who uses the {@link EventDispatcher}
   * functionality.
   */
  private final EventUtil<E, EK, L> eventUtil;

  private final ExceptionHandler<LK, E> exceptionHandler;

  /**
   * Listeners based on their key that are currently registered in registration order.
   */
  private final Map<LK, ListenerData<L>> listeners = new LinkedHashMap<>();

  /**
   * Fair read-write locker for the listener collection.
   */
  private final ReentrantReadWriteLock listenersLocker = new ReentrantReadWriteLock(true);

  /**
   * Simpler constructor that sets the {@link EventDispatcher#DEFAULT_LISTENER_CALL_TIMEOUT} as the
   * timeout for event processing.
   *
   * @param eventUtil
   *          The object that must be implemented and passed by the programmer to be able to use
   *          this library.
   */
  public EventDispatcherImpl(final EventUtil<E, EK, L> eventUtil) {
    this(eventUtil, null);
  }

  /**
   * Constructor.
   *
   * @param eventUtil
   *          The object that must be implemented and passed by the programmer to be able to use
   *          this library.
   *
   * @param exceptionHandler
   *          The handler that catches exceptions that come from listeners. If null, the default
   *          implementation will be used that writes exceptions to the standard error output.
   */
  public EventDispatcherImpl(final EventUtil<E, EK, L> eventUtil,
      final ExceptionHandler<LK, E> exceptionHandler) {
    this.eventUtil = eventUtil;
    if (exceptionHandler != null) {
      this.exceptionHandler = exceptionHandler;
    } else {
      this.exceptionHandler = new DefaultExceptionHandler<>();
    }
  }

  @Override
  public void addListener(final LK listenerKey, final L listener)
      throws ListenerAlreadyRegisteredException {
    ListenerData<L> listenerData = new ListenerData<>(listener);
    ReentrantReadWriteLock locker = listenerData.getLocker();

    WriteLock listenerWriteLock = locker.writeLock();
    listenerWriteLock.lock();
    try {

      Collection<E> cloneOfCurrentReplayEvents;

      WriteLock listenersWriteLock = this.listenersLocker.writeLock();
      listenersWriteLock.lock();
      try {
        ReadLock etrReadLock = this.etrLocker.readLock();
        etrReadLock.lock();
        try {

          ListenerData<L> alreadyRegisteredListener = this.listeners.put(listenerKey, listenerData);

          if (alreadyRegisteredListener != null) {
            throw new ListenerAlreadyRegisteredException(
                "Listener with key " + listenerKey.toString()
                    + " is already registered");
          }

          cloneOfCurrentReplayEvents = getCloneOfCurrentReplayEvents();

        } finally {
          etrReadLock.unlock();
        }
      } finally {
        listenersWriteLock.unlock();
      }

      for (E event : cloneOfCurrentReplayEvents) {
        callListener(listenerKey, listenerData, event);
      }

    } finally {
      listenerWriteLock.unlock();
    }
  }

  /**
   * Calling a listener with an event. In case there is any exception or a timeout the listener will
   * be removed from the listeners collection and no more events will be passed.
   *
   * @param listenerKey
   *          The reference of the listener OSGi service.
   * @param listener
   *          The listener object.
   * @param event
   *          The event.
   */
  private void callListener(final LK listenerKey, final ListenerData<L> listenerData,
      final E event) {

    ReentrantReadWriteLock listenerLocker = listenerData.getLocker();
    ReadLock listenerReadLock = listenerLocker.readLock();
    listenerReadLock.lock();

    try {
      this.eventUtil.callListener(listenerData.getListener(), event);
    } catch (Throwable e) {
      try {
        this.exceptionHandler.handleException(listenerKey, event, e);
      } catch (RuntimeException handlerE) {
        e.addSuppressed(handlerE);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        sw.write(
            "Error during calling exception handler after recieving an exception from listener '"
                + listenerKey.toString() + "' with the event: " + event.toString() + "\n");
        e.printStackTrace(pw);
        System.err.println(sw.toString());
      }
    } finally {
      listenerReadLock.unlock();
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
    EK eventKey = this.eventUtil.getEventKey(event);

    WriteLock etrWriteLock = this.etrLocker.writeLock();
    etrWriteLock.lock();

    if (removeAfterDispatch) {
      this.eventsToReplay.remove(eventKey);
    } else {
      this.eventsToReplay.remove(eventKey);
      this.eventsToReplay.put(eventKey, new EventWithReplayFlag<>(event));
    }

    etrWriteLock.unlock();

    ReadLock listenersReadLock = this.listenersLocker.readLock();
    listenersReadLock.lock();

    Collection<Entry<LK, ListenerData<L>>> clonedListneners =
        new ArrayList<>(
            this.listeners.entrySet());

    listenersReadLock.unlock();

    for (Entry<LK, ListenerData<L>> listenerEntry : clonedListneners) {
      LK listenerKey = listenerEntry.getKey();
      ListenerData<L> listenerData = listenerEntry.getValue();
      callListener(listenerKey, listenerData, event);
    }

  }

  private Collection<E> getCloneOfCurrentReplayEvents() {
    Collection<E> result = new ArrayList<>();

    for (EventWithReplayFlag<E> eventWithReplayFlag : this.eventsToReplay.values()) {
      if (!eventWithReplayFlag.isReplay()) {
        eventWithReplayFlag
            .setEvent(this.eventUtil.createReplayEvent(eventWithReplayFlag.getEvent()));
        eventWithReplayFlag.setReplay(true);
      }
      result.add(eventWithReplayFlag.getEvent());
    }
    return result;
  }

  @Override
  public boolean removeEvent(final EK eventKey) {
    WriteLock etrWriteLock = this.etrLocker.writeLock();
    etrWriteLock.lock();

    boolean result;
    try {
      result = this.eventsToReplay.remove(eventKey) != null;
    } finally {
      etrWriteLock.unlock();
    }

    return result;
  }

  @Override
  public boolean removeListener(final LK listenerKey) {
    WriteLock listenersWriteLock = this.listenersLocker.writeLock();
    listenersWriteLock.lock();

    boolean result;
    try {
      result = this.listeners.remove(listenerKey) != null;
    } finally {
      listenersWriteLock.unlock();
    }
    return result;
  }
}
