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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.Date;
import java.util.Random;


/**
 * Unit test helper for reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderTestHelper extends TestHelper {

    /**
     * Creates and saves a new <em>entity.reminderType</em>.
     *
     * @param groups a list of <em>lookup.reminderGroup</em>
     * @return a new reminder
     */
    public static Entity createReminderType(Lookup ... groups) {
        return createReminderType(1, DateUnits.MONTHS, groups);
    }

    /**
     * Creates and saves a new <em>entity.reminderType</em>.
     *
     * @param defaultInterval the default reminder interval
     * @param defaultUnits    the default reminder interval units
     * @param groups          a list of <em>lookup.reminderGroup</em>
     * @return a new reminder
     */
    public static Entity createReminderType(int defaultInterval,
                                            DateUnits defaultUnits,
                                            Lookup ... groups) {
        Entity reminder = (Entity) create("entity.reminderType");
        EntityBean bean = new EntityBean(reminder);
        bean.setValue("name", "XReminderType");
        bean.setValue("defaultInterval", defaultInterval);
        bean.setValue("defaultUnits", defaultUnits.toString());
        for (Lookup group : groups) {
            reminder.addClassification(group);
        }
        bean.save();
        return reminder;
    }

    /**
     * Creates and saves a new reminder.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param dueDate      the due date
     * @return a new reminder
     */
    public static Act createReminder(Party patient, Entity reminderType,
                                     Date dueDate) {
        Act reminder = createReminder(patient, reminderType);
        reminder.setActivityEndTime(dueDate);
        save(reminder);
        return reminder;
    }

    /**
     * Creates a new reminder.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @return a new reminder
     */
    public static Act createReminder(Party patient, Entity reminderType) {
        Act act = (Act) create("act.patientReminder");
        ActBean bean = new ActBean(act);
        bean.setStatus(ActStatus.IN_PROGRESS);
        bean.setParticipant("participation.patient", patient);
        bean.setParticipant(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION, reminderType);
        return act;
    }

    /**
     * Creates and saves a new <em>lookup.reminderGroup</em> classification
     * lookup.
     *
     * @return a new lookup
     */
    public static Lookup createReminderGroup() {
        Lookup group = (Lookup) create("lookup.reminderGroup");
        group.setCode("XREMINDERGROUP_" + Math.abs(new Random().nextInt()));
        save(group);
        return group;
    }

}
