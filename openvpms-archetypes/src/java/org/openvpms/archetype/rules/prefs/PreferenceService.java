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
import org.openvpms.component.business.domain.im.security.User;

/**
 * User preference service.
 *
 * @author Tim Anderson
 */
public interface PreferenceService {

    /**
     * Returns the preferences for a user.
     * <p/>
     * These preferences exist for the user session; changes and are not persistent.
     *
     * @param user the user
     * @return the user preferences
     */
    Preferences getPreferences(User user);

    /**
     * Returns the root preference entity for a user, creating them if they don't exist.
     *
     * @param user the user
     * @return the root preference entity
     */
    Entity getEntity(User user);

}
