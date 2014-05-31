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

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * An {@link ContactMatcher} that matches contacts on archetype and purpose.
 *
 * @author Tim Anderson
 */
public class PurposeMatcher extends ContactMatcher {

    /**
     * The purpose to match on.
     */
    private final String purpose;

    /**
     * If {@code true} the contact must contain the purpose to be returned
     */
    private final boolean exact;

    /**
     * Constructs a {@link PurposeMatcher} where the contact must have the specified purpose to be considered a match.
     *
     * @param shortName the contact archetype short name
     * @param purpose   the purpose
     * @param service   the archetype service
     */
    public PurposeMatcher(String shortName, String purpose, IArchetypeService service) {
        this(shortName, purpose, true, service);
    }

    /**
     * Constructs a {@link PurposeMatcher}.
     *
     * @param shortName the contact archetype short name
     * @param purpose   the purpose. May be {@code null}
     * @param exact     if {@code true} the contact must contain the purpose in order to be considered a match
     * @param service   the archetype service
     */
    public PurposeMatcher(String shortName, String purpose, boolean exact, IArchetypeService service) {
        super(shortName, service);
        this.purpose = purpose;
        this.exact = exact;
    }

    /**
     * Determines if a contact matches the criteria.
     *
     * @param contact the contact
     * @return {@code true} if the contact is an exact match; otherwise {@code false}
     */
    @Override
    public boolean matches(Contact contact) {
        boolean best = false;
        if (super.matches(contact)) {
            boolean preferred = isPreferred(contact);
            if (purpose != null) {
                if (hasContactPurpose(contact, purpose)) {
                    if (preferred) {
                        setMatch(0, contact);
                        best = true;
                    } else {
                        setMatch(1, contact);
                    }
                } else if (!exact) {
                    if (preferred) {
                        setMatch(2, contact);
                    } else {
                        setMatch(3, contact);
                    }
                }
            } else {
                if (preferred) {
                    setMatch(0, contact);
                    best = true;
                } else {
                    setMatch(1, contact);
                }
            }
        }
        return best;
    }

    /**
     * Determines if a contact has a particular purpose.
     *
     * @param contact the contact
     * @param purpose the contact purpose
     * @return {@code true} if the contact has the specified purpose,
     *         otherwise {@code false}
     */
    private boolean hasContactPurpose(Contact contact, String purpose) {
        for (Lookup classification : contact.getClassifications()) {
            if (classification.getCode().equals(purpose)) {
                return true;
            }
        }
        return false;
    }
}
