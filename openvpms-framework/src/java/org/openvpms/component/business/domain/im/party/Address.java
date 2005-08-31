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

// openehr kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;

// openvpms framework
import org.openvpms.component.business.domain.im.IMObject;

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
    private static final long serialVersionUID = -2619257937834307246L;

    /**
     * The contact that owns this address
     */
    private Set<Contact> contacts;
    
    /**
     * Address details specific, which is specified by the archetype definition
     */
    private ItemStructure details;

    /**
     * Define a protected default constructor
     */
    protected Address() {
    }
    
    /**
     * Construct an address.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archietype that is constraining this object
     * @param imVersion
     *            the version of the reference model
     * @param archetypeNodeId
     *            the id of this node                        
     * @param name
     *            the name 
     * @param details
     *            The details of the addrss object
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    @FullConstructor
    public Address(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "details", required = true) ItemStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
        if (details == null) {
            throw new IllegalArgumentException("null details");
        }
        this.details = details;
        this.contacts = new HashSet<Contact>();
    }

    /**
     * @return Returns the details.
     */
    public ItemStructure getDetails() {
        return details;
    }

    /**
     * @param details
     *            The details to set.
     */
    public void setDetails(ItemStructure details) {
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
