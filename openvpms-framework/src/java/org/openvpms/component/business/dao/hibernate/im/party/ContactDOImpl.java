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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link ContactDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-05-02 14:28:50 +1000 (Wed, 02 May 2007) $
 */
public class ContactDOImpl extends IMObjectDOImpl implements ContactDO {

    /**
     * The time that this contact was activitated.
     */
    private Date startTime;

    /**
     * The time that this contact was inactivated.
     */
    private Date endTime;

    /**
     * The classification for the contact.
     */
    private Set<LookupDO> classifications = new HashSet<LookupDO>();

    /**
     * The owning party.
     */
    private PartyDO party;


    /**
     * Default constructor.
     */
    public ContactDOImpl() {
        // do nothing
    }

    /**
     * Returns the party that has this contact.
     *
     * @return returns the party. May be <tt>null</tt>
     */
    public PartyDO getParty() {
        return party;
    }

    /**
     * Sets the party.
     *
     * @param party the party to set. May be <tt>null</tt>
     */
    public void setParty(PartyDO party) {
        this.party = party;
    }

    /**
     * Returns the time when the contact became active.
     *
     * @return the active start time
     */
    public Date getActiveStartTime() {
        return startTime;
    }

    /**
     * Sets the active start time.
     *
     * @param startTime the active start time.
     */
    public void setActiveStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the time when the contact becomes inactive.
     *
     * @return the active end time
     */
    public Date getActiveEndTime() {
        return endTime;
    }

    /**
     * Sets the active end time.
     *
     * @param endTime the active end time
     */
    public void setActiveEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the classifications for this contact.
     *
     * @return the classifications
     */
    public Set<LookupDO> getClassifications() {
        return classifications;
    }

    /**
     * Adds a classification.
     *
     * @param classification the classification to add
     */
    public void addClassification(LookupDO classification) {
        classifications.add(classification);
    }

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    public void removeClassification(LookupDO classification) {
        classifications.remove(classification);
    }

    /**
     * Sets the classifications for this contact.
     *
     * @param classifications the classifications to set.
     */
    protected void setClassifications(Set<LookupDO> classifications) {
        this.classifications = classifications;
    }

}
