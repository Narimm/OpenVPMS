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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.openvpms.smartflow.model.event.Event;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

/**
 * Reads and dispatches messages on a {@link Queue} using an {@link EventDispatcher}.
 *
 * @author Tim Anderson
 */
class QueueDispatcher {

    /**
     * The practice location that the queue is associated with.
     */
    private final Party location;

    /**
     * The queue.
     */
    private final Queue queue;

    /**
     * The event dispatcher.
     */
    private final EventDispatcher dispatcher;

    /**
     * The object mapper.
     */
    private final ObjectMapper mapper;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(QueueDispatcher.class);

    /**
     * Constructs a {@link QueueDispatcher}.
     *
     * @param location           the practice location
     * @param config             the Azure Service Bus configuration
     * @param dispatcher         the event dispatcher
     * @param mapper             the object mapper
     * @param transactionManager the transaction manager
     */
    public QueueDispatcher(Party location, ServiceBusConfig config, EventDispatcher dispatcher, ObjectMapper mapper,
                           PlatformTransactionManager transactionManager) {
        this.location = location;
        this.dispatcher = dispatcher;
        this.transactionManager = transactionManager;
        this.mapper = mapper;
        queue = createQueue(config.getConnectionString(), config.getQueueName());
    }

    /**
     * Returns the practice location.
     *
     * @return the practice location
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Dispatches the next message in the queue, if any.
     *
     * @return {@code true} if a message was dispatched
     * @throws IOException      if the message body can't be read
     * @throws ServiceException for any Azure Service Bus error
     */
    public boolean dispatch() throws IOException, ServiceException {
        boolean result = false;
        BrokeredMessage message = queue.next();
        if (message != null) {
            dispatch(message);
            result = true;
        } else if (log.isDebugEnabled()) {
            log.debug("No messages for location='" + location.getName() + "'");
        }
        return result;
    }

    /**
     * Returns the event dispatcher.
     *
     * @return the event dispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return dispatcher;
    }

    /**
     * Dispatches a message.
     *
     * @param message the message
     * @throws IOException      if the message body can't be read
     * @throws ServiceException for any Azure Service Bus error
     */
    protected void dispatch(BrokeredMessage message) throws IOException, ServiceException {
        Event event = getEvent(message);
        if (event != null) {
            dispatch(event, message);
        }
    }

    /**
     * Returns an event from a message.
     *
     * @param message the message
     * @return the event, or {@code null} if it cannot be deserialized
     * @throws IOException      if the message body can't be read
     * @throws ServiceException for any Azure Service Bus error
     */
    protected Event getEvent(BrokeredMessage message) throws IOException, ServiceException {
        String content = IOUtils.toString(message.getBody());
        if (log.isDebugEnabled()) {
            log.debug("location='" + location.getName() + "', messageID=" + message.getMessageId() + ", timeToLive="
                      + message.getTimeToLive() + ", sequence=" + message.getSequenceNumber() + ", contentType="
                      + message.getContentType() + ", content=" + content);
        }
        Event event = null;
        try {
            event = mapper.readValue(content, Event.class);
        } catch (Exception exception) {
            // can't read the message so discard it
            log.error("Failed to deserialize message for location='" + location.getName()
                      + "', messageID=" + message.getMessageId() + ", sequence=" + message.getSequenceNumber()
                      + ", timeToLive=" + message.getTimeToLive() + ", contentType=" + message.getContentType()
                      + ", content=" + content, exception);
            queue.remove(message);
        }
        return event;
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

}
