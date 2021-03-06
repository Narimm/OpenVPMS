/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.sms;


/**
 * Creates connections to an SMS provider.
 *
 * @author Tim Anderson
 */
public interface ConnectionFactory {

    /**
     * Creates a new connection.
     *
     * @return a new connection
     * @throws SMSException if the connection cannot be created
     */
    Connection createConnection();

    /**
     * Returns the maximum number of message parts supported by the SMS provider.
     *
     * @return the maximum number of message parts
     */
    int getMaxParts();

}