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
import java.util.Set;

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.DvOrdinal;
import org.openehr.rm.datatypes.quantity.DvQuantity;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;
import org.openehr.rm.support.identification.ObjectReference;

/**
 * Describes the relationship between two entities.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityRelationship extends Locatable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -7721503029190016273L;

    /**
     * TODO Definition for sequence
     */
    private DvOrdinal sequence;

    /**
     * TODO Definition for reason
     */
    private DvText reason;

    /**
     * TODO Definition for scrapQuantity
     */
    private DvQuantity scrapQuantity;

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
    private ObjectReference sourceEntity;

    /**
     * Record the target entity in the relationship
     */
    private ObjectReference targetEntity;

    /**
     * Constructs a valid intance of an entity relationship
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
     * @param sourceEntity
     *            the source entity of this relationship
     * @param targetEntity
     *            the target entity of this relationship                        
     * @param details
     *            a compound item that describes the details of this
     *            archetype.
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public EntityRelationship(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true)DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "sourceEntity", required = true) ObjectReference sourceEntity,
            @Attribute(name = "targetEntity", required = true) ObjectReference targetEntity,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
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
    public DvText getReason() {
        return reason;
    }

    /**
     * @param reason
     *            The reason to set.
     */
    public void setReason(DvText reason) {
        this.reason = reason;
    }

    /**
     * @return Returns the scrapQuantity.
     */
    public DvQuantity getScrapQuantity() {
        return scrapQuantity;
    }

    /**
     * @param scrapQuantity
     *            The scrapQuantity to set.
     */
    public void setScrapQuantity(DvQuantity scrapQuantity) {
        this.scrapQuantity = scrapQuantity;
    }

    /**
     * @return Returns the sequence.
     */
    public DvOrdinal getSequence() {
        return sequence;
    }

    /**
     * @param sequence
     *            The sequence to set.
     */
    public void setSequence(DvOrdinal sequence) {
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
    public ObjectReference getSourceEntity() {
        return sourceEntity;
    }

    /**
     * @param sourceEntity The sourceEntity to set.
     */
    public void setSourceEntity(ObjectReference sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    /**
     * @return Returns the targetEntity.
     */
    public ObjectReference getTargetEntity() {
        return targetEntity;
    }

    /**
     * @param targetEntity The targetEntity to set.
     */
    public void setTargetEntity(ObjectReference targetEntity) {
        this.targetEntity = targetEntity;
    }

}
