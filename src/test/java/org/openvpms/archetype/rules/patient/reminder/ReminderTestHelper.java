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
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public static Entity createReminderType(Lookup... groups) {
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
    public static Entity createReminderType(int defaultInterval, DateUnits defaultUnits, Lookup... groups) {
        return createReminderType(defaultInterval, defaultUnits, -1, null, groups);
    }

    /**
     * Creates and saves a new <em>entity.reminderType</em>.
     *
     * @param defaultInterval the default reminder interval
     * @param defaultUnits    the default reminder interval units
     * @param cancelInterval  the cancel interval
     * @param cancelUnits     the cancel interval units. May be <tt>null</tt> to indicate no cancel units
     * @param groups          a list of <em>lookup.reminderGroup</em>
     * @return a new reminder
     */
    public static Entity createReminderType(int defaultInterval, DateUnits defaultUnits, int cancelInterval,
                                            DateUnits cancelUnits, Lookup... groups) {
        Entity reminder = (Entity) create("entity.reminderType");
        EntityBean bean = new EntityBean(reminder);
        bean.setValue("name", "XReminderType");
        bean.setValue("defaultInterval", defaultInterval);
        bean.setValue("defaultUnits", defaultUnits.toString());
        if (cancelUnits != null) {
            bean.setValue("cancelInterval", cancelInterval);
            bean.setValue("cancelUnits", cancelUnits.toString());
        }
        for (Lookup group : groups) {
            reminder.addClassification(group);
        }
        bean.save();
        return reminder;
    }

    /**
     * Adds a document template to a reminder type.
     *
     * @param reminderType    the reminder type
     * @param reminderCount   the reminder count
     * @param overdueInterval the overdue interval
     * @param overdueUnits    the overdue interval units
     */
    public static void addTemplate(Entity reminderType, int reminderCount, int overdueInterval,
                                   DateUnits overdueUnits) {
        EntityBean bean = new EntityBean(reminderType);
        Entity template = (Entity) create("entity.documentTemplate");
        EntityBean templateBean = new EntityBean(template);
        templateBean.setValue("name", "ZDummyTemplate-" + System.currentTimeMillis());
        templateBean.setValue("archetype", "act.patientDocumentForm");
        templateBean.save();
        EntityRelationship relationship = bean.addRelationship("entityRelationship.reminderTypeTemplate", template);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("reminderCount", reminderCount);
        relBean.setValue("interval", overdueInterval);
        relBean.setValue("units", overdueUnits.toString());
        bean.save();
    }

    /**
     * Creates and saves a new reminder, calculating the due date from the start time and reminder type.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param startTime    the start time
     * @return a new reminder
     */
    public static Act createReminder(Party patient, Entity reminderType, Date startTime) {
        Act reminder = createReminder(patient, reminderType);
        reminder.setActivityStartTime(startTime);
        ReminderRules rules = new ReminderRules();
        rules.calculateReminderDueDate(reminder);
        save(reminder);
        return reminder;
    }

    /**
     * Creates and saves a new reminder with the specified due date.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @param dueDate      the due date
     * @return a new reminder
     */
    public static Act createReminderWithDueDate(Party patient, Entity reminderType, Date dueDate) {
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

    /**
     * Helper to create <tt>count</tt> reminders of a given reminder type.
     *
     * @param count        the no. to create
     * @param reminderType the reminder type
     * @return the reminders
     */
    public static List<Act> createReminders(int count, Entity reminderType) {
        List<Act> result = new ArrayList<Act>();
        Date dueDate = new Date();

        for (int i = 0; i < count; ++i) {
            Party customer = createCustomer();
            Party patient = createPatient(customer);
            Act reminder = createReminderWithDueDate(patient, reminderType, dueDate);
            result.add(reminder);
        }
        return result;
    }
}
