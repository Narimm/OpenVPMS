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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.communication;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.TextDocumentHandler;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.doc.HtmlToTextFormatter;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

/**
 * Logs customer communication.
 *
 * @author Tim Anderson
 */
public class CommunicationLogger {

    /**
     * The archetype service
     */
    private final IArchetypeService service;

    /**
     * The user rules
     */
    private final UserRules rules;

    /**
     * The text document handler.
     */
    private final TextDocumentHandler handler;

    /**
     * Constructs a {@link CommunicationLogger}.
     *
     * @param service the archetype service
     * @param rules   the user rules
     */
    public CommunicationLogger(IArchetypeService service, UserRules rules) {
        this.service = service;
        this.rules = rules;
        handler = new TextDocumentHandler(service);
    }

    /**
     * Logs an email to a customer.
     *
     * @param customer    the customer
     * @param patient     the patient. May be {@code null}
     * @param from        the from address
     * @param to          the to addresses. May be {@code null}
     * @param cc          the CC addresses. May be {@code null}
     * @param bcc         the BCC addresses. May be {@code null}
     * @param subject     the subject. May be {@code null}
     * @param reason      the reason for the communication  (see <em>act.customerCommunication/reason</em>)
     * @param message     the message (in HTML). May be {@code null}
     * @param note        optional note. May be {@code null}
     * @param attachments the attachments. May be {@code null}
     * @param location    the practice location where the communication took place. May be {@code null}
     */
    public void logEmail(Party customer, Party patient, String from, String[] to, String[] cc, String[] bcc,
                         String subject, String reason, String message, String note, String attachments,
                         Party location) {
        if (message != null) {
            HtmlToTextFormatter formatter = new HtmlToTextFormatter();
            message = formatter.format(message);
        }
        ActBean bean = createLog(CommunicationArchetypes.EMAIL, customer, patient, getAddresses(to), subject, reason,
                                 note, location);
        setValue(bean, "from", from);
        bean.setValue("cc", getAddresses(cc));
        bean.setValue("bcc", getAddresses(bcc));
        setValue(bean, "attachments", attachments);
        saveLog(message, bean, "email.txt");
    }

    /**
     * Logs an SMS to a customer.
     *
     * @param customer the customer
     * @param patient  the patient. May be {@code null}
     * @param address  the address
     * @param subject  the subject. May be {@code null}
     * @param reason   the reason for the communication  (see <em>act.customerCommunication/reason</em>)
     * @param message  the message
     * @param note     optional note. May be {@code null}
     * @param location the practice location where the communication took place. May be {@code null}
     */
    public void logSMS(Party customer, Party patient, String address, String subject, String reason, String message,
                       String note, Party location) {
        ActBean bean = createLog(CommunicationArchetypes.SMS, customer, patient, address, subject, reason,
                                 note, location);
        setValue(bean, "message", message);
        bean.save();
    }

    /**
     * Logs a mail to a customer.
     *
     * @param customer the customer
     * @param patient  the patient. May be {@code null}
     * @param address  the address
     * @param subject  the subject. May be {@code null}
     * @param reason   the reason for the communication  (see <em>act.customerCommunication/reason</em>)
     * @param message  the message
     * @param note     optional note. May be {@code null}
     * @param location the practice location where the communication took place. May be {@code null}
     */
    public void logMail(Party customer, Party patient, String address, String subject, String reason, String message,
                        String note, Party location) {
        ActBean bean = createLog(CommunicationArchetypes.MAIL, customer, patient, address, subject, reason,
                                 note, location);
        saveLog(message, bean, "mail.txt");
    }

