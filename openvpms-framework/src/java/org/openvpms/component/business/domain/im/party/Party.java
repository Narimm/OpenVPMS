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
import java.util.Set;

// openehr-java-kernel
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;
import org.openvpms.component.business.domain.im.Entity;

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
     * @param uid
     *            uniquely identifies this instance
     * @param archetypeNodeId
     *            the node within the archetype definition this refers too
     * @param name
     *            the name
     * @param archetypeDetails
     *            a reference to the achetype definition
     * @param contacts
     *            a list of contacts
     * @param details
     *            details of this party            
     */
    protected Party(
            ObjectID uid, 
            String archetypeNodeId, 
            DvText name,
            Archetyped archetypeDetails, 
            Set<Contact> contacts,
            ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, details);
        this.contacts = contacts;
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
