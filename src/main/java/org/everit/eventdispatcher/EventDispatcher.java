package org.everit.eventdispatcher;

public interface EventDispatcher<E, EK, L, LK> {

    void dispatchAndRemoveEvent(E event);

    void dispatchEvent(E event);

    boolean removeEvent(EK eventKey);

    void addListener(LK listenerKey, L listener);

    void removeListener(LK listenerKey);
}
