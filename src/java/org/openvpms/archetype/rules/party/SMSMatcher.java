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

package org.openvpms.archetype.rules.party;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Matches phone contacts if they have SMS enabled.
 *
 * @author Tim Anderson
 */
public class SMSMatcher extends ContactMatcher {

    /**
     * Constructs an {@link SMSMatcher}.
     *
     * @param service the archetype service
     */
    public SMSMatcher(IArchetypeService service) {
        super(ContactArchetypes.PHONE, service);
    }

    /**
     * Determines if a contact matches the criteria.
     *
     * @param contact the contact
     * @return {@code true} if the contact is an exact match; otherwise {@code false}
     */
    @Override
    public boolean matches(Contact contact) {
        boolean result = super.matches(contact);
        if (result) {
            IMObjectBean bean = createBean(contact);
            if (bean.getBoolean("sms")) {
                if (isPreferred(bean)) {
                    result = true;
                    setMatch(0, contact);
                } else {
                    result = false;
                    setMatch(1, contact);
                }
            } else {
                result = false;
            }
        }
        return result;
    }
}
