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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.party.Contact;

import java.util.Date;
import java.util.Set;


/**
 * Data object interface corresponding to the {@link Contact} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ContactDO extends IMObjectDO {

    /**
     * Returns the party that has this contact.
     *
     * @return returns the party. May be <tt>null</tt>
     */
    PartyDO getParty();

    /**
     * Sets the party.
     *
     * @param party the party to set. May be <tt>null</tt>
     */
    void setParty(PartyDO party);

    /**
     * Returns the time when the contact became active.
     *
     * @return the active start time
     */
    Date getActiveStartTime();

    /**
     * Sets the active start time.
     *
     * @param startTime the active start time.
     */
    void setActiveStartTime(Date startTime);

    /**
     * Returns the time when the contact becomes inactive.
     *
     * @return the active end time
     */
    Date getActiveEndTime();

    /**
     * Sets the active end time.
     *
     * @param endTime the active end time
     */
    void setActiveEndTime(Date endTime);

    /**
     * Returns the classifications for this contact.
     *
     * @return the classifications
     */
    Set<LookupDO> getClassifications();

    /**
     * Adds a classification.
     *
     * @param classification the classification to add
     */
    void addClassification(LookupDO classification);

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    void removeClassification(LookupDO classification);
}
