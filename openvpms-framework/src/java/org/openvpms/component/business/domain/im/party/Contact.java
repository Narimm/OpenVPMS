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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValueMap;
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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
     * The classification for the contact.
     */
    private Set<Lookup> classifications = new HashSet<Lookup>();

    /*
    * Specific details for the contact purpose.
    */
    private Map<String, TypedValue> details = new HashMap<String, TypedValue>();

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
     * Returns the party.
     *
     * @return returns the party
     */
    public Party getParty() {
        return party;
    }

    /**
     * Sets the party.
     *
     * @param party the entity to set.
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
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(Map<String, Object> details) {
        this.details = TypedValueMap.create(details);
    }

    /**
     * Convenience method that returns all the classification lookups as an 
     * array.
     * 
     * @return the classifications
     */
    public Lookup[] getClassificationsAsArray() {
        return classifications.toArray(new Lookup[classifications.size()]);
    }

    /**
     * Returns the classifications for this contact.
     * 
     * @return the classifications
     */
    public Set<Lookup> getClassifications() {
        return classifications;
    }

    /**
     * Sets the classifications for this contact.
     *
     * @param classifications the classifications to set.
     */
    public void setClassifications(Set<Lookup> classifications) {
        this.classifications = classifications;
    }

    /**
     * Adds a classification.
     * 
     * @param classification the classification to add
     */
    public void addClassification(Lookup classification) {
        classifications.add(classification);
    }

    /**
     * Removes a classification.
     * 
     * @param classification the classification to remove
     */
    public void removeClassification(Lookup classification) {
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
        copy.classifications = new HashSet<Lookup>(this.classifications);
        copy.details = (details == null) ? null : new HashMap<String, TypedValue>(details);
        copy.party = this.party;

        return copy;
    }
}
