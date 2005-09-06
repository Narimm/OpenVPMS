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

package org.openvpms.component.business.domain.im;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.DvInterval;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

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
     * Indicates the period that time interval that this relationship is valid.
     */
    private DvInterval<DvDateTime> activePeriod;

    /**
     * Records details of the relationship between the entities.
     */
    private DynamicAttributeMap details;

    /**
     * Record the source entity in the relationship
     */
    private Entity sourceEntity;

    /**
     * Record the target entity in the relationship
     */
    private Entity targetEntity;
    
    /**
     * A relationship may also have an associated entity identity
     */
    private EntityIdentity entityIdentity;

    /**
     * Default constructor
     */
    protected EntityRelationship() {
        // do nothing
    }
    
    /**
     * Constructs a valid intance of an entity relationship
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name of this object
     * @param sourceEntity
     *            the relationship source
     * @param targetEntity
     *            the relationship target                       
     * @param details
     *            The details of the address object
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    public EntityRelationship(String uid, ArchetypeId archetypeId, 
            String name, Entity sourceEntity, Entity targetEntity,
            DynamicAttributeMap details) {
        super(uid, archetypeId, name);
        
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.details = details;
    }
    
    /**
     * @return Returns the activePeriod.
     */
    public DvInterval<DvDateTime> getActivePeriod() {
        return activePeriod;
    }

    /**
     * @param activePeriod
     *            The activePeriod to set.
     */
    public void setActivePeriod(DvInterval<DvDateTime> activePeriod) {
        this.activePeriod = activePeriod;
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
    public Entity getSourceEntity() {
        return sourceEntity;
    }

    /**
     * @param sourceEntity The sourceEntity to set.
     */
    public void setSourceEntity(Entity sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    /**
     * @return Returns the targetEntity.
     */
    public Entity getTargetEntity() {
        return targetEntity;
    }

    /**
     * @param targetEntity The targetEntity to set.
     */
    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    /**
     * @return Returns the entityIdentity.
     */
    public EntityIdentity getEntityIdentity() {
        return entityIdentity;
    }

    /**
     * @param entityIdentity The entityIdentity to set.
     */
    public void setEntityIdentity(EntityIdentity entityIdentity) {
        this.entityIdentity = entityIdentity;
    }
}
