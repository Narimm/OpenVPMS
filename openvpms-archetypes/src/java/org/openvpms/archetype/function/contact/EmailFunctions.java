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

package org.openvpms.archetype.function.contact;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Email contact functions.
 *
 * @author Tim Anderson
 */
public class EmailFunctions extends ContactFunctions {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link EmailFunctions}.
     *
     * @param rules   the party rules
     * @param service the archetype service
     */
    public EmailFunctions(PartyRules rules, IArchetypeService service) {
        super(ContactArchetypes.EMAIL, rules);
        this.service = service;
    }

    /**
     * Formats a contact.
     *
     * @param contact the contact. May be {@code null}
     * @return the formatted contact. May be {@code null}
     */
    @Override
    public String format(Contact contact) {
        String result = null;
        if (contact != null) {
            IMObjectBean bean = new IMObjectBean(contact, service);
            result = bean.getString("emailAddress");
        }
        return result;
    }

}
