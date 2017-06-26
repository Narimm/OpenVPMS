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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.SMSException;
import org.openvpms.web.component.im.sms.SMSHelper;

/**
 * SMS service.
 *
 * @author Tim Anderson
 */
public class SMSService {

    /**
     * The SMS connection factory.
     */
    private final ConnectionFactory factory;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SMSService.class);

    /**
     * Constructs a {@link SMSService}.
     *
     * @param factory the factory
     * @param service the archetype service
     */
    public SMSService(ConnectionFactory factory, IArchetypeService service) {
        this.factory = factory;
        this.service = service;
    }

    /**
     * Returns the maximum number of message parts supported by the SMS provider.
     *
     * @return the maximum number of message parts
     */
    public int getMaxParts() {
        return factory.getMaxParts();
    }

    /**
     * Sends an SMS.
     *
     * @param message  the SMS text
     * @param contact  the phone contact
     * @param party    the party associated with the phone number. May be {@code null}
     * @param subject  the subject of the SMS, for communication logging purposes
     * @param reason   the reason of the SMS, for communication logging purposes
     * @param location the practice location. May be {@code null}
     * @throws IllegalArgumentException if the phone contact is incomplete
     * @throws SMSException             if the send fails
     */
    public void send(String message, Contact contact, Party party, String subject, String reason, Party location) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        String telephoneNumber = bean.getString("telephoneNumber");
        if (StringUtils.isEmpty(telephoneNumber)) {
            throw new IllegalArgumentException("Argument 'contact' doesn't have a telephoneNumber");
        }
        String phone = SMSHelper.getPhone(contact);
        send(phone, message, party, contact, subject, reason, location);
    }

    /**
     * Sends an SMS.
     *
     * @param phone    the phone number to send the SMS to
     * @param message  the SMS text
     * @param party    the party associated with the phone number. May be {@code null}
     * @param contact  the phone contact. May be {@code null}
     * @param subject  the subject of the SMS, for communication logging purposes
     * @param reason   the reason of the SMS, for communication logging purposes
     * @param location the practice location. May be {@code null}
     */
    public void send(String phone, String message, Party party, Contact contact, String subject, String reason,
                     Party location) {
        if (log.isDebugEnabled()) {
            String p = (party != null) ? party.getName() + " (" + party.getId() + ")" : null;
            String c = (contact != null) ? contact.getDescription() + " (" + contact.getId() + ")" : null;
            String l = (location != null) ? location.getName() + " (" + location.getId() + ")" : null;
            log.debug("SMS: phone=" + phone + ", message='" + message + "', party=" + p + ", contact=" + c
                      + ", subject=" + subject + ", reason=" + reason + ", location=" + l);
        }
        send(phone, message);
    }

    /**
     * Sends an SMS to a customer.
     *
     * @param phone    the phone number to send the SMS to
     * @param message  the SMS text
     * @param customer the customer associated with the phone number
     * @param patient  the patient the SMS refers to. May be {@code null}
     * @param contact  the phone contact. May be {@code null}
     * @param subject  the subject of the SMS, for communication logging purposes
     * @param reason   the reason of the SMS, for communication logging purposes
     * @param location the practice location. May be {@code null}
     * @throws SMSException if the send fails
     */
    public void send(String phone, String message, Party customer, Party patient, Contact contact, String subject,
                     String reason, Party location) {
        if (log.isDebugEnabled()) {
            String p = (patient != null) ? patient.getName() + " (" + patient.getId() + ")" : null;
            String c = (contact != null) ? contact.getDescription() + " (" + contact.getId() + ")" : null;
            String l = (location != null) ? location.getName() + " (" + location.getId() + ")" : null;
            log.debug("SMS: phone=" + phone + ", message='" + message + "', customer=" + customer.getName()
                      + "(" + customer.getId() + ")" + p + ", contact=" + c + ", subject=" + subject
                      + ", reason=" + reason + ", location=" + l);
        }
        send(phone, message);
    }

    /**
     * Sends an SMS.
     *
     * @param phone   the phone number
     * @param message the SMS text
     * @throws SMSException if the send fails
     */
    protected void send(String phone, String message) {
        Connection connection = factory.createConnection();
        try {
            connection.send(phone, message);
        } finally {
            connection.close();
        }
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }
}
