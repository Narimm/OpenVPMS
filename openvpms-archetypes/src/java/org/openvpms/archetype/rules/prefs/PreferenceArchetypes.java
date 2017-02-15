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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

/**
 * Preference archetypes.
 *
 * @author Tim Anderson
 */
public class PreferenceArchetypes {

    /**
     * Preference archetype short name.
     */
    public static final String PREFERENCES = "entity.preferences";

    /**
     * General preferences archetype short name.
     */
    public static final String GENERAL = "entity.preferenceGroupGeneral";

    /**
     * Summary preferences archetype short name.
     */
    public static final String SUMMARY = "entity.preferenceGroupSummary";

    /**
     * Charge preferences archetype short name.
     */
    public static final String CHARGE = "entity.preferenceGroupCharge";

    /**
     * Patient history preferences archetype short name.
     */
    public static final String HISTORY = "entity.preferenceGroupHistory";

    /**
     * Scheduling preferences archetype short name.
     */
    public static final String SCHEDULING = "entity.preferenceGroupScheduling";

    /**
     * Work list preferences archetype short name.
     */
    public static final String WORK_LIST = "entity.preferenceGroupWorkList";

    /**
     * All preference groups.
     */
    public static final String PREFERENCE_GROUPS = "entity.preferenceGroup*";

    /**
     * All preference group links.
     */
    public static final String PREFERENCE_GROUP_LINKS = "entityLink.preferenceGroup*";

    /**
     * Preferences user archetype short name.
     */
    public static final String USER_LINK = "entityLink.preferencesUser";
}
