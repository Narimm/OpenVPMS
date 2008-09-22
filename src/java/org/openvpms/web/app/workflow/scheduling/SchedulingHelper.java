/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import java.util.Calendar;
import java.util.Date;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SchedulingHelper {
    /**
     * Returns the minutes from midnight for the specified time.
     *
     * @param time the time
     * @return the minutes from midnight for <tt>time</tt>
     */
    public static int getMinutes(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int mins = calendar.get(Calendar.MINUTE);
        return (hour * 60) + mins;
    }

    public static int getSlotMinutes(Date time, int slotSize, boolean roundUp) {
        int mins = getMinutes(time);
        int result = (mins / slotSize) * slotSize;
        if (result != mins && roundUp) {
            result += slotSize;
        }
        return result;
    }
}
