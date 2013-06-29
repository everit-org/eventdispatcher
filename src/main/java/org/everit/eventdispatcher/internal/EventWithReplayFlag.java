package org.everit.eventdispatcher.internal;

public class EventWithReplayFlag<E> {

    private boolean replay = false;
    private E event;

    public EventWithReplayFlag(E event) {
        this.event = event;
    }

    public boolean isReplay() {
        return replay;
    }

    public void setReplay(boolean replay) {
        this.replay = replay;
    }

    public E getEvent() {
        return event;
    }

    public void setEvent(E event) {
        this.event = event;
    }
    
    
}
