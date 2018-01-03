/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.model.party;

import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.IMObject;

import java.util.Date;
import java.util.Set;

/**
 * .
 *
 * @author Tim Anderson
 */
public interface Contact extends IMObject {

    /**
     * @return Returns the activeEndTime.
     */
    Date getActiveEndTime();

    /**
     * @param activeEndTime The activeEndTime to set.
     */
    void setActiveEndTime(Date activeEndTime);

    /**
     * @return Returns the activeStartTime.
     */
    Date getActiveStartTime();

    /**
     * @param activeStartTime The activeStartTime to set.
     */
    void setActiveStartTime(Date activeStartTime);

    /**
     * Returns the classifications for this contact.
     *
     * @return the classifications
     */
    Set<Lookup> getClassifications();

    /**
     * Adds a classification.
     *
     * @param classification the classification to add
     */
    void addClassification(Lookup classification);

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    void removeClassification(Lookup classification);

}
