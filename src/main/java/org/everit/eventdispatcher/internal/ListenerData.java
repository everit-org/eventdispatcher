package org.everit.eventdispatcher.internal;

import java.util.LinkedList;

public class ListenerData<L, E> {

    private L listener;

    private boolean blacklisted = false;

    private LinkedList<E> eventQueue = new LinkedList<E>();

    public ListenerData(L listener) {
        this.listener = listener;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public L getListener() {
        return listener;
    }

    public LinkedList<E> getEventQueue() {
        return eventQueue;
    }

}
