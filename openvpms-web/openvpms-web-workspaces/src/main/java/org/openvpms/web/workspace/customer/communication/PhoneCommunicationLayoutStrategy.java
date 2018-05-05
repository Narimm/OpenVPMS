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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Layout strategy for <em>act.customerCommunicationPhone</em> acts.
 *
 * @author Tim Anderson
 */
public class PhoneCommunicationLayoutStrategy extends CommunicationLayoutStrategy {

    /**
     * The name node default value.
     */
    private final String defaultValue;

    /**
     * Constructs an {@link PhoneCommunicationLayoutStrategy}.
     */
    public PhoneCommunicationLayoutStrategy() {
        this(null, true);
    }

    /**
     * Constructs an {@link CommunicationLayoutStrategy}.
     *
     * @param message     the message property. May be {@code null}
     * @param showPatient determines if the patient node should be displayed when editing
     */
    public PhoneCommunicationLayoutStrategy(Property message, boolean showPatient) {
        super(message, ContactArchetypes.PHONE, showPatient);
        defaultValue = ContactHelper.getDefaultPhoneName();
    }

    /**
     * Formats a contact.
     * <p/>
     * This version returns the contact description
     *
     * @param contact the contact to format
     * @return the formatted contact
     */
    @Override
    protected String formatContact(Contact contact) {
        String result;
        String name = contact.getName();
        if (name != null && !StringUtils.equals(name, defaultValue)) {
            result = Messages.format("customer.communication.phone", name, contact.getDescription());
        } else {
            result = contact.getDescription();
        }
        return result;
    }
}
