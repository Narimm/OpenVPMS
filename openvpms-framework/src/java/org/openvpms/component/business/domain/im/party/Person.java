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

import java.util.Set;

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

/**
 * A person is an {@link Entity} that can participate in {@link Act}s.
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Person extends Actor {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 7833616791459329772L;

    /**
     * The person's title
     */
    private DvText title;
    
    /**
     * The person's first name
     */
    private DvText firstName;
    
    /**
     * The person's last name
     */
    private DvText lastName;
    
    /**
     * The person's initials
     */
    private DvText initials;
    
    /**
     * Constructs a person entity.
     * 
     * @param uid
     *            a unique object identity
     * @param archetypeNodeId
     *            the node id for this archetype
     * @param name
     *            the name of this archetype
     * @param archetypeDetails
     *            descriptive meta data for the achetype
     * @param links
     *            null if not specified
     * @param title            
     *            the person's title
     * @param firstName 
     *            the person's first name
     * @param lastName
     *            the person's last name
     * @param initials
     *            the person's initials                                   
     * @param contacts
     *            a collection of contacts for this actor            
     * @param roles
     *            the collection of roles it belongs too
     * @param details 
     *            actor details
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    @FullConstructor
    public Person(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails,
            @Attribute(name = "title") DvText title,
            @Attribute(name = "firstName") DvText firstName,
            @Attribute(name = "lastName") DvText lastName,
            @Attribute(name = "initials") DvText initials,
            @Attribute(name = "contacts") Set<Contact> contacts,
            @Attribute(name = "roles") Set<Role> roles,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, contacts, roles, details);
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
    }

    /**
     * @return Returns the firstName.
     */
    public DvText getFirstName() {
        return firstName;
    }

    /**
     * @param firstName The firstName to set.
     */
    public void setFirstName(DvText firstName) {
        this.firstName = firstName;
    }

    /**
     * @return Returns the initials.
     */
    public DvText getInitials() {
        return initials;
    }

    /**
     * @param initials The initials to set.
     */
    public void setInitials(DvText initials) {
        this.initials = initials;
    }

    /**
     * @return Returns the lastName.
     */
    public DvText getLastName() {
        return lastName;
    }

    /**
     * @param lastName The lastName to set.
     */
    public void setLastName(DvText lastName) {
        this.lastName = lastName;
    }

    /**
     * @return Returns the title.
     */
    public DvText getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(DvText title) {
        this.title = title;
    }
}

