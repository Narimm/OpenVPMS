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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.hl7.util.HL7MessageStatuses;

import java.util.Date;

/**
 * HL7 message service.
 *
 * @author Tim Anderson
 */
public interface MessageService {

    /**
     * Saves a message.
     *
     * @param message   the message
     * @param connector the associated connector
     * @param user      the user responsible for the message
     * @return the persistent message
     * @throws HL7Exception              for any HL7 error
     * @throws ArchetypeServiceException for any archetype service error
     */
    DocumentAct save(Message message, Connector connector, User user) throws HL7Exception;

    /**
     * Updates a persistent message to indicate it has been sent.
     *
     * @param message   the message
     * @param timestamp the sent timestamp
     * @throws ArchetypeServiceException for any archetype service error
     */
    void sent(DocumentAct message, Date timestamp);

    /**
     * Updates a persistent message to indicate it is in error.
     *
     * @param message   message
     * @param status    the new status
     * @param timestamp the error timestamp
     * @param error     the error message
     */
    void error(DocumentAct message, String status, Date timestamp, String error);

    /**
     * Decodes an HL7 message from a persistent message.
     *
     * @param message the persistent message
     * @param parser  the parser to use
     * @return the decoded message
     * @throws HL7Exception              for any HL7 error
     * @throws ArchetypeServiceException for any archetype service error
     */
    Message get(DocumentAct message, Parser parser) throws HL7Exception;

    /**
     * Returns the next message for a connector.
     *
     * @param connector the connector
     * @return the next message, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    DocumentAct next(Connector connector);

    /**
     * Returns a count of messages for the specified connector and status.
     *
     * @param connector the connector
     * @param status    the message status
     * @return the count of messages matching the connector and status
     */
    int getMessages(Connector connector, String status);

    /**
     * Resubmit a message.
     * <p/>
     * The associated connector must support message resubmission, and the message must have an
     * {@link HL7MessageStatuses#ERROR} status.
     *
     * @param message the message to resubmit
     */
    void resubmit(DocumentAct message);
}