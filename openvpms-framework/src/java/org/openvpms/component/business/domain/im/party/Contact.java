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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Defines a contact for a {@link Party}. 
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Contact extends IMObject {
    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The time that this participation was activitated
     */
    private Date activeStartTime;
    
    /**
     * The time that this participation was inactivated
     */
    private Date activeEndTime;
    
    /**
     * A list of {@link Address} instances for this contact.
     */
    private Set<Address> addresses = new HashSet<Address>();
    
    /**
     * A reference to the owning {@link Party}
     */
    private Party party;
    
    
    /**
     * Define a protected default constructor
     */
    public Contact() {
        // do nothing
    }
    
    /**
     * Constructs a Contact.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param activePeriod
     *            the active period for this contact
     */
    public Contact(ArchetypeId archetypeId) {
        super(archetypeId);
    }
    

    /**
     * @return Returns the entity.
     */
    public Party getParty() {
        return party;
    }

    /**
     * @param entity The entity to set.
     */
    public void setParty(Party party) {
        this.party = party;
    }

    /**
     * @return Returns the activeEndTime.
     */
    public Date getActiveEndTime() {
        return activeEndTime;
    }

    /**
     * @param activeEndTime The activeEndTime to set.
     */
    public void setActiveEndTime(Date activeEndTime) {
        this.activeEndTime = activeEndTime;
    }

    /**
     * @return Returns the activeStartTime.
     */
    public Date getActiveStartTime() {
        return activeStartTime;
    }

    /**
     * @param activeStartTime The activeStartTime to set.
     */
    public void setActiveStartTime(Date activeStartTime) {
        this.activeStartTime = activeStartTime;
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
        addresses.add(address);
    }

    /**
     * Remove the specified address from the contact list
     * 
     * @param address
     */
    public void removeAddress(Address address) {
        addresses.remove(address);
    }
    
    /**
     * Return a string, which concatenates the descriptos of 
     * each address
     * 
     * @return String
     */
    public String getAddressesAsString() {
        StringBuffer buf = new StringBuffer();
        for (Address address : addresses) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(address.getDescription());
        }
        
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Contact copy = (Contact)super.clone();
        copy.activeEndTime = (Date)(this.activeEndTime == null ?
                null : this.activeEndTime.clone());
        copy.activeStartTime = (Date)(this.activeStartTime == null ?
                null : this.activeStartTime.clone());
        copy.addresses = new HashSet<Address>(this.addresses);
        copy.party = this.party;

        return copy;
    }
}
