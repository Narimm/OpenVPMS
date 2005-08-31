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
import java.util.HashSet;
import java.util.Set;

// openehr java kernel
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.text.DvText;

// openvpms framework
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.IMlObject;

/**
 * Defines a contact for a {@link Party}. 
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Contact extends IMlObject {
    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The period for which this contact is active
     */
    private DvInterval<DvDate> activePeriod;
    
    /**
     * A list of {@link Address} instances for this contact.
     */
    private Set<Address> addresses;
    
    
    /**
     * Define a protected default constructor
     */
    protected Contact() {
    }
    
    /**
     * Constructs a Contact.
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
     * @param activePeriod
     *            the active period for this contact
     * @throws IllegalArgumentException
     *             if name null or archetypeNodeId null, or links not null and
     *             empty, or addresses null or empty
     */
    @FullConstructor
    public Contact(
            @Attribute(name = "uid", required=true) String uid, 
            @Attribute(name = "archetypeId", required=true) String archetypeId, 
            @Attribute(name = "imVersion", required=true) String imVersion, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true) DvText name, 
            @Attribute(name = "activePeriod") DvInterval<DvDate> activePeriod) {
        super(uid, archetypeId, imVersion, archetypeNodeId, name);
        this.activePeriod = activePeriod;
        this.addresses = new HashSet<Address>();
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
     * Convenience method that return all the addresses as an array.
     * 
     * @return Returns the addresses.
     */
    public Address[] getAddressesAsArray() {
        return (Address[])addresses.toArray(new Address[addresses.size()]);
    }
    
    /**
     * Convenience method that returns the number of address elements
     * 
     * @return int
     */
    public int getNumOfAddresses() {
        return addresses.size();
    }

    /**
     * Return the addresses
     * 
     * @return Set<Address>
     */
    public Set<Address> getAddresses() {
        return this.addresses;
    }
    
    /**
     * @param addresses The addresses to set.
     */
    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }
    
    /**
     * Add an address to the contact
     * 
     * @param address 
     *            the address to add
     */
    public void addAddress(Address address) {
        address.addContact(this);
        addresses.add(address);
    }

    /**
     * Remove the specified address from the contact list
     * 
     * @param address
     */
    public void removeAddress(Address address) {
        address.removeContact(this);
        addresses.remove(address);
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
