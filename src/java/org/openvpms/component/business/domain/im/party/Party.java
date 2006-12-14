/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.domain.im.party;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

import java.util.HashSet;
import java.util.Set;


/**
 * The base class of all party types including real world entities and their
 * roles. A party is any entity which can participate in an {@link Act}.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Party extends Entity {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -3369738673105587947L;

    /**
     * The list of contacts for the party
     */
    private Set<Contact> contacts = new HashSet<Contact>();

    /**
     * Default Constructor
     */
    public Party() {
        // do nothing
    }

    /**
     * Construct a Party object.
     *
     * @param archetypeId the archetype id constraining this object
     * @param name        the name of the entity
     * @param description the description of the party
     * @param contacts    a collection of contacts for this actor
     * @param details     party details
     */
    public Party(ArchetypeId archetypeId, String name,
                 String description, Set<Contact> contacts,
                 DynamicAttributeMap details) {
        super(archetypeId, name, description, details);
        this.contacts = (contacts == null) ? new HashSet<Contact>() : contacts;
    }

    /**
     * Returns the contacts.
     *
     * @return the contacts
     */
    public Set<Contact> getContacts() {
        return contacts;
    }

    /**
     * Sets the contacts.
     *
     * @param contacts the contacts to set
     */
    public void setContacts(Set<Contact> contacts) {
        for (Contact contact : contacts) {
            contact.setParty(this);
        }
        this.contacts = contacts;
    }

    /**
     * Add the {@link Contact} to this party
     *
     * @param contact contact to add
     */
    public void addContact(Contact contact) {
        contact.setParty(this);
        contacts.add(contact);
    }

    /**
     * Remove the {@link Contact} from this party.
     *
     * @param contact the contact to remove
     */
    public void removeContact(Contact contact) {
        contact.setParty(null);
        contacts.remove(contact);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.Entity#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Party copy = (Party) super.clone();
        copy.contacts = new HashSet<Contact>(this.contacts);

        return copy;
    }
}
