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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.openvpms.smartflow.model.event.Event;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Reads and dispatches messages on a {@link Queue} using a {@link EventDispatcher}.
 *
 * @author Tim Anderson
 */
public class QueueReader {

    /**
     * The queue.
     */
    private final Queue queue;

    /**
     * The event dispatcher.
     */
    private final EventDispatcher dispatcher;

    /**
     * The user to associate with the {@link EventDispatcher} thread.
     */
    private final User user;

    /**
     * The executor service.
     */
    private final ExecutorService executor;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The interval between polls, in seconds.
     */
    private final int interval;

    /**
     * The object mapper.
     */
    private final ObjectMapper mapper;

    /**
     * Determines if {@link #destroy()} has been invoked.
     */
    private volatile boolean shutdown;

    /**
     * Used to restricted the number of tasks that can be scheduled via the executor.
     */
    private final Semaphore running = new Semaphore(1);

    /**
     * Used to wait for scheduling the next poll.
     */
    private final Semaphore pause = new Semaphore(0);

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(QueueReader.class);

    /**
     * Constructs a {@link QueueReader}.
     *
     * @param config             the Azure Service Bus configuration
     * @param dispatcher         the event dispatcher
     * @param user               the user to process events as
     * @param executor           the thread pool
     * @param transactionManager the transaction manager
     * @param interval           the poll interval, in seconds
     */
    public QueueReader(ServiceBusConfig config, EventDispatcher dispatcher, User user, ExecutorService executor,
                       PlatformTransactionManager transactionManager, int interval) {
        this.dispatcher = dispatcher;
        this.user = user;
        this.executor = executor;
        this.transactionManager = transactionManager;
        this.interval = interval;
        mapper = new ObjectMapper();
        queue = createQueue(config.getConnectionString(), config.getQueueName());
    }

    /**
     * Triggers a poll for events.
     */
    public void poll() {
        schedule();
    }

    /**
     * Destroys this reader.
     */
    public void destroy() {
        shutdown = true;
        pause.release(); // wakes up pause() if it is waiting
        try {
            running.tryAcquire(30, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
            // do nothing
        }
    }

    /**
     * Schedules a dispatch.
     */
    protected void schedule() {
        pause.release(); // wakes up pause() if it is waiting

        if (isShutdown()) {
            log.debug("QueueReader shut down. Schedule request ignored");
        } else if (running.tryAcquire()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        RunAs.run(user, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    dispatch();
                                    if (!isShutdown()) {
                                        pause();
                                    }
                                } finally {
                                    running.release();
                                }
                                if (!isShutdown()) {
                                    schedule();
                                }
                            }
                        });
                    } catch (Throwable exception) {
                        log.error(exception.getMessage(), exception);
                    }
                }
            });
        } else {
            log.debug("QueueReader already scheduled");
        }
    }

    /**
     * Dispatches all messages on the queue until there are no messages, an error occurs, or {@link #isShutdown()}
     * returns {@code true}.
     */
    protected void dispatch() {
        while (!isShutdown()) {
            try {
                BrokeredMessage message = queue.next();
                if (message != null) {
                    dispatch(message);
                } else {
                    break;
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
                break;
            }
        }
    }

    /**
     * Dispatches a message.
     *
     * @param message the message
     * @throws IOException      if the message body can't be read
     * @throws ServiceException for any Azure Service Bus error
     */
    protected void dispatch(BrokeredMessage message) throws IOException, ServiceException {
        String content = IOUtils.toString(message.getBody());
        if (log.isDebugEnabled()) {
            log.debug("messageID=" + message.getMessageId() + ", timeToLive=" + message.getTimeToLive()
                      + ", sequence=" + message.getSequenceNumber() + ", contentType="
                      + message.getContentType() + ", content=" + content);
        }
        Event event = null;
        try {
            event = mapper.readValue(content, Event.class);
        } catch (Exception exception) {
            // can't read the message so discard it
            log.error("Failed to deserialize message, messageID=" + message.getMessageId()
                      + ", sequence=" + message.getSequenceNumber() + ", timeToLive=" + message.getTimeToLive()
                      + ", contentType=" + message.getContentType() + ", content=" + content, exception);
            queue.remove(message);
        }
        if (event != null) {
            dispatch(event, message);
        }
    }

    /**
     * Dispatches an event, removing the message if it is dispatched successfully.
     *
     * @param event   the event
     * @param message the message
     * @throws ServiceException for any Azure Service Bus error
     */
    protected void dispatch(final Event event, final BrokeredMessage message) throws ServiceException {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        try {
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    dispatcher.dispatch(event);
                    try {
                        queue.remove(message);
                    } catch (ServiceException exception) {
                        throw new RuntimeException(exception);
                    }
                }
            });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof ServiceException) {
                throw (ServiceException) exception.getCause();
            } else {
                throw exception;
            }
        }
    }

    /**
     * Creates a queue.
     *
     * @param connectionString the connection string
     * @param queueName        the queue name
     * @return a new queue
     */
    protected Queue createQueue(String connectionString, String queueName) {
        return new ServiceBusQueue(connectionString, queueName);
    }

    /**
     * Pauses between polls.
     */
    private void pause() {
        if (interval > 0) {
            log.debug("QueueReader waiting for " + interval + "s");
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
