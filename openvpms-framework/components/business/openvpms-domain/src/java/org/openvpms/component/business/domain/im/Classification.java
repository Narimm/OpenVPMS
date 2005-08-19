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
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

/**
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Classification extends Locatable {

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
     * Constructs an classification.
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
     *            a compound item that describes the details of this archetype.
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    @FullConstructor
    public Classification(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true)DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "details") ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
        this.details = details;
        this.children = new HashSet<Classification>();
    }

    /**
     * @return Returns the children.
     */
    public Set<Classification> getChildren() {
        return children;
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
    public void addChildClassification(Classification classification) {
        this.children.add(classification);
    }
    
    /**
     * Remove a child {@link Classification}
     * 
     * @return Classification
     */
    public void removeChildClassification(Classification classification) {
        this.children.remove(classification);
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
