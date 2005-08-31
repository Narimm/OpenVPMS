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
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvText;

/**
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class User extends Role {

    /**
     * Geenrated SUID
     */
    private static final long serialVersionUID = 7635011589554016052L;
    
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
    private DvDate lastLogin;

    /**
     * Constructs an employee.
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
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    @FullConstructor
    public User(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "contacts") Set<Contact> contacts,
            @Attribute(name = "userId") String userId,
            @Attribute(name = "password") String password,
            @Attribute(name = "activePeriod") DvInterval<DvDateTime> activePeriod,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name, contacts, activePeriod, details);
        this.userId = userId;
        this.password = password;
    }

    /**
     * @return Returns the lastLogin.
     */
    public DvDate getLastLogin() {
        return lastLogin;
    }

    /**
     * @param lastLogin The lastLogin to set.
     */
    public void setLastLogin(DvDate lastLogin) {
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
