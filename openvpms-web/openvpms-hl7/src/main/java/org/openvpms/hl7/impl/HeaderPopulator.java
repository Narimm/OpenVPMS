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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.EVN;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import org.openvpms.hl7.io.Connector;

import java.util.Date;

import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Populates the {@code MSH} and {@code EVN} segments of a message.
 *
 * @author Tim Anderson
 */
class HeaderPopulator {

    /**
     * Populates an MSH segment, and an EVN segment if present.
     *
     * @param message          the message
     * @param connector        the connector
     * @param timestamp        the date/time of the message
     * @param messageControlId the message control identifier
     * @param config           the message population configuration
     * @throws HL7Exception for any error
     */
    public void populate(Message message, Connector connector, Date timestamp, long messageControlId,
                         MessageConfig config)
            throws HL7Exception {
        MSH msh = (MSH) message.get("MSH");
        msh.getSendingApplication().getNamespaceID().setValue(connector.getSendingApplication());
        msh.getSendingFacility().getNamespaceID().setValue(connector.getSendingFacility());
        msh.getReceivingApplication().getNamespaceID().setValue(connector.getReceivingApplication());
        msh.getReceivingFacility().getNamespaceID().setValue(connector.getReceivingFacility());
        populateDTM(msh.getDateTimeOfMessage().getTime(), timestamp, config);
        msh.getMessageControlID().setValue(Long.toString(messageControlId));
        msh.getCharacterSet(0).setValue("UTF-8");

        // populate the EVN segment, if the message has one
        for (String name : message.getNames()) {
            if ("EVN".equals(name)) {
                EVN evn = (EVN) message.get(name);
                evn.getEventTypeCode().setValue(msh.getMessageType().getTriggerEvent().getValue());
                populateDTM(evn.getRecordedDateTime().getTime(), timestamp, config);
                evn.getEventFacility().getNamespaceID().setValue(msh.getSendingFacility().getNamespaceID().getValue());
                break;
            }
        }
    }
}
