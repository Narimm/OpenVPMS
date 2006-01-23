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


/**
 * An actor is any {@link Entity} that can take on a {@link Role}.
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Actor extends Party {

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The set of {@link Role}s supported by this actor.
     */
    private Set<Role> roles = new HashSet<Role>();

    /**
     * Default constructor
     */
    public Actor() {
        // do nothing
    }
    
    /**
     * Constructs an actor.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name of the entity            
     * @param description
     *            the description of the party            
     */
    public Actor(ArchetypeId archetypeId, String name, 
            String description) {
        super(archetypeId, name, description, null, null);
    }
    
    /**
     * @return Returns the roles.
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * @param roles The roles to set.
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    /** 
     * Add a role to this actor
     * 
     * @param role
     *            the role to create
     */
    public void addRole(Role role) {
        role.setActor(this);
        roles.add(role);
    }
    
    /**
     * Remove the role for this actor
     * 
     * @param role
     *            trhe roles to remvoe
     */
    public void removeRole(Role role) {
        role.setActor(null);
        roles.remove(role);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.Entity#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Actor copy = (Actor)super.clone();
        copy.roles = new HashSet<Role>(this.roles);
        
        return copy;
    }
}
