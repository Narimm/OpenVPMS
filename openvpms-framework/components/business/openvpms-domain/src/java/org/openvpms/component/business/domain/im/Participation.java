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

// openehr-java-kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.DataStructure;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.DvOrdinal;
import org.openehr.rm.datatypes.quantity.DvQuantity;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

/**
 * A class representing an {@link Entity}'s participantion in an {@link Act}.
 * 
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
/**
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Participation extends Locatable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * An integer representing the relative order of the participantion in
     * relation to other participations of the same act.
     */
    private DvOrdinal sequence;
    
    /**
     * Description of the participation
     */
    private DvText description;
    
    /**
     * Indicates that a particular participation did not occur
     */
    private DvBoolean negationInd;
    
    /**
     * Indicates the mode that the {@link Entity} is participating in the
     * {@link Act}
     */
    private DvText mode;
    
    /**
     * Indicates the time interval that the {@link Entity} was participating
     * in the {@link Act}.
     */
    private DvInterval timeInterval;
    
    /**
     * The percentage of participation in the specified {@link Act}.
     */
    private DvQuantity percentage;
    
    /**
     * Reference to the entity participating in the act
     */
    private Entity entity;
    
    /**
     * Reference to the associated act
     */
    private Act act;
    
    /**
     * Holds details about the participation
     */
    private DataStructure details;


    /**
     * Constructs a participantion between an {@link Entity} and an {@link Act}.
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
     * @param entity
     *            the entity participating in the act
     * @param act 
     *            the associated act                   
     * @param details
     *            a compound item that describes the details of this
     *            archetype.
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public Participation(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "archetypeDetails")Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "entity", required = true) Entity entity,
            @Attribute(name = "act", required = true) Act act,
            @Attribute(name = "details") DataStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
        
        this.details = details;
    }
    
    /**
     * @return Returns the description.
     */
    public DvText getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(DvText description) {
        this.description = description;
    }

    /**
     * @return Returns the details.
     */
    public DataStructure getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DataStructure details) {
        this.details = details;
    }

    /**
     * @return Returns the mode.
     */
    public DvText getMode() {
        return mode;
    }

    /**
     * @param mode The mode to set.
     */
    public void setMode(DvText mode) {
        this.mode = mode;
    }

    /**
     * @return Returns the negationInd.
     */
    public DvBoolean getNegationInd() {
        return negationInd;
    }

    /**
     * @param negationInd The negationInd to set.
     */
    public void setNegationInd(DvBoolean negationInd) {
        this.negationInd = negationInd;
    }

    /**
     * @return Returns the sequence.
     */
    public DvOrdinal getSequence() {
        return sequence;
    }

    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(DvOrdinal sequence) {
        this.sequence = sequence;
    }

    /**
     * @return Returns the timeInterval.
     */
    public DvInterval getTimeInterval() {
        return timeInterval;
    }

    /**
     * @param timeInterval The timeInterval to set.
     */
    public void setTimeInterval(DvInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    /**
     * @return Returns the act.
     */
    public Act getAct() {
        return act;
    }

    /**
     * @return Returns the entity.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return Returns the percentage.
     */
    public DvQuantity getPercentage() {
        return percentage;
    }

    /**
     * @param percentage The percentage to set.
     */
    public void setPercentage(DvQuantity percentage) {
        this.percentage = percentage;
    }

    /* (non-Javadoc)
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO implement this method
        return null;
    }

}
