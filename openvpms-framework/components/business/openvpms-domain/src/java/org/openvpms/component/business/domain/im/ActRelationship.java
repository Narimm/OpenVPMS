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

import java.util.Set;

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.quantity.DvOrdinal;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

/**
 * A class that represents the directed association between Acts.  
 * 
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ActRelationship extends Locatable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -5338835987898861256L;

    /**
     * An integer representing the relative order of the relationship among 
     * other like typed relationships.
     */
    private DvOrdinal sequence;
    
    /**
     * Indicates that the target {@link Act} is NOT related to the source 
     * {@link Act}.
     */
    private DvBoolean negationInd;
    
    /**
     * Holds dynamic details about the act relationship
     */
    private ItemStructure details;
    
    /**
     * Reference to the source {@link Act}
     */
    private Act sourceAct;
    
    /**
     * Reference to the target {@link Act}
     */
    private Act targetAct;
    
    
    /**
     * Constructs an instance of an act.
     * TODO Need to determine what constitutes a valid construction of this
     * object.
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
     * @param details
     *            a compound item that describes the details of this
     *            archetype.
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public ActRelationship(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true)DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "sourceAct", required = true) Act sourceAct,
            @Attribute(name = "targetAct", required = true) Act targetAct,
            @Attribute(name = "sequence") DvOrdinal sequence,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
        this.sequence = sequence;
        this.sourceAct = sourceAct;
        this.targetAct = targetAct;
        this.details = details;
    }
    
    /**
     * @return Returns the details.
     */
    public ItemStructure getDetails() {
        return details;
    }



    /**
     * @param details The details to set.
     */
    public void setDetails(ItemStructure details) {
        this.details = details;
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
     * @return Returns the sourceAct.
     */
    public Act getSourceAct() {
        return sourceAct;
    }



    /**
     * @param sourceAct The sourceAct to set.
     */
    public void setSourceAct(Act sourceAct) {
        this.sourceAct = sourceAct;
    }



    /**
     * @return Returns the targetAct.
     */
    public Act getTargetAct() {
        return targetAct;
    }



    /**
     * @param targetAct The targetAct to set.
     */
    public void setTargetAct(Act targetAct) {
        this.targetAct = targetAct;
    }



    /* (non-Javadoc)
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO Auto-generated method stub
        return null;
    }

}
