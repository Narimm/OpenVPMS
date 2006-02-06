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

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

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
    private DynamicAttributeMap details;
    
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
        // do nothing
    }
    
    /**
     * Constructs an instance of an act.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param sequence
     *            the associated sequence number
     * @param sourceAct
     *            the source act
     * @param targetAct
     *            the target act                                    
     * @param details
     *            dynamic details of the act relationship
     */
    public ActRelationship(ArchetypeId archetypeId, Act sourceAct, 
        Act targetAct, int sequence, DynamicAttributeMap details) {
        super(archetypeId);
        this.sequence = sequence;
        this.sourceAct = sourceAct;
        this.targetAct = targetAct;
        this.details = details;
        this.parentChildRelationship = false;
    }
    
    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
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
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ActRelationship copy = (ActRelationship)super.clone();
        copy.details = (DynamicAttributeMap)(this.details == null ?
                null : this.details.clone());
        copy.negationInd = this.negationInd;
        copy.parentChildRelationship = this.parentChildRelationship;
        copy.sequence = this.sequence;
        
        // no need to clone the source and target act
        copy.sourceAct = this.sourceAct;
        copy.targetAct = this.targetAct;
        
        return copy;
    }

}
