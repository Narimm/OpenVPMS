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
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Contact;

import java.util.Arrays;
import java.util.List;

/**
 * An {@link ContactMatcher} that matches contacts on archetype and purpose.
 *
 * @author Tim Anderson
 */
public class PurposeMatcher extends ContactMatcher {

    /**
     * The purpose to match on.
     */
    private final List<String> purposes;

    /**
     * A purpose to exclude.
     */
    private String exclusion;

    /**
     * If {@code true} the contact must contain the purpose to be returned
     */
    private final boolean exact;

    /**
     * Constructs a {@link PurposeMatcher} where the contact must have the specified purpose to be considered a match.
     *
     * @param shortName the contact archetype short names
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
        this(new String[]{shortName}, purpose, exact, service);
    }

    /**
     * Constructs a {@link PurposeMatcher}.
     *
     * @param shortNames the contact archetype short names
     * @param purpose   the purpose. May be {@code null}
     * @param exact     if {@code true} the contact must contain the purpose in order to be considered a match
     * @param service   the archetype service
     */
    public PurposeMatcher(String[] shortNames, String purpose, boolean exact, IArchetypeService service) {
        this(shortNames, exact, service, purpose != null ? new String[]{purpose} : new String[0]);
    }

    /**
     * Constructs a {@link PurposeMatcher}.
     *
     * @param shortName the contact archetype short name
     * @param exact     if {@code true} the contact must contain the purpose in order to be considered a match
     * @param service   the archetype service
     * @param purposes  the purposes to match on. May be empty
     */
    public PurposeMatcher(String shortName, boolean exact, IArchetypeService service, String... purposes) {
        this(new String[]{shortName}, exact, service, purposes);
    }

    /**
     * Constructs a {@link PurposeMatcher}.
     *
     * @param shortNames the contact archetype short names
     * @param exact      if {@code true} the contact must contain the purpose in order to be considered a match
     * @param service    the archetype service
     * @param purposes   the purposes to match on. May be empty
     */
    public PurposeMatcher(String[] shortNames, boolean exact, IArchetypeService service, String... purposes) {
        super(shortNames, service);
        this.exact = exact;
        this.purposes = Arrays.asList(purposes);
    }

    /**
     * Sets a contact purpose to exclude.
     * <p/>
     * Any contact with a purpose matching that supplied won't be considered a match.
     *
     * @param exclusion the purpose to exclude. May be {@code null}
     */
    public void setExclusion(String exclusion) {
        this.exclusion = exclusion;
    }

    /**
     * Determines if a contact matches the criteria.
     *
     * @param contact the contact
     * @return {@code true} if the contact is an exact match; otherwise {@code false}
     */
    @Override
    public boolean matches(Contact contact) {
        return super.matches(contact) && !excluded(contact) && matchesPurpose(contact);
    }

    /**
     * Determines if a contact is excluded.
     *
     * @param contact the contact
     * @return {@code true} if the contact is excluded otherwise {@code false}
     */
    protected boolean excluded(Contact contact) {
        return exclusion != null && hasContactPurpose(contact, exclusion);
    }

    /**
     * Determines if a contact matches the criteria.
     *
     * @param contact the contact
     * @return {@code true} if the contact is an exact match; otherwise {@code false}
     */
    protected boolean matchesPurpose(Contact contact) {
        boolean best = false;
        boolean preferred = isPreferred(contact);
        if (!purposes.isEmpty()) {
            int priority = purposes.size();
            if (exact) {
                int i = 0;
                for (String purpose : purposes) {
                    if (hasContactPurpose(contact, purpose)) {
                        i++;
                    }
                }
                if (i == priority && preferred) { // has matched all purposes and is preferred
                    setMatch(0, contact);
                    best = true;
                } else if (i == priority) { // has matched all purposes and is not preferred
                    setMatch(1, contact);
                }
            } else {
                int i = 0;
                for (String purpose : purposes) {
                    if (hasContactPurpose(contact, purpose)) {
                        i++;
                    }
                }
                if (i == priority && preferred) { //has matched all purposes and is preferred
                    setMatch(0, contact);
                    best = true;
                } else if (i == priority) { // has matched all purposes and is not preferred
                    setMatch(1, contact);
                } else if (i > 0 && !preferred) { // has matched some purposes and is not preferred.
                    setMatch(priority - i + 2, contact);
                } else if (i > 0 && preferred) { // has matched some purposes and is not preferred.
                    setMatch(priority - i + 1, contact);
                } else if (i == 0 && !preferred) {
                    setMatch(priority + 2, contact); //matched no purposes and is not preferred
                } else if (i == 0) {
                    setMatch(priority + 1, contact); //matched no purposes and is preferred
                }
            }
        } else {
            if (preferred) {
                setMatch(1, contact);
                best = true;
            } else {
                setMatch(2, contact);
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
