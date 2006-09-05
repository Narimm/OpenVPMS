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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Reminder rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ReminderRules {

    /**
     * Patient reminder act short name.
     */
    public static final String PATIENT_REMINDER = "act.patientReminder";

    /**
     * Reminder type participation short name.
     */
    public static final String REMINDER_TYPE = "participation.reminderType";

    /**
     * The archetype service.
     */
    private final IArchetypeService _service;


    /**
     * Creates a new <code>ReminderRules</code>.
     *
     * @param service the archetype service
     */
    public ReminderRules(IArchetypeService service) {
        _service = service;
    }

    /**
     * Calculate the due date for a reminder using the reminder's start date
     * plus the default interval and units from the associated reminder type.
     *
     * @param act the act
     * @throws ArchetypeServiceException for any archetype service error
     */

    public void calculateReminderDueDate(Act act) {
        ActBean bean = new ActBean(act, _service);
        if (bean.isA(PATIENT_REMINDER)) {
            Date startTime = act.getActivityStartTime();
            Entity reminderType = bean.getParticipant(REMINDER_TYPE);
            Date endTime = null;
            if (startTime != null && reminderType != null) {
                endTime = calculateReminderDueDate(startTime, reminderType);
            }
            act.setActivityEndTime(endTime);
        }
    }

    /**
     * Calculates the due date for a reminder.
     *
     * @param startTime    the start time
     * @param reminderType the reminder type
     * @return the end time for a reminder
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date calculateReminderDueDate(Date startTime, Entity reminderType) {
        EntityBean bean = new EntityBean(reminderType, _service);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        int interval = bean.getInt("defaultInterval");
        String units = bean.getString("defaultUnits");
        if (units != null) {
            units = units.toLowerCase();
            if (units.equals("years")) {
                calendar.add(Calendar.YEAR, interval);
            } else if (units.equals("months")) {
                calendar.add(Calendar.MONTH, interval);
            } else if (units.equals("weeks")) {
                calendar.add(Calendar.DAY_OF_YEAR, interval * 7);
            } else if (units.equals("days")) {
                calendar.add(Calendar.DAY_OF_YEAR, interval);
            }
        }
        return calendar.getTime();
    }

}
