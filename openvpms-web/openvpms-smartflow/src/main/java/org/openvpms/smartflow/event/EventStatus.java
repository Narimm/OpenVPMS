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
 * Smart Flow Sheet event status.
 *
 * @author Tim Anderson
 */
public class EventStatus {

    /**
     * The time when an event was last received.
     */
    private final Date received;

    /**
     * The time of the last error.
     */
    private final Date error;

    /**
     * The error message.
     */
    private final String errorMessage;

    /**
     * Constructs a {@link EventStatus}.
     *
     * @param received     the time when an event was last received, or {@code null} if none has been received
     * @param error        the time when an error last occurred, or {@code null} if no error has occurred, or the the
     *                     last event was processed successfully
     * @param errorMessage the error message associated with the last error or {@code null} if no error has occurred,
     *                     or the the last event was processed successfully
     */
    public EventStatus(Date received, Date error, String errorMessage) {
        this.received = received;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the time when an event was last received.
     *
     * @return the time when an event was last received, or {@code null} if no message has been received
     */
    public Date getReceived() {
        return received;
    }

    /**
     * Returns the time when an error last occurred.
     *
     * @return the time when an error last occurred, or {@code null} if no error has occurred or the last event was
     * processed successfully
     */
    public Date getError() {
        return error;
    }

    /**
     * Returns the error message for the time when an error last occurred.
     *
     * @return the error message for the time when an error last occurred, or {@code null} if no error has occurred or
     * the last event was processed successfully
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
