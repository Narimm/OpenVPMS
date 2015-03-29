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

import ca.uhn.hl7v2.ErrorCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.parser.Parser;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.hl7.io.Connector;
import org.openvpms.hl7.io.HL7DocumentHandler;
import org.openvpms.hl7.io.MessageService;
import org.openvpms.hl7.util.HL7Archetypes;
import org.openvpms.hl7.util.HL7MessageStatuses;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of the {@link MessageService}.
 *
 * @author Tim Anderson
 */
public class MessageServiceImpl implements MessageService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The handler for <em>document.hl7</em> acts.
     */
    private final HL7DocumentHandler handler;

    /**
     * The hl7 mime type.
     */
    private static final String MIME_TYPE = "application/hl7-v2+er7"; // for now, will be using the PipeParser

    /**
     * Length restriction for error messages.
     */
    private static final int MAX_ERROR_LENGTH = 5000;

    /**
     * Constructs a {@link MessageServiceImpl}.
     *
     * @param service the archetype service
     */
    public MessageServiceImpl(IArchetypeService service) {
        this.service = service;
        handler = new HL7DocumentHandler(service);
    }

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
    @Override
    public DocumentAct save(Message message, Connector connector, User user) throws HL7Exception {
        DocumentAct act = (DocumentAct) service.create(HL7Archetypes.MESSAGE);
        ActBean bean = new ActBean(act, service);
        bean.addNodeParticipation("connector", connector.getReference());
        bean.addNodeParticipation("author", user);
        bean.setValue("hl7Version", message.getVersion());
        MSH header = (MSH) message.get("MSH");
        String name = HL7MessageHelper.getMessageName(header);
        act.setName(name);
        TS dateTimeOfMessage = header.getDateTimeOfMessage();
        act.setActivityStartTime(dateTimeOfMessage.getTime().getValueAsDate());
        name = name + "_" + header.getMessageControlID().getValue() + ".hl7";
        String encoded = message.encode();
        Document document = handler.create(name, encoded, MIME_TYPE);
        act.setDocument(document.getObjectReference());
        List<IMObject> toSave = Arrays.asList(document, act);
        service.save(toSave);
        return act;
    }

    /**
     * Updates a persistent message to indicate it has been accepted.
     *
     * @param message   the message
     * @param timestamp the accepted timestamp
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public void accepted(DocumentAct message, Date timestamp) {
        message.setActivityEndTime(timestamp);
        message.setStatus(HL7MessageStatuses.ACCEPTED);
        ActBean bean = new ActBean(message, service);
        bean.setValue("error", null);
        bean.save();
    }

    /**
     * Updates a persistent message to indicate it is in error.
     *
     * @param message   message
     * @param status    the new status
     * @param timestamp the error timestamp
     * @param error     the error message
     */
    @Override
    public void error(DocumentAct message, String status, Date timestamp, String error) {
        if (error != null && error.length() > MAX_ERROR_LENGTH) {
            error = error.substring(0, MAX_ERROR_LENGTH);
        }
        message.setActivityEndTime(timestamp);
        message.setStatus(status);
        ActBean bean = new ActBean(message, service);
        bean.setValue("error", error);
        bean.save();
    }

    /**
     * Decodes an HL7 message from a persistent message.
     *
     * @param message the persistent message
     * @param parser  the parser to use
     * @return the decoded message
     * @throws HL7Exception              for any HL7 error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Message get(DocumentAct message, Parser parser) throws HL7Exception {
        IMObjectReference ref = message.getDocument();
        Document document = (ref != null) ? (Document) service.get(ref) : null;
        if (document == null) {
            throw new HL7Exception("No message content", ErrorCode.APPLICATION_INTERNAL_ERROR);
        }
        String content = handler.getStringContent(document);
        return parser.parse(content);
    }

    /**
     * Returns the next message for a connector.
     *
     * @param connector the connector
     * @return the next message, or {@code null} if none is found
     */
    @Override
    public DocumentAct next(Connector connector) {
        ArchetypeQuery query = createQuery(connector, HL7MessageStatuses.PENDING);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<DocumentAct>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Resubmit a message.
     * <p/>
     * The associated connector must support message resubmission, and the message must have an
     * {@link HL7MessageStatuses#ERROR} status.
     *
     * @param message the message to resubmit
     */
    @Override
    public void resubmit(DocumentAct message) {
        if (!HL7MessageStatuses.ERROR.equals(message.getStatus())) {
            throw new IllegalArgumentException("Cannot resubmit messages with status " + message.getStatus());
        }
        ActBean bean = new ActBean(message, service);
        IMObjectReference ref = bean.getNodeParticipantRef("connector");
        if (!TypeHelper.isA(ref, HL7Archetypes.SENDERS)) {
            throw new IllegalArgumentException("Cannot resubmit messages using " + ref.getArchetypeId());
        }
        bean.setStatus(HL7MessageStatuses.PENDING);
        bean.save();
    }

    /**
     * Returns a count of messages for the specified connector and status.
     *
     * @param connector the connector
     * @param status    the message status
     * @return the count of messages matching the connector and status
     */
    @Override
    public int getMessages(Connector connector, String status) {
        ArchetypeQuery query = createQuery(connector, status);
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        return page.getTotalResults();
    }

    /**
     * Creates a query for PENDING messages.
     *
     * @param connector the connector
     * @param status    the message status
     * @return a new query
     */
    private ArchetypeQuery createQuery(Connector connector, String status) {
        ArchetypeQuery query = new ArchetypeQuery(HL7Archetypes.MESSAGE);
        query.add(Constraints.join("connector").add(Constraints.eq("entity", connector.getReference())));
        query.add(Constraints.eq("status", status));
        return query;
    }

}
