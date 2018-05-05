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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.party;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.party.Contact;

/**
 * Matches phone contacts if they have SMS enabled.
 *
 * @author Tim Anderson
 */
public class SMSMatcher extends PurposeMatcher {

    /**
     * Constructs an {@link SMSMatcher}.
     *
     * @param service the archetype service
     */
    public SMSMatcher(IArchetypeService service) {
        this(null, false, service);
    }

    /**
     * Constructs an {@link SMSMatcher}.
     *
     * @param purpose the contact purpose. May be {@code null}
     * @param exact   if {@code true} the contact must contain the purpose in order to be considered a match
     * @param service the archetype service
     */
    public SMSMatcher(String purpose, boolean exact, IArchetypeService service) {
        super(ContactArchetypes.PHONE, purpose, exact, service);
    }

    /**
     * Determines if a contact matches the criteria.
     *
     * @param contact the contact
     * @return {@code true} if the contact is an exact match; otherwise {@code false}
     */
    @Override
    public boolean matches(Contact contact) {
        boolean result = isA(contact);
        if (result) {
            IMObjectBean bean = createBean(contact);
            result = bean.getBoolean("sms") && matchesPurpose(contact);
        }
        return result;
    }
}