    /**
     * Logs a phone call to a customer.
     *
     * @param customer    the customer
     * @param patient     the patient. May be {@code null}
     * @param phoneNumber the phone number
     * @param subject     the subject. May be {@code null}
     * @param reason      the reason for the communication  (see <em>act.customerCommunication/reason</em>)
     * @param message     the message
     * @param note        optional note. May be {@code null}
     * @param location    the practice location where the communication took place. May be {@code null}
     */
    public void logPhone(Party customer, Party patient, String phoneNumber, String subject, String reason,
                         String message, String note, Party location) {
        ActBean bean = createLog(CommunicationArchetypes.PHONE, customer, patient, phoneNumber, subject, reason,
                                 note, location);
        saveLog(message, bean, "phone.txt");
    }

    /**
     * Saves a communication log.
     *
     * @param message  the message. If this exceeds the maximum allowed characters of the node, it will be stored as a
     *                 document instead
     * @param bean     the bean wrapping the act
     * @param fileName the document filename
     */
    protected void saveLog(String message, ActBean bean, String fileName) {
        int length = getLength(bean, "message");
        if (message != null && message.length() > length) {
            Document document = handler.create(fileName, message);
            bean.setValue("document", document.getObjectReference());
            if (bean.hasNode("mimeType")) {
                bean.setValue("mimeType", document.getMimeType());
            }
            if (bean.hasNode("fileName")) {
                bean.setValue("fileName", document.getName());
            }
            service.save(Arrays.asList(bean.getAct(), document));
        } else {
            bean.setValue("message", message);
            bean.save();
        }
    }

    /**
     * Creates a new <em>act.customerCommunication*</em> act.
     *
     * @param shortName the act short name to create
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param address   the address
     * @param subject   the subject
     * @param reason    the reason
     * @param note      the note. May be {@code null}
     * @param location  the location. May be {@code null}
     * @return the act, wrapped in a bean
     */
    protected ActBean createLog(String shortName, Party customer, Party patient, String address, String subject,
                                String reason, String note, Party location) {
        User author = rules.getUser(SecurityContextHolder.getContext().getAuthentication());
        return createLog(shortName, customer, patient, author, address, subject, reason, note, location);
    }

    /**
     * Creates a new <em>act.customerCommunication*</em> act.
     *
     * @param shortName the act short name to create
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param author    the author. May be {@code null}
     * @param address   the address
     * @param subject   the subject
     * @param reason    the reason
     * @param note      the note. May be {@code null}
     * @param location  the location. May be {@code null}
     * @return the act, wrapped in a bean
     */
    protected ActBean createLog(String shortName, Party customer, Party patient, User author, String address,
                                String subject, String reason, String note, Party location) {
        Act act = (Act) IMObjectCreator.create(shortName);
        ActBean bean = new ActBean(act, service);
        bean.setNodeParticipant("customer", customer);
        bean.setNodeParticipant("patient", patient);
        bean.setNodeParticipant("author", author);
        bean.setNodeParticipant("location", location);
        setValue(bean, "address", address);
        setValue(bean, "description", subject);
        bean.setValue("reason", reason);
        setValue(bean, "note", note);
        return bean;
    }

    /**
     * Returns a concatenated list of addresses, separated by new lines.
     *
     * @param addresses the addresses. May be {@code null}
     * @return the concatenated addresses. May be {@code null}
     */
    protected String getAddresses(String[] addresses) {
        String result = null;
        if (addresses != null) {
            result = StringUtils.join(addresses, "\n");
            result = StringUtils.abbreviate(result, 5000);
        }
        return result;
    }

    /**
     * Helper to populate a node, truncating values if they exceed the node length.
     *
     * @param bean  the bean
     * @param name  the node name
     * @param value the value
     */
    protected void setValue(ActBean bean, String name, String value) {
        if (!StringUtils.isEmpty(value)) {
            bean.setValue(name, StringUtils.abbreviate(value, getLength(bean, name)));
        }
    }

    /**
     * Returns the length of a node.
     *
     * @param bean the act
     * @param name the node name
     * @return the length of the node
     */
    private int getLength(ActBean bean, String name) {
        int length = bean.getDescriptor(name).getMaxLength();
        if (length == -1) {
            length = NodeDescriptor.DEFAULT_MAX_LENGTH;
        }
        return length;
    }

}
