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

package org.openvpms.component.business.domain.im.party;

// openehr-java-kernel
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
 * Address of a contact.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Address extends Locatable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -2619257937834307246L;

    /**
     * Address details specific, which is specified by the archetype definition
     */
    private ItemStructure details;

    /**
     * Construct an address
     * 
     * @param uid
     *            unique identity for this field
     * @param archetypeNodeId
     *            the identity of the associated node in the archetype def
     * @param name
     *            the name ?????
     * @param archetypeDetails
     *            a reference ot the achetype definition
     * @param links
     *            TODO Need to resolv this
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    @FullConstructor
    public Address(
            @Attribute(name = "uid") ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "details", required = true) ItemStructure details) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
        if (details == null) {
            throw new IllegalArgumentException("null details");
        }
        this.details = details;
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
