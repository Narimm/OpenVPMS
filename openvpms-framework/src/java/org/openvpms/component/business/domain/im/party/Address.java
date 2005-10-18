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
import java.util.HashSet;
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Address of a contact.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Address extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The contact that owns this address
     */
    private Set<Contact> contacts = new HashSet<Contact>();
    
    /**
     * Address details specific, which is specified by the archetype definition
     */
    private DynamicAttributeMap details;

    /**
     * Define a protected default constructor
     */
    public Address() {
    }
    
    /**
     * Construct an address.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param details
     *            The details of the address object
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    public Address(ArchetypeId archetypeId,
            DynamicAttributeMap details) {
        super(archetypeId);
        this.details = details;
        this.contacts = new HashSet<Contact>();
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details
     *            The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }
    
    /**
     * Return the {@link Contact} that is associated with this address
     * 
     * @return Contact
     */
    public Contact[] getContactsAsArray() {
        return (Contact[])contacts.toArray(new Contact[contacts.size()]);
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
     * @param contacts The contacts to set.
     */
    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Associated the address with a contact
     * 
     * @param contact 
     *            the contract to associate it with
     */
    public void addContact(Contact contact) {
        contacts.add(contact);
    }
    
    /**
     * Disassociate the address with the specified contact
     * 
     * @parm contact
     *            the contact to disassociate
     */
    public void removeContact(Contact contact) {
        contacts.remove(contact);
    }
}
