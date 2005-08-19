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

// java core
import java.util.List;
import java.util.Set;

// openehr java kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.demographic.Address;
import org.openehr.rm.support.identification.ObjectID;

/**
 * Defines a contact for a {@link Party}. 
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Contact extends Locatable {
    /**
     * Generated SUID
     */
    private static final long serialVersionUID = -3563061772811921224L;

    /**
     * The period for which this contact is active
     */
    private DvInterval<DvDate> activePeriod;
    
    /**
     * A list of {@link Address} instances for this contact.
     */
    private List<Address> addresses;
    
    /**
     * Constructs a Contact
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeNodeId
     *            the node id within the archetype definition
     * @param name
     *            the name 
     * @param archetypeDetails
     *            reference to the archetype definition
     * @param links
     *            TODO What does this actually do
     * @param activePeriod
     *            the period for which this contact is active
     * @param addresses
     *            not null
     * @throws IllegalArgumentException
     *             if name null or archetypeNodeId null, or links not null and
     *             empty, or addresses null or empty
     */
    @FullConstructor
    public Contact(
            @Attribute(name = "uid") ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "activePeriod") DvInterval<DvDate> activePeriod,
            @Attribute(name = "addresses", required = true) List<Address> addresses) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
        if (addresses == null || addresses.size() == 0) {
            throw new IllegalArgumentException("null or empty addresses");
        }
        this.activePeriod = activePeriod;
        this.addresses = addresses;
    }
    
    /**
     * @return Returns the activePeriod.
     */
    public DvInterval<DvDate> getActivePeriod() {
        return activePeriod;
    }

    /**
     * @param activePeriod The activePeriod to set.
     */
    public void setActivePeriod(DvInterval<DvDate> activePeriod) {
        this.activePeriod = activePeriod;
    }

    /**
     * @return Returns the addresses.
     */
    public List<Address> getAddresses() {
        return addresses;
    }

    /**
     * @param addresses The addresses to set.
     */
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
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
