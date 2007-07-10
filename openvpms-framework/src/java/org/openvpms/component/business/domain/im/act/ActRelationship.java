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


package org.openvpms.component.business.domain.im.act;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValueMap;

import java.util.HashMap;
import java.util.Map;


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
     * Indicates whether the relationship is one of parent-child. This means
     * that the parent is the owner of the relationship and is responsible for
     * managing its lifecycle. When the parent is deleted then it will also
     * delete the child
     */
    private boolean parentChildRelationship;

    /**
     * Holds dynamic details about the act relationship
     */
    private Map<String, TypedValue> details = new HashMap<String, TypedValue>();

    /**
     * Reference to the source {@link Act} reference
     */
    private IMObjectReference source;

    /**
     * Reference to the target {@link Act} reference
     */
    private IMObjectReference target;


    /**
     * Default constructor
     */
    public ActRelationship() {
        // do nothing
    }

    /**
     * @return Returns the details.
     */
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(Map<String, Object> details) {
        this.details = TypedValueMap.create(details);
    }

    /**
     * @return Returns the sequence.
     * @deprecated no replacement
     */
    @Deprecated
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence The sequence to set.
     * @deprecated no replacement
     */
    @Deprecated
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * @return Returns the sourceAct.
     */
    public IMObjectReference getSource() {
        return source;
    }

    /**
     * @param sourceAct The sourceAct to set.
     */
    public void setSource(IMObjectReference sourceAct) {
        this.source = sourceAct;
    }

    /**
     * @return Returns the targetAct.
     */
    public IMObjectReference getTarget() {
        return target;
    }

    /**
     * @param targetAct The targetAct to set.
     */
    public void setTarget(IMObjectReference targetAct) {
        this.target = targetAct;
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
        copy.details = (details == null) ? null
                : new HashMap<String, TypedValue>(details);
        copy.parentChildRelationship = this.parentChildRelationship;

        // no need to clone the source and target act
        copy.source = this.source;
        copy.target = this.target;

        return copy;
    }

}
