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
package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Reminder helper.
 *
 * @author Tim Anderson
 */
class ReminderHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderHelper.class);


    /**
     * Flags a reminder item as being in error.
     * <p/>
     * This:
     * <ul>
     * <li>sets the status to {@link ReminderItemStatus#ERROR} and</li>
     * <li>formats a message based on the supplied exception</li>
     * </ul>
     *
     * @param reminder the reminder item
     * @param error    the error
     */
    public static void setError(Act reminder, Throwable error) {
        try {
            reminder = IMObjectHelper.reload(reminder);
            if (reminder != null) {
                reminder.setStatus(ReminderItemStatus.ERROR);
                IMObjectBean bean = new IMObjectBean(reminder);
                String message = ErrorFormatter.format(error);
                if (message != null) {
                    int maxLength = bean.getDescriptor("error").getMaxLength();
                    message = StringUtils.abbreviate(message, maxLength);
                }
                bean.setValue("error", message);
                bean.save();
            }
        } catch (Throwable exception) {
            log.warn(exception, exception);
        }
    }

}
