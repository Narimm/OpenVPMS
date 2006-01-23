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


// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;

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
    private static final long serialVersionUID = 1L;

    /**
     * The person's title
     */
    private String title;
    
    /**
     * The person's first name
     */
    private String firstName;
    
    /**
     * The person's last name
     */
    private String lastName;
    
    /**
     * The person's initials
     */
    private String initials;
    
    /**
     * Default constructor
     */
    public Person() {
        // do nothing
    }
    
    /**
     * Constructs a person entity.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param description
     *            the description of this entity            
     * @param title            
     *            the person's title
     * @param firstName 
     *            the person's first name
     * @param lastName
     *            the person's last name
     * @param initials
     *            the person's initials                                   
     */
    public Person(ArchetypeId archetypeId, 
            String description, String title, String firstName,
            String lastName, String initials) {
        super(archetypeId, (firstName + " " + lastName),description);
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
    }

    /**
     * @return Returns the firstName.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName The firstName to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return Returns the initials.
     */
    public String getInitials() {
        return initials;
    }

    /**
     * @param initials The initials to set.
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }

    /**
     * @return Returns the lastName.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName The lastName to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.party.Actor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Person copy = (Person)super.clone();
        copy.firstName = this.firstName;
        copy.initials = this.initials;
        copy.lastName = this.lastName;
        copy.title = this.title;

        return copy;
    }
}

