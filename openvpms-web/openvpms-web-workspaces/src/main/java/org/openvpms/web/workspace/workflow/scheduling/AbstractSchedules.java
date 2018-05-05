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

package org.openvpms.web.workspace.workflow.scheduling;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.List;

/**
 * Abstract implementation of the {@link Schedules} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractSchedules implements Schedules {

    /**
     * The current location. May be {@code null}
     */
    private Party location;

    /**
     * The archetype short name of the schedule views
     */
    private final String viewShortName;

    /**
     * User preferences.
     */
    private final Preferences prefs;

    /**
     * The location rules.
     */
    private final LocationRules rules;

    /**
     * Constructs an {@link AbstractSchedules}.
     *
     * @param location      the location. May be {@code null}
     * @param viewShortName the schedule view archetype short name
     * @param prefs         the user preferences
     * @param rules         the location rules
     */
    public AbstractSchedules(Party location, String viewShortName, Preferences prefs, LocationRules rules) {
        this.location = location;
        this.viewShortName = viewShortName;
        this.prefs = prefs;
        this.rules = rules;
    }

    /**
     * Returns the current location.
     *
     * @return the location. May be {@code null}
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Returns the schedule view archetype short name.
     *
     * @return the schedule view archetype short name
     */
    @Override
    public String getScheduleViewShortName() {
        return viewShortName;
    }

    /**
     * Returns the location rules.
     *
     * @return the location rules
     */
    protected LocationRules getLocationRules() {
        return rules;
    }

    /**
     * Returns the schedule view from user preferences.
     *
     * @param preferenceGroup the preference group name. The group must have a 'view' node.
     * @param views           the available schedule views
     * @return the schedule view. May be {@code null}
     */
    protected Entity getScheduleView(String preferenceGroup, List<Entity> views) {
        IMObjectReference reference = getScheduleView(preferenceGroup);
        return IMObjectHelper.getObject(reference, views);
    }

    /**
     * Returns the schedule view reference from user preferences.
     *
     * @param preferenceGroup the preference group name. The group must have a 'view' node.
     * @return the schedule view reference. May be {@code null}
     */
    protected IMObjectReference getScheduleView(String preferenceGroup) {
        return prefs.getReference(preferenceGroup, "view", null);
    }

    /**
     * Returns the schedule from user preferences.
     *
     * @param preferenceGroup the preference group name. The group must have a 'schedule' node.
     * @param schedules       the available schedules
     * @return the schedule view. May be {@code null}
     */
    protected Entity getSchedule(String preferenceGroup, List<Entity> schedules) {
        IMObjectReference reference = prefs.getReference(preferenceGroup, "schedule", null);
        return IMObjectHelper.getObject(reference, schedules);
    }
}
