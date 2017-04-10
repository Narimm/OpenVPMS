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

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;

/**
 * Default implementation of the {@link Queue} interface.
 *
 * @author Tim Anderson
 */
public class ServiceBusQueue implements Queue {

    /**
     * The contract.
     */
    private final ServiceBusContract service;

    /**
     * The queue name.
     */
    private final String queueName;

    /**
     * The message options.
     */
    private final ReceiveMessageOptions options;


    /**
     * Constructs a {@link ServiceBusQueue}.
     *
     * @param connectionString the connection string
     * @param queueName        the queue name
     */
    public ServiceBusQueue(String connectionString, String queueName) {
        Configuration config = Configuration.getInstance();
        ServiceBusConfiguration.configureWithConnectionString(null, config, connectionString);

        service = ServiceBusService.create(config);
        this.queueName = queueName;
        options = new ReceiveMessageOptions();
        options.setReceiveMode(ReceiveMode.PEEK_LOCK);
    }

    /**
     * Returns the next message from the queue.
     *
     * @return the next message, or {@code null} if there are no more messages.
     * @throws ServiceException if a service exception is encountered
     */
    @Override
    public BrokeredMessage next() throws ServiceException {
        ReceiveQueueMessageResult queueMessage = service.receiveQueueMessage(queueName, options);
        BrokeredMessage message = queueMessage.getValue();
        return (message != null && message.getMessageId() != null) ? message : null;
    }

    /**
     * Removes the message from the queue.
     *
     * @param message the message to remove
     * @throws ServiceException if a service exception is encountered
     */
    @Override
    public void remove(BrokeredMessage message) throws ServiceException {
        service.deleteMessage(message);
    }
}
