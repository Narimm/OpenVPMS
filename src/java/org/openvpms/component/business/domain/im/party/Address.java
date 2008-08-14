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

// java core

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Address of a contact.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @deprecated will be removed post 1.0
 */
@Deprecated
class Address extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Contacts that reference this address
     */
    private Set<Contact> contacts = new HashSet<Contact>();

    /**
     * A reference to the owning {@link Party}
     */
    private Party party;

    /**
     * Define a protected default constructor
     */
    public Address() {
    }

    /**
     * Return the {@link Contact} that is associated with this address
     *
     * @return Contact
     */
    public Contact[] getContactsAsArray() {
        return contacts.toArray(new Contact[contacts.size()]);
    }

    /**
     * Convenience method that returns the number of {@link Contact) that this
     * address is associated with.
     *
     * @return int
     */
    public int getNumOfContacts() {
        return contacts.size();
    }

    /**
     * @return Returns the contacts.
     */
    public Set<Contact> getContacts() {
        return contacts;
    }

    /**
     * Associated the address with a contact
     *
     * @param contact the contract to associate it with
     */
    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    /**
     * Disassociate the address with the specified contact
     *
     * @param contact the contact to disassociate
     */
    public void removeContact(Contact contact) {
        contacts.remove(contact);
    }

    /**
     * @return Returns the party.
     */
    public Party getParty() {
        return party;
    }

    /**
     * @param party The party to set.
     */
    public void setParty(Party party) {
        this.party = party;
    }

    /**
     * @param contacts The contacts to set.
     */
    protected void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Address copy = (Address) super.clone();
        copy.contacts = new HashSet<Contact>(this.contacts);
        return copy;
    }
}
