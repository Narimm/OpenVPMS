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

package org.openvpms.web.component.im.sms;

import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.system.ServiceHelper;

/**
 * SMS helper methods.
 *
 * @author Tim Anderson
 */
public class SMSHelper {

    /**
     * Determines if SMS is configured for the practice.
     *
     * @param practice the practice. May be {@code null}
     * @return {@code true} if SMS is configured, otherwise {@code false}
     */
    public static boolean isSMSEnabled(Party practice) {
        return practice != null && ServiceHelper.getBean(PracticeRules.class).isSMSEnabled(practice);
    }

    /**
     * Determines if a customer can receive SMS messages.
     *
     * @param customer the customer. May be {@code null}
     * @return {@code true} if the customer can receive SMS messages
     */
    public static boolean canSMS(Party customer) {
        return customer != null && new Contacts(ServiceHelper.getArchetypeService()).canSMS(customer);
    }

    /**
     * Returns the phone number from a contact, extracting any formatting.
     *
     * @param contact the phone contact. May be {@code null}
     * @return the phone number. May be {@code null}
     */
    public static String getPhone(Contact contact) {
        return contact != null ? new Contacts(ServiceHelper.getArchetypeService()).getPhone(contact) : null;
    }

    /**
     * Returns the appointment reminder SMS template for a practice location, falling back to the one configured
     * for the practice if none is defined.
     *
     * @param location the practice location
     * @return the appointment reminder SMS template, or {@code null} if none is defined for the location or practice
     */
    public static Entity getAppointmentTemplate(Party location) {
        LocationRules rules = ServiceHelper.getBean(LocationRules.class);
        Entity template = rules.getAppointmentSMSTemplate(location);
        if (template == null) {
            PracticeService service = ServiceHelper.getBean(PracticeService.class);
            template = service.getAppointmentSMSTemplate();
        }
        return template;
    }
}
