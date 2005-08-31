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

// openehr-java-kernel

// openehr-kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvText;

// openvpms-framework
import org.openvpms.component.business.domain.im.support.IMObjectReference;

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
    private ItemStructure details;

    /**
     * Record the source entity in the relationship
     */
    private IMObjectReference sourceEntity;

    /**
     * Record the target entity in the relationship
     */
    private IMObjectReference targetEntity;
    
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
     *            the archietype that is constraining this object
     * @param imVersion
     *            the version of the reference model
     * @param archetypeNodeId
     *            the id of this node                        
     * @param name
     *            the name 
     * @param details
     *            The details of the addrss object
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    @FullConstructor
    public EntityRelationship(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "sourceEntity", required = true) IMObjectReference sourceEntity,
            @Attribute(name = "targetEntity", required = true) IMObjectReference targetEntity,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
        
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
    public ItemStructure getDetails() {
        return details;
    }

    /**
     * @param details
     *            The details to set.
     */
    public void setDetails(ItemStructure details) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return Returns the sourceEntity.
     */
    public IMObjectReference getSourceEntity() {
        return sourceEntity;
    }

    /**
     * @param sourceEntity The sourceEntity to set.
     */
    public void setSourceEntity(IMObjectReference sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    /**
     * @return Returns the targetEntity.
     */
    public IMObjectReference getTargetEntity() {
        return targetEntity;
    }

    /**
     * @param targetEntity The targetEntity to set.
     */
    public void setTargetEntity(IMObjectReference targetEntity) {
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
