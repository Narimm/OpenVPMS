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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.system.ServiceHelper;

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
     * The location rules.
     */
    private final LocationRules rules;

    /**
     * Constructs an {@link AbstractSchedules}.
     *
     * @param location      the location. May be {@code null}
     * @param viewShortName the schedule view archetype short name
     */
    public AbstractSchedules(Party location, String viewShortName) {
        this.location = location;
        this.viewShortName = viewShortName;
        rules = ServiceHelper.getBean(LocationRules.class);
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
}
