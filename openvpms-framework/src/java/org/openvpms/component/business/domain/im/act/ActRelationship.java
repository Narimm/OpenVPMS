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
 * A class that represents the directed association between two {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActRelationship extends IMObject {

    /**
     * Serialisation version identifier.
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
     * Holds dynamic details about the act relationship.
     */
    private Map<String, TypedValue> details = new HashMap<String, TypedValue>();

    /**
     * Reference to the source {@link Act} reference.
     */
    private IMObjectReference source;

    /**
     * Reference to the target {@link Act} reference.
     */
    private IMObjectReference target;


    /**
     * Default constructor.
     */
    public ActRelationship() {
        // do nothing
    }

    /**
     * Returns the relationship details.
     *
     * @return the details
     */
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * Sets the relationship details.
     *
     * @param details the details to set
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
     * Returns the source act reference. If this is a parent/child relationship,
     * then the source act represents the parent.
     *
     * @return the source act reference
     */
    public IMObjectReference getSource() {
        return source;
    }

    /**
     * Sets the source act reference.
     *
     * @param source a reference to the source act
     */
    public void setSource(IMObjectReference source) {
        this.source = source;
    }

    /**
     * Returns the target act reference, If this is a parent/child relationship,
     * then the target act represents the child.
     *
     * @return the target act reference
     */
    public IMObjectReference getTarget() {
        return target;
    }

    /**
     * Sets the target act reference.
     *
     * @param target a reference to the target act
     */
    public void setTarget(IMObjectReference target) {
        this.target = target;
    }

    /**
     * @return Returns the parentChildRelationship.
     * @deprecated use {@link #isParentChildRelationship()}
     */
    @Deprecated
    public boolean getParentChildRelationship() {
        return parentChildRelationship;
    }

    /**
     * Determines if this is a parent/child relationship between two acts.
     *
     * @param parentChildRelationship if <tt>true</true> it is a parent/child
     *                                relationship
     */
    public void setParentChildRelationship(boolean parentChildRelationship) {
        this.parentChildRelationship = parentChildRelationship;
    }

    /**
     * Determines if this is a parent/child relationship between two acts.
     * If <tt>true</tt> it indicates that the parent act is the owner of the
     * relationship and is responsible for managing its lifecycle. When the
     * parent act is deleted, then the child act must also be deleted.
     *
     * @return <tt>true</tt> if this is a parent/child relationship
     */
    public boolean isParentChildRelationship() {
        return parentChildRelationship;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ActRelationship copy = (ActRelationship) super.clone();
        copy.details = (details == null) ? null
                : new HashMap<String, TypedValue>(details);
        copy.parentChildRelationship = this.parentChildRelationship;

        // no need to clone the source and target act
        copy.source = this.source;
        copy.target = this.target;

        return copy;
    }

}
