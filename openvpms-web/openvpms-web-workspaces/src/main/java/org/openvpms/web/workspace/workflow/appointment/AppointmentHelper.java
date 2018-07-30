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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.entity.Entity;

/**
 * Appointment helper methods.
 *
 * @author Tim Anderson
 */
public class AppointmentHelper {

    /**
     * Determines if a schedule view is a multi-day view.
     *
     * @param scheduleView the schedule view. May be {@code null}
     * @return {@code true} if the view is a multi-day view
     */
    public static boolean isMultiDayView(Entity scheduleView) {
        boolean result = false;
        if (scheduleView != null) {
            IMObjectBean bean = new IMObjectBean(scheduleView);
            result = bean.getBoolean("multipleDayView");
        }
        return result;
    }

}
