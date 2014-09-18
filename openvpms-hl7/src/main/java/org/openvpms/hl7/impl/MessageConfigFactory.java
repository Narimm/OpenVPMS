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

package org.openvpms.hl7.impl;

import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPSender;

/**
 * Factory for {@link MessageConfig} objects.
 *
 * @author Tim Anderson
 */
public class MessageConfigFactory {

    /**
     * Creates a new {@link MessageConfig}, initialised from a connector.
     *
     * @param connector the connector
     * @return a new {@link MessageConfig}.
     */
    public static MessageConfig create(Connector connector) {
        MessageConfig result = new MessageConfig();
        if (connector instanceof MLLPSender) {
            MLLPSender sender = (MLLPSender) connector;
            result.setIncludeMillis(sender.isIncludeMillis());
            result.setIncludeTimeZone(sender.isIncludeTimeZone());
        }
        return result;
    }
}
