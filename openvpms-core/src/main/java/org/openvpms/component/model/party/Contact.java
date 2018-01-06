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
 * Defines a contact for a {@link Party}.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Contact extends IMObject {

    /**
     * Returns the time when the contact is active from.
     *
     * @return the start time. May be {@code null}.
     */
    Date getActiveStartTime();

    /**
     * Sets the time when the contact is active from.
     *
     * @param time the start time. May be {@code null}.
     */
    void setActiveStartTime(Date time);

    /**
     * Returns the time when the contact is active to.
     *
     * @return the end time. May be {@code null}.
     */
    Date getActiveEndTime();

    /**
     * Sets the time when the contact is active to.
     *
     * @param time the end time. May be {@code null}.
     */
    void setActiveEndTime(Date time);

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
