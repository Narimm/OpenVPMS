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

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;

/**
 * Provides a mechanism to define a hierarchy of classifications
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Classification extends IMlObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1430690868255233290L;

    /**
     * Indicates the details of the classification
     */
    private ItemStructure details;

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
     *            the archietype that is constraining this object
     * @param imVersion
     *            the version of the reference model
     * @param archetypeNodeId
     *            the id of this node                        
     * @param name
     *            the name 
     * @param details
     *            the details of this classification
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    @FullConstructor
    public Classification(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "details") ItemStructure details,
            @Attribute(name = "parent") Classification parent) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
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

}
