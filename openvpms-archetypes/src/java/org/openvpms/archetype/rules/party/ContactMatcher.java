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
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Helper to find a contact matching some criteria.
 *
 * @author Tim Anderson
 */
public abstract class ContactMatcher {

    /**
     * The contact archetype short name.
     */
    private final String shortName;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The contacts matching some or all of the criteria, keyed on
     * priority, where the 0 is the highest priority.
     */
    private SortedMap<Integer, Contact> contacts = new TreeMap<Integer, Contact>();

    /**
     * Constructs a new {@code ContactMatcher}.
     *
     * @param shortName the contact archetype short name
     */
    public ContactMatcher(String shortName, IArchetypeService service) {
        this.shortName = shortName;
        this.service = service;
    }

    /**
     * Determines if a contact matches the criteria.
     *
     * @param contact the contact
     * @return {@code true} if the contact is an exact match; otherwise {@code false}
     */
    public boolean matches(Contact contact) {
        return TypeHelper.isA(contact, shortName);
    }

    /**
     * Returns the contact that best matches the criteria.
     *
     * @return the contact that best matches the criteria, or {@code null}
     */
    public Contact getMatch() {
        Integer best = null;
        if (!contacts.isEmpty()) {
            best = contacts.firstKey();
        }
        return (best != null) ? contacts.get(best) : null;
    }

    /**
     * Registers a contact that matches some/all of the criteria, if none with the same priority is present.
     *
     * @param priority the priority, where {@code 0} is the highest priority.
     * @param contact  the contact
     */
    protected void setMatch(int priority, Contact contact) {
        if (contacts.get(priority) == null) {
            contacts.put(priority, contact);
        }
    }

    /**
     * Determines if a contact has a preferred node with value 'true'.
     *
     * @param contact the contact
     * @return {@code true} if the contact is preferred
     */
    protected boolean isPreferred(Contact contact) {
        return isPreferred(createBean(contact));
    }

    /**
     * Determines if a contact has a preferred node with value 'true'.
     *
     * @param bean the contact
     * @return {@code true} if the contact is preferred
     */
    protected boolean isPreferred(IMObjectBean bean) {
        return bean.hasNode("preferred") && bean.getBoolean("preferred");
    }

    /**
     * Creates a bean for a contact.
     *
     * @param contact the contact
     * @return a new bean
     */
    protected IMObjectBean createBean(Contact contact) {
        return new IMObjectBean(contact, service);
    }
}
