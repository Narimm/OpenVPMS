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

// openehr kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.DvText;

// openvpms framework
import org.openvpms.component.business.domain.im.InfoModelObject;

/**
 * Address of a contact.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Address extends InfoModelObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -2619257937834307246L;

    /**
     * Address details specific, which is specified by the archetype definition
     */
    private ItemStructure details;

    /**
     * Define a protected default constructor
     */
    protected Address() {
    }
    
    /**
     * Construct an address.
     * 
     * @param uid
     *            unique identity for this field
     * @param archetypeNodeId
     *            the identity of the associated node in the archetype def
     * @param name
     *            the type of address
     * @param archetypeDetails
     *            a reference ot the achetype definition
     * @param details
     *            The details of the addrss object
     * @throws IllegalArgumentException
     *             if the constructor pre-conditions are not satisfied.
     */
    @FullConstructor
    public Address(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "details", required = true) ItemStructure details) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
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
}
