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

import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.InvalidTillArchetype;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openvpms.archetype.rules.tax.TaxRuleException;
import org.openvpms.archetype.rules.till.TillRuleException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Reminder Rules
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class ReminderRules {

    /**
     * Till balance act short name.
     */
    public static final String PATIENT_REMINDER = "act.patientReminder";

    /**
     * Calculate the due date for a reminder using the reminders start date
     * plus the default interval and units from the associated reminder type.
     *
     * @param act     the financial act to calculate tax for
     * @param service the archetype service
     * @return the amount of tax for the act
     * @throws TaxRuleException          if the act is invalid
     * @throws ArchetypeServiceException for any archetype service error
     */

     public static void calculateReminderDueDate(Act act, IArchetypeService service) {
        ActBean reminderbean = new ActBean(act, service);
        if (reminderbean.isA(PATIENT_REMINDER)) {
            Entity reminderType = (Entity) reminderbean.getParticipant("participation.reminderType");
            IMObjectBean remtypebean = new IMObjectBean(reminderType);
            GregorianCalendar sdatecal = new GregorianCalendar();
            sdatecal.setTime(reminderbean.getDate("startTime"));
            Integer interval = remtypebean.getInt("defaultInterval");
            String units = remtypebean.getString("defaultUnits");
            if (units.equalsIgnoreCase("years"))
                sdatecal.add(Calendar.YEAR, interval);
            else if (units.equalsIgnoreCase("months"))
                sdatecal.add(Calendar.MONTH, interval);
            else if (units.equalsIgnoreCase("weeks"))
                    sdatecal.add(Calendar.DAY_OF_YEAR, interval*7);
            else if (units.equalsIgnoreCase("days"))
                sdatecal.add(Calendar.DAY_OF_YEAR, interval);
            reminderbean.setValue("endTime", sdatecal.getTime());
         }
     }

}
