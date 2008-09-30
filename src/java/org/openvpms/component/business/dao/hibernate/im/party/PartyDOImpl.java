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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.dao.hibernate.im.entity.EntityDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link PartyDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-05-22 15:14:34 +1000 (Thu, 22 May 2008) $
 */
public class PartyDOImpl extends EntityDOImpl implements PartyDO {

    /**
     * The set of contacts for the party.
     */
    private Set<ContactDO> contacts = new HashSet<ContactDO>();


    /**
     * Default constructor.
     */
    public PartyDOImpl() {
        // do nothing
    }

    /**
     * Creates a new <tt>PartyDOImpl</tt>.
     *
     * @param archetypeId the archetype id
     */
    public PartyDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the contacts.
     *
     * @return the contacts
     */
    public Set<ContactDO> getContacts() {
        return contacts;
    }

    /**
     * Adds a contact.
     *
     * @param contact contact to add
     */
    public void addContact(ContactDO contact) {
        contact.setParty(this);
        contacts.add(contact);
    }

    /**
     * Removes a contact.
     *
     * @param contact the contact to remove
     */
    public void removeContact(ContactDO contact) {
        contact.setParty(null);
        contacts.remove(contact);
    }

    /**
     * Sets the contacts.
     *
     * @param contacts the contacts to set
     */
    protected void setContacts(Set<ContactDO> contacts) {
        this.contacts = contacts;
    }

}
