package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

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
