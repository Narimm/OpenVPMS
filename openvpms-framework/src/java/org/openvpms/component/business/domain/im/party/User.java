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
import java.util.Calendar;
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.DvInterval;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

/**
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class User extends Role {

    /**
     * Geenrated SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The identity the user uses to login to the system
     */
    private String userId;
    
    /**
     * The user's password
     */
    private String password;
    
    /**
     * The last recorded login date
     * TODO Is this the best place to hold this information
     */
    private Calendar lastLogin;

    
    /**
     * Default constructor 
     */
    public User() {
        // do nothing
    }
    
    /**
     * Constructs an employee.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name 
     * @param description
     *            the description of this entity            
     * @param contacts
     *            the collection of contacts of this role
     * @param activePeriod
     *            the period that this role is valid                        
     * @param userId
     *            the identity of the user
     * @param password
     *            the associated user password
     * @param details
     *            dynamic properties for this role
     */
    public User(String uid, ArchetypeId archetypeId, String name,
            String description, Set<Contact> contacts, String userId, 
            String password, DvInterval<DvDateTime> activePeriod,
            DynamicAttributeMap details) {
        super(uid, archetypeId, name, description, contacts, activePeriod, details);
        this.userId = userId;
        this.password = password;
    }

    /**
     * @return Returns the lastLogin.
     */
    public Calendar getLastLogin() {
        return lastLogin;
    }

    /**
     * @param lastLogin The lastLogin to set.
     */
    public void setLastLogin(Calendar lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the userId.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The userId to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
