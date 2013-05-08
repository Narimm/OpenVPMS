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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.ActActions;

/**
 * Determines the actions that may be performed on <em>act.patientReminder</em> and <em>act.patientAlert</em> acts.
 *
 * @author Tim Anderson
 */
public class ReminderActions extends ActActions<Act> {

    /**
     * The singleton instance.
     */
    private static final ReminderActions INSTANCE = new ReminderActions();


    /**
     * Default constructor.
     */
    private ReminderActions() {

    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static ReminderActions getInstance() {
        return INSTANCE;
    }

    /**
     * Determines if a reminder can be resent.
     *
     * @param act the act
     * @return {@code true} if the act is a reminder that can be resent, otherwise {@code false}
     */
    public boolean canResendReminder(Act act) {
        boolean result = false;
        if (TypeHelper.isA(act, ReminderArchetypes.REMINDER)) {
            ActBean bean = new ActBean(act);
            if (bean.getInt("reminderCount") > 0) {
                result = true;
            }
        }
        return result;
    }
}
