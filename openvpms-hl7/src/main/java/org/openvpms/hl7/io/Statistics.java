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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.io;

import java.util.Date;

/**
 * HL7 connector statistics.
 *
 * @author Tim Anderson
 */
public interface Statistics {

    /**
     * Returns the number of messages in the queue.
     * <p/>
     * Only applies to sending connectors.
     *
     * @return the number of messages
     */
    int size();

    /**
     * Returns the time of the last processed message.
     * <p/>
     * For senders, this indicates the time when a message was last sent, and an acknowledgment received.
     * <p/>
     * For receivers, this indicates the time when a message was last received and processed.
     *
     * @return the time when a message was last processed, or {@code null} if none have been processed
     */
    Date getProcessedTimestamp();

    /**
     * Returns the time of the last error.
     *
     * @return the time of the last error, or {@code null} if the last message was successfully processed
     */
    Date getErrorTimestamp();

    /**
     * Returns the error message of the last error.
     *
     * @return the last error message. May be {@code null}
     */
    String getErrorMessage();

    /**
     * Returns the connector that these statistics apply to.
     *
     * @return the connector
     */
    Connector getConnector();

}