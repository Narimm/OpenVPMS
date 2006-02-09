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
import org.openvpms.component.business.domain.im.common.Classification;
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
     * The classification for the contact
     */
    private Set<Classification> classifications = new HashSet<Classification>();
    
    /*
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
     * Convenience method that return all the {@link Classification} as an array.
     * 
     * @return Classification[]
     */
    public Classification[] getClassificationsAsArray() {
        return (Classification[])classifications.toArray(
                new Classification[classifications.size()]);
    }
    
    /**
     * Return all the associated {@link ContactPurpose}
     * 
     * @return Set<Classification>
     */
    public Set<Classification> getClassifications() {
        return this.classifications;
    }
    
    /**
     * @param classifications The classifications to set.
     */
    public void setClassifications(Set<Classification> classifications) {
        this.classifications = classifications;
    }
    
    /**
     * Add a {@link Classification}
     * 
     * @param classification 
     *            the classification to add
     */
    public void addClassification(Classification classification) {
        classifications.add(classification);
    }

    /**
     * Remove the specified {@link Classification}.
     * 
     * @param classification
     */
    public void removeClassification(Classification classification) {
        classifications.remove(classification);
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
        copy.classifications = new HashSet<Classification>(this.classifications);
        copy.party = this.party;

        return copy;
    }
}
