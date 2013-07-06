package org.everit.eventdispatcher.internal;

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

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TimeoutCheckerThread<LK> extends Thread {

    private boolean stopped = false;

    /**
     * Using a single linked queue to store the listener calls. When a call is done a new element is inserted into the
     * queue. When a call is ended the data is removed from the queue. In case many calls would be in the queue at the
     * same time a concurrent ordered map might have been better due to remove performance, however only a few call
     * expected at the same moment.
     */
    private final ConcurrentLinkedQueue<ListenerCallMeta<LK>> callQueue =
            new ConcurrentLinkedQueue<ListenerCallMeta<LK>>();

    private final TimeoutCallback<LK> timeoutCallback;

    private final long timeout;

    private final Object waiter = new Object();

    public TimeoutCheckerThread(final long timeout, final TimeoutCallback<LK> timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
        this.timeout = timeout;
    }

    public void callEnded(final ListenerCallMeta<LK> listenerCallMeta) {
        callQueue.remove(listenerCallMeta);
    }

    @Override
    public void run() {
        while (!stopped) {
            ListenerCallMeta<LK> listenerCall = callQueue.peek();
            if (listenerCall == null) {
                waitAndStopIfInterrupted(timeout);
            } else {
                long currentTime = new Date().getTime();
                long callTime = listenerCall.getCallTime();
                long gap = (callTime + timeout) - currentTime;
                if (gap > 0) {
                    waitAndStopIfInterrupted(gap);
                } else {
                    timeoutCallback.takeListenerToBlacklist(listenerCall.getListenerKey());
                    callQueue.remove(listenerCall);
                }
            }
        }
    }

    private void waitAndStopIfInterrupted(long millis) {
        try {
            synchronized (waiter) {
                waiter.wait(millis);
            }
        } catch (InterruptedException e) {
            stopped = true;
            interrupt();
        }
    }

    public void shutdown() {
        stopped = true;
        synchronized (waiter) {
            waiter.notify();
        }
    }

    public ListenerCallMeta<LK> startCall(final LK listenerKey) {
        ListenerCallMeta<LK> listenerCallMeta =
                new ListenerCallMeta<LK>(listenerKey, new Date().getTime());
        callQueue.add(listenerCallMeta);
        return listenerCallMeta;
    }
}
