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

// java-core
import java.util.Date;
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * A role is a type of party and can be assigned to one or more parties.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Role extends Party {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The time that this participation was activitated
     */
    private Date activeStartTime;

    /**
     * The time that this participation was inactivated
     */
    private Date activeEndTime;
    
    /**
     * The actor that owes the role
     */
    private Actor actor;

    /**
     * Default constructor
     */
    public Role() {
        // do nothing
    }

    /**
     * Constructs an role.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name of the role
     * @param description
     *            the description of this entity
     * @param contacts
     *            a collection of contacts for this actor
     * @param activePeriod
     *            the period that this role is valid
     * @param details
     *            dynamic properties for this role
     */
    public Role(ArchetypeId archetypeId, String name, String description,
            Set<Contact> contacts, DynamicAttributeMap details) {
        super(archetypeId, name, description, contacts, details);
    }

    /**
     * @return Returns the activeEndTime.
     */
    public Date getActiveEndTime() {
        return activeEndTime;
    }

    /**
     * @param activeEndTime The activeEndTime to set.
     */
    public void setActiveEndTime(Date activeEndTime) {
        this.activeEndTime = activeEndTime;
    }

    /**
     * @return Returns the activeStartTime.
     */
    public Date getActiveStartTime() {
        return activeStartTime;
    }

    /**
     * @param activeStartTime The activeStartTime to set.
     */
    public void setActiveStartTime(Date activeStartTime) {
        this.activeStartTime = activeStartTime;
    }

    /**
     * @return Returns the actor.
     */
    public Actor getActor() {
        return actor;
    }

    /**
     * @param actor The actor to set.
     */
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.party.Party#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Role copy = (Role)super.clone();
        copy.activeEndTime = (Date)(this.activeEndTime == null ?
                null : this.activeEndTime.clone());
        copy.activeStartTime = (Date)(this.activeStartTime == null ?
                null : this.activeStartTime.clone());
        copy.actor = this.actor;
        
        return copy;
    }
}
