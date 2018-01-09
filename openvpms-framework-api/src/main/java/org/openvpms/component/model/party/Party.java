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

package org.openvpms.component.model.party;

import org.openvpms.component.model.entity.Entity;

import java.util.Set;

/**
 * The base class of all party types including real world entities.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Party extends Entity {

    /**
     * Returns the contacts.
     *
     * @return the contacts
     */
    Set<Contact> getContacts();

    /**
     * Add the {@link Contact} to this party
     *
     * @param contact contact to add
     */
    void addContact(Contact contact);

    /**
     * Remove the {@link Contact} from this party.
     *
     * @param contact the contact to remove
     */
    void removeContact(Contact contact);

}
