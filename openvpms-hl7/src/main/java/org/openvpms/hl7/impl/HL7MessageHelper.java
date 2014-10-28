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

import ca.uhn.hl7v2.model.v25.datatype.MSG;
import ca.uhn.hl7v2.model.v25.segment.MSH;

/**
 * HL7 Message helper methods.
 *
 * @author Tim Anderson
 */
public class HL7MessageHelper {

    /**
     * Returns a formatted name for a message.
     *
     * @param header the message header
     * @return the formatted type
     */
    public static String getMessageName(MSH header) {
        MSG type = header.getMessageType();
        return type.getMessageCode() + "^" + type.getTriggerEvent() + "^" + type.getMessageStructure();
    }

}
