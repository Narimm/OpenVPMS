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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * The base class of all party types including real world entities and their
 * roles. A party is any entity which can participate in an {@link Act}. The
 * meaning attribute inherited from {@link Locatable} is used to indicate the
 * actual type of party.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class Party extends Entity {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -3369738673105587947L;

    /**
     * The list of contacts for the party
     */
    private Set<Contact> contacts;

    /**
     * Default Constructor
     */
    protected Party() {
        // do nothing
    }
    
    /**
     * Construct a Party object.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name of the entity            
     * @param description
     *            the description of the party            
     * @param contacts
     *            a collection of contacts for this actor            
     * @param details 
     *            party details
     */
    protected Party(ArchetypeId archetypeId, String name,  
            String description, Set<Contact> contacts, 
            DynamicAttributeMap details) {
        super(archetypeId, name, description, details);
        this.contacts = (contacts == null) ? new HashSet<Contact>() : contacts;
    }

    /**
     * @return Returns the contacts.
     */
    public Set<Contact> getContacts() {
        return contacts;
    }

    /**
     * @param contacts
     *            The contacts to set.
     */
    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }
    
    /**
     * Convenience method to add a {@link Contact}
     * 
     * @param contact
     *            contact to add
     */
    public void addContact(Contact contact) {
        this.contacts.add(contact);
    }
    
    /**
     * Convenience method for removing a {@link Contact}
     */
    public void removeContact(Contact contact) {
        this.contacts.remove(contact);
    }
}
