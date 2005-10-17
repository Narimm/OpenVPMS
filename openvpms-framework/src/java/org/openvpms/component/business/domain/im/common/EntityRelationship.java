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

package org.openvpms.component.business.domain.im.common;

// java core
import java.util.Date;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Describes the relationship between two entities.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityRelationship extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * TODO Definition for sequence
     */
    private int sequence;

    /**
     * TODO Definition for reason
     */
    private String reason;

    /**
     * Indicates the active start time for this relationship
     */
    private Date activeStartTime;

    /**
     * Indicates the active end time for this relationship
     */
    private Date activeEndTime;

    /**
     * Records details of the relationship between the entities.
     */
    private DynamicAttributeMap details;

    /**
     * Record the source entity in the relationship
     */
    private Entity source;

    /**
     * Record the target entity in the relationship
     */
    private Entity target;
    
    /**
     * A relationship may also have an associated entity identity
     */
    private EntityIdentity identity;

    /**
     * Default constructor
     */
    public EntityRelationship() {
        // do nothing
    }
    
    /**
     * Constructs a valid intance of an entity relationship
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param source
     *            the relationship source
     * @param target
     *            the relationship target                       
     * @param details
     *            The details of the address object
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    public EntityRelationship(ArchetypeId archetypeId, 
            Entity source, Entity target,
            DynamicAttributeMap details) {
        super(archetypeId);
        
        this.source = source;
        this.target = target;
        this.details = details;
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
     * @return Returns the reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason
     *            The reason to set.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return Returns the sequence.
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence
     *            The sequence to set.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * @return Returns the sourceEntity.
     */
    public Entity getSource() {
        return source;
    }

    /**
     * @param source The source to set.
     */
    public void setSource(Entity source) {
        this.source = source;
    }

    /**
     * @return Returns the target.
     */
    public Entity getTarget() {
        return target;
    }

    /**
     * @param target The target to set.
     */
    public void setTarget(Entity target) {
        this.target = target;
    }

    /**
     * @return Returns the entityIdentity.
     */
    public EntityIdentity getIdentity() {
        return identity;
    }

    /**
     * @param identity The identity to set.
     */
    public void setIdentity(EntityIdentity identity) {
        this.identity = identity;
    }
}
