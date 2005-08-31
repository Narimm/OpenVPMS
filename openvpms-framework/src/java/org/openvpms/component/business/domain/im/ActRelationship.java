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

// openehr-kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;

/**
 * A class that represents the directed association between Acts.  
 * 
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ActRelationship extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * An integer representing the relative order of the relationship among 
     * other like typed relationships.
     */
    private int sequence;
    
    /**
     * Indicates that the target {@link Act} is NOT related to the source 
     * {@link Act}.
     */
    private boolean negationInd;
    
    /**
     * Indicates whether the relationship is one of parent-child. This means
     * that the parent is the owner of the relationship and is responsible for
     * managing its lifecycle. When the parent is deleted then it will also
     * delete the child
     */
    private boolean parentChildRelationship;
    
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
     * Default constructor
     */
    protected ActRelationship() {
        // dop nothing
    }
    
    /**
     * Constructs an instance of an act.
     * TODO Need to determine what constitutes a valid construction of this
     * object.
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
     *            dynamic attrbiutes
     * @throws IllegalArgumentException
     *            thrown if the preconditions are not met.
     */
    @FullConstructor
    public ActRelationship(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "sourceAct") Act sourceAct,
            @Attribute(name = "targetAct") Act targetAct,
            @Attribute(name = "sequence") int sequence,
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
        this.sequence = sequence;
        this.sourceAct = sourceAct;
        this.targetAct = targetAct;
        this.details = details;
        this.parentChildRelationship = false;
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
    public boolean getNegationInd() {
        return negationInd;
    }

    /**
     * @param negationInd The negationInd to set.
     */
    public void setNegationInd(boolean negationInd) {
        this.negationInd = negationInd;
    }

    /**
     * @return Returns the sequence.
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(int sequence) {
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

    /**
     * @return Returns the parentChildRelationship.
     */
    public boolean getParentChildRelationship() {
        return parentChildRelationship;
    }

    /**
     * @param parentChildRelationship The parentChildRelationship to set.
     */
    public void setParentChildRelationship(boolean parentChildRelationship) {
        this.parentChildRelationship = parentChildRelationship;
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
