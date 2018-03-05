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

package org.openvpms.smartflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Azure Service Bus configuration.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceBusConfig {

    /**
     * The Azure Service Bus connection string.
     */
    private String connectionString;

    /**
     * The queue name.
     */
    private String queueName;

    /**
     * Returns the Azure Service Bus connection string.
     *
     * @return the connection string
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Sets the Azure Service Bus connection string.
     *
     * @param connectionString the connection string
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Returns the queue name.
     *
     * @return the queue name
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Sets the queue name.
     *
     * @param queueName the queue name
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
