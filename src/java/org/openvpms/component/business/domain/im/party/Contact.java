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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

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
     * A history oF contact puprpose
     */
    private Set<ContactPurpose> contactPurposes = new HashSet<ContactPurpose>();
    
    /**
     * Specific details for the contact purpose.
     */
    private DynamicAttributeMap details;

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
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /**
     * Convenience method that return all the {@link ContactPurpose} as an array.
     * 
     * @return ContactPurpose[]
     */
    public ContactPurpose[] getContactPurposesAsArray() {
        return (ContactPurpose[])contactPurposes.toArray(
                new ContactPurpose[contactPurposes.size()]);
    }
    
    /**
     * Convenience method that returns the number of {@link ContactPurpose}
     * elements
     * 
     * @return int
     */
    public int getNumOfAddresses() {
        return contactPurposes.size();
    }

    /**
     * Return all the associated {@link ContactPurpose}
     * 
     * @return Set<ContactPurpose>
     */
    public Set<ContactPurpose> getContactPurposes() {
        return this.contactPurposes;
    }
    
    /**
     * @param contactPurposes The contactPurposes to set.
     */
    public void setContactPurposes(Set<ContactPurpose> contactPurposes) {
        this.contactPurposes = contactPurposes;
    }
    
    /**
     * Add a {@link ContactPurpose}
     * 
     * @param contactPurpose 
     *            the contact purpose to add
     */
    public void addContactPurpose(ContactPurpose contactPurpose) {
        contactPurpose.setContact(this);
        contactPurposes.add(contactPurpose);
    }

    /**
     * Remove the specified {@link ContactPurpose}.
     * 
     * @param contactPurpose
     */
    public void removeContactPurpose(ContactPurpose contactPurpose) {
        contactPurposes.remove(contactPurpose);
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
        copy.contactPurposes = new HashSet<ContactPurpose>(this.contactPurposes);
        copy.party = this.party;

        return copy;
    }
}
