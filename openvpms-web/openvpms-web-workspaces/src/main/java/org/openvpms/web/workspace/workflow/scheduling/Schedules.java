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

import org.openvpms.component.business.domain.im.common.Entity;

import java.util.List;

/**
 * Schedule information.
 *
 * @author Tim Anderson
 */
public interface Schedules {

    /**
     * Returns the schedule views.
     *
     * @return the schedule views
     */
    List<Entity> getScheduleViews();

    /**
     * Returns the default schedule view.
     *
     * @return the default schedule view. May be {@code null}
     */
    Entity getDefaultScheduleView();

    /**
     * Returns the schedules for the specified schedule view.
     *
     * @param view the schedule view
     * @return the corresponding schedules
     */
    List<Entity> getSchedules(Entity view);

    /**
     * Returns a display name for the schedule selector.
     *
     * @return a display name for the schedule selector
     */
    String getScheduleDisplayName();

    /**
     * Returns the schedule view archetype short name.
     *
     * @return the schedule view archetype short name
     */
    String getScheduleViewShortName();
}
