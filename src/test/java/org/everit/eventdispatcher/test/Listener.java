package org.everit.eventdispatcher.test;

public interface Listener<E> {

    void receiveEvent(E event);
}
