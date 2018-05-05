/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.security.RunAs;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled to dispatch events using {@link QueueDispatcher}s.
 *
 * @author Tim Anderson
 */
class ScheduledDispatcher {

    /**
     * The dispatchers.
     */
    private final QueueDispatchers dispatchers;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The executor service.
     */
    private final ExecutorService executor;

    /**
     * Used to restricted the number of tasks that can be scheduled via the executor.
     */
    private final Semaphore running = new Semaphore(1);

    /**
     * Used to wait for scheduling the next poll.
     */
    private final Semaphore pause = new Semaphore(0);

    /**
     * The interval between polls, in seconds.
     */
    private volatile int pollInterval = 30;

    /**
     * The interval to pause after failure, in seconds.
     */
    private volatile int failureInterval = 60;

    /**
     * Determines if {@link #destroy()} has been invoked.
     */
    private volatile boolean shutdown;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ScheduledDispatcher.class);

    /**
     * Constructs a {@link ScheduledDispatcher}.
     *
     * @param dispatchers     the dispatchers
     * @param practiceService the practice service
     */
    public ScheduledDispatcher(QueueDispatchers dispatchers, PracticeService practiceService) {
        this.dispatchers = dispatchers;
        this.practiceService = practiceService;
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Sets the interval between each poll.
     *
     * @param pollInterval the interval, in seconds
     */
    public void setPollInterval(int pollInterval) {
        if (pollInterval <= 0) {
            throw new IllegalArgumentException("Argument 'pollInterval' must be > 0");
        }
        this.pollInterval = pollInterval;
    }

    /**
     * Sets the interval to wait after a failure.
     *
     * @param failureInterval the interval, in seconds
     */
    public void setFailureInterval(int failureInterval) {
        if (failureInterval <= 0) {
            throw new IllegalArgumentException("Argument 'failureInterval' must be > 0");
        }
        this.failureInterval = failureInterval;
    }

    /**
     * Dispatch events.
     */
    public void dispatch() {
        schedule();
    }

    /**
     * Destroys this dispatcher.
     */
    public void destroy() {
        shutdown = true;
        pause.release(); // wakes up pause() if it is waiting
        try {
            running.tryAcquire(30, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
            // do nothing
        }
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException exception) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Schedules a dispatch.
     */
    protected void schedule() {
        pause.release(); // wakes up pause() if it is waiting

        if (isShutdown()) {
            log.debug("ScheduledDispatcher shut down. Schedule request ignored");
        } else if (running.tryAcquire()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    dispatchAndReschedule();
                }
            });
        } else {
            log.debug("ScheduledDispatcher already scheduled");
        }
    }

    /**
     * Dispatches all messages on the queue, before pausing and rescheduling.
     * <p>
     * If it is interrupted, terminates without re-scheduling.
     */
    protected void dispatchAndReschedule() {
        try {
            User user = practiceService.getServiceUser();
            if (user != null) {
                boolean success = RunAs.run(user, new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return dispatchAll();
                    }
                });
                if (success) {
                    pause(pollInterval);
                } else {
                    // there were no queues or none could be read, so pause for a longer period.
                    pause(failureInterval);
                }
            } else {
                log.error("Missing party.organisationPractice serviceUser. Messages cannot be processed until " +
                          "this is configured");
                pause(failureInterval);
            }
        } catch (Throwable exception) {
            log.error(exception.getMessage(), exception);
            pause(failureInterval);
        } finally {
            running.release();
        }
        if (!isShutdown()) {
            if (!dispatchers.getDispatchers().isEmpty()) {
                // only reschedule if there a queues available
                schedule();
            } else {
                log.info("Not rescheduling until there are Azure Service Bus queues available");
            }
        }
    }

    /**
     * Dispatches messages for all registered dispatchers, until there are no more messages or
     * {@link #destroy() is invoked}.
     * <p>
     * If an error occurs, working queues will continue to be processed.
     *
     * @return {@code true} if there was at least one queue and it didn't fail on the last dispatch
     */
    protected boolean dispatchAll() {
        boolean dispatched;
        List<QueueDispatcher> available = dispatchers.getDispatchers();
        do {
            dispatched = false;
            Iterator<QueueDispatcher> iterator = available.iterator();
            while (iterator.hasNext()) {
                QueueDispatcher dispatcher = iterator.next();
                try {
                    dispatched |= dispatcher.dispatch();
                } catch (Exception exception) {
                    // dispatch failed, so exclude this from subsequent dispatches
                    iterator.remove();
                    log.error("Failed to dispatch message for location='" + dispatcher.getLocation().getName() + "': "
                              + exception.getMessage(), exception);
                }
                if (isShutdown()) {
                    dispatched = false;
                    break;
                }
            }
        } while (dispatched);
        return (!available.isEmpty());
    }

    /**
     * Pauses between polls.
     *
     * @param interval the time to pause
     */
    private void pause(long interval) {
        if (interval > 0) {
            log.debug("ScheduledDispatcher waiting for " + interval + "s");
            // wait until the interval has expired, or a poll() occurs
            pause.drainPermits();
            if (!isShutdown()) {
                try {
                    pause.tryAcquire(interval, TimeUnit.SECONDS);
                } catch (InterruptedException ignore) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Determines if the reader has been shut down.
     *
     * @return {@code true} if {@link #destroy()} has been invoked, or the thread has been interrupted
     */
    private boolean isShutdown() {
        return shutdown || Thread.currentThread().isInterrupted();
    }

}
