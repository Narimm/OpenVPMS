package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.EVN;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import org.openvpms.hl7.Connector;

import java.util.Date;

/**
 * Populates the {@code MSH} and {@code EVN} segments of a message.
 *
 * @author Tim Anderson
 */
class HeaderPopulator {

    public void populate(Message message, Connector connector, Date timestamp, long sequence)
            throws HL7Exception {
        MSH msh = (MSH) message.get("MSH");
        msh.getSendingApplication().getNamespaceID().setValue(connector.getSendingApplication());
        msh.getSendingFacility().getNamespaceID().setValue(connector.getSendingFacility());
        msh.getReceivingApplication().getNamespaceID().setValue(connector.getReceivingApplication());
        msh.getReceivingFacility().getNamespaceID().setValue(connector.getReceivingFacility());
        msh.getDateTimeOfMessage().getTime().setValue(timestamp);
        msh.getMessageControlID().setValue(Long.toString(sequence));
        msh.getCharacterSet(0).setValue("UTF-8");

        // populate the EVN segment, if the message has one
        for (String name : message.getNames()) {
            if ("EVN".equals(name)) {
                EVN evn = (EVN) message.get(name);
                evn.getEventTypeCode().setValue(msh.getMessageType().getTriggerEvent().getValue());
                evn.getRecordedDateTime().getTime().setValue(msh.getDateTimeOfMessage().getTime().getValueAsCalendar());
                evn.getEventFacility().getNamespaceID().setValue(msh.getSendingFacility().getNamespaceID().getValue());
                break;
            }
        }
    }
}
