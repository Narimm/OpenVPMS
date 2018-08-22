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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event;

import java.util.Date;

/**
 * .
 *
 * @author Tim Anderson
 */
public class EventStatus {

    private final Date lastReceived;

    private final Date lastError;

    private final String errorMessage;

    public EventStatus(Date lastReceived, Date lastError, String errorMessage) {
        this.lastReceived = lastReceived;
        this.lastError = lastError;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the time when a message was last received.
     *
     * @return the time when a message was last received, or {@code null} if no message has been received
     */
    public Date getLastReceived() {
        return lastReceived;
    }

    public Date getLastError() {
        return lastError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
