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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;

/**
 * User preference service.
 *
 * @author Tim Anderson
 */
public interface PreferenceService {

    /**
     * Returns preferences for a user or practice.
     *
     * @param party  the party
     * @param source if non-null, specifies the source to copy preferences from if the party has none
     * @param save   if {@code true}, changes will be made persistent  @return the preferences
     */
    Preferences getPreferences(Party party, Party source, boolean save);

    /**
     * Returns the root preference entity for a user or practice, creating it if it doesn't exist.
     *
     * @param party  the party
     * @param source if non-null, specifies the source to copy preferences from if the party has none
     * @return the root preference entity
     */
    Entity getEntity(Party party, Party source);

    /**
     * Resets the preferences for a user or practice.
     *
     * @param party  the party
     * @param source if non-null, specifies the source to copy preferences from if the party has none
     */
    void reset(Party party, Party source);
}
