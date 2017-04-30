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

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;

/**
 * Azure Service Bus queue.
 *
 * @author Tim Anderson
 */
public interface Queue {

    /**
     * Returns the next message from the queue.
     *
     * @return the next message, or {@code null} if there are no more messages.
     * @throws ServiceException if a service exception is encountered
     */
    BrokeredMessage next() throws ServiceException;

    /**
     * Removes the message from the queue.
     *
     * @param message the message to remove
     * @throws ServiceException if a service exception is encountered
     */
    void remove(BrokeredMessage message) throws ServiceException;
}
