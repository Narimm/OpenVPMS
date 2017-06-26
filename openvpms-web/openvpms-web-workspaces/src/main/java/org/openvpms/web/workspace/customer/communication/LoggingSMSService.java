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

package org.openvpms.web.workspace.customer.communication;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.SMSException;
import org.openvpms.web.component.service.SMSService;

/**
 * An {@link SMSService} that logs SMS messages for customers, if communication logging is enabled for the practice.
 *
 * @author Tim Anderson
 */
public class LoggingSMSService extends SMSService {

    /**
     * The communication logger.
     */
    private final CommunicationLogger logger;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * Constructs a {@link LoggingSMSService}.
     *
     * @param factory         the factory
     * @param service         the archetype service
     * @param logger          the communication logger
     * @param practiceService the practice service
     */
    public LoggingSMSService(ConnectionFactory factory, IArchetypeService service, CommunicationLogger logger,
                             PracticeService practiceService) {
        super(factory, service);
        this.logger = logger;
        this.practiceService = practiceService;
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
     * @throws SMSException if the send fails
     */
    @Override
    public void send(String phone, String message, Party party, Contact contact, String subject, String reason,
                     Party location) {
        super.send(phone, message, party, contact, subject, reason, location);
        if (log(party)) {
            logger.logSMS(party, null, phone, subject, reason, message, null, location);
        }
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
    @Override
    public void send(String phone, String message, Party customer, Party patient, Contact contact, String subject,
                     String reason, Party location) {
        super.send(phone, message, customer, patient, contact, subject, reason, location);
        if (log(customer)) {
            logger.logSMS(customer, patient, phone, subject, reason, message, null, location);
        }
    }

    /**
     * Determines if logging should take place.
     *
     * @param party the customer
     * @return {@code true} if logging should take place
     */
    protected boolean log(Party party) {
        return TypeHelper.isA(party, CustomerArchetypes.PERSON)
               && CommunicationHelper.isLoggingEnabled(practiceService, getService());
    }

}
