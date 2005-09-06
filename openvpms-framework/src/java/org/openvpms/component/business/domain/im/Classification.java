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

import java.util.HashSet;
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Provides a mechanism to define a hierarchy of classifications
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Classification extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1430690868255233290L;

    /**
     * Indicates the details of the classification
     */
    private DynamicAttributeMap details;

    /**
     * Holds the set of children classifications
     */
    private Set<Classification> children;
    
    /**
     * This is a reference to the parent classification in the hierarchy.
     */
    private Classification parent;

    /**
     * Default constructor
     */
    public Classification() {
        // do nothing
    }
    
    /**
     * Constructs an classification.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name 
     * @param parent
     *            the parent classification, if one exists            
     * @param details
     *            dynamic details of the act.
     */
    public Classification(String uid, ArchetypeId archetypeId, String name, 
            Classification parent, DynamicAttributeMap details) {
        super(uid, archetypeId, name);
        this.details = details;
        this.children = new HashSet<Classification>();
        this.parent = parent;
    }

    /**
     * @param children
     *            The children to set.
     */
    public void setChildren(Set<Classification> children) {
        this.children = children;
    }

    /**
     * Add a child {@link Classification}
     * 
     * @param classification
     */
    public void addChild(Classification classification) {
        if (classification == null) {
            throw new EntityException(
                    EntityException.ErrorCode.NullChildClassificationSpecified);
        }
        this.children.add(classification);
    }
    
    /**
     * Remove a child {@link Classification}
     * 
     * @return Classification
     */
    public void removeChild(Classification classification) {
        this.children.remove(classification);
    }
    
    /**
     * Return the the children classifications
     * 
     * @return Classification[]
     */
    public Classification[] getChildren() {
        return (Classification[])this.children.toArray(
                new Classification[children.size()]);
    }
    
    /**
     * @return Returns the parent.
     */
    protected Classification getParent() {
        return parent;
    }

    /**
     * @param parent The parent to set.
     */
    protected void setParent(Classification parent) {
        this.parent = parent;
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
}
