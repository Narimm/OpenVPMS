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

package org.openvpms.archetype.rules.patient.reminder;

import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.patient.reminder.ReminderStatus.CANCELLED;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createExportReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createPrintReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSReminder;
import static org.openvpms.archetype.test.TestHelper.createPatient;

/**
 * Tests the {@link ReminderQueueQueryFactory} class.
 *
 * @author Tim Anderson
 */
public class ReminderQueueQueryFactoryTestCase extends ArchetypeServiceTest {

    /**
     * Tests the behaviour of the query for reminders with no reminder items.
     */
    @Test
    public void testRemindersWithNoItems() {
        ReminderQueueQueryFactory factory = new ReminderQueueQueryFactory();
        Date date = DateRules.getToday();
        Entity reminderType = createReminderType(1, DateUnits.YEARS);
        Party customer = TestHelper.createCustomer();
        Party patient = createPatient(customer);

        Act reminder1 = createReminder(date, patient, reminderType, IN_PROGRESS);
        Act reminder2 = createReminder(date, patient, reminderType, CANCELLED);
        Act reminder3 = createReminder(date, patient, reminderType, COMPLETED);

        checkExists(factory, DateRules.getTomorrow(), reminder1);
        checkNotExists(factory, DateRules.getTomorrow(), reminder2, reminder3);
        checkNotExists(factory, DateRules.getToday(), reminder1, reminder2, reminder3);
    }

    /**
     * Tests the behaviour of the query for reminders with reminder items.
     */
    @Test
    public void testRemindersWithItems() {
        ReminderQueueQueryFactory factory = new ReminderQueueQueryFactory();
        Date date = DateRules.getToday();
        Entity reminderType = createReminderType(1, DateUnits.YEARS);
        Party customer = TestHelper.createCustomer();
        Party patient = createPatient(customer);

        Act email1 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act sms1 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder1 = createReminder(date, patient, reminderType, IN_PROGRESS, email1, sms1);

        Act email2 = createSMSReminder(date, date, ReminderItemStatus.ERROR, 0);
        Act sms2 = createSMSReminder(date, date, ReminderItemStatus.ERROR, 0);
        Act reminder2 = createReminder(date, patient, reminderType, IN_PROGRESS, email2, sms2);

        Act email3 = createPrintReminder(date, date, ReminderItemStatus.COMPLETED, 0);
        Act print3 = createPrintReminder(date, date, ReminderItemStatus.COMPLETED, 0);
        Act reminder3 = createReminder(date, patient, reminderType, IN_PROGRESS, email3, print3);

        Act email4 = createExportReminder(date, date, ReminderItemStatus.CANCELLED, 0);
        Act export4 = createExportReminder(date, date, ReminderItemStatus.CANCELLED, 0);
        Act reminder4 = createReminder(date, patient, reminderType, IN_PROGRESS, email4, export4);

        Act email5 = createExportReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act sms5 = createExportReminder(date, date, ReminderItemStatus.COMPLETED, 0);
        Act reminder5 = createReminder(date, patient, reminderType, IN_PROGRESS, email5, sms5);

        checkNotExists(factory, DateRules.getTomorrow(), reminder1, reminder2, reminder5);
        checkExists(factory, DateRules.getTomorrow(), reminder3, reminder4);
        checkNotExists(factory, DateRules.getToday(), reminder1, reminder2, reminder3, reminder4, reminder5);
    }

    /**
     * Verifies that the reminders returned by the query match those specified.
     *
     * @param factory   the query factory
     * @param date      the date
     * @param reminders the expected reminders
     */
    private void checkExists(ReminderQueueQueryFactory factory, Date date, Act... reminders) {
        int matches = getMatches(factory, date, reminders);
        assertEquals(reminders.length, matches);
    }

    /**
     * Verifies that no reminders returned by the query match those specified.
     *
     * @param factory   the query factory
     * @param date      the date
     * @param reminders the reminders that shouldn't be matched
     */
    private void checkNotExists(ReminderQueueQueryFactory factory, Date date, Act... reminders) {
        int matches = getMatches(factory, date, reminders);
        assertEquals(0, matches);
    }

    /**
     * Returns the number of reminders that match those specified.
     *
     * @param factory   the query factory
     * @param date      the date
     * @param reminders the reminders to match
     * @return the no. of matches
     */
    private int getMatches(ReminderQueueQueryFactory factory, Date date, Act[] reminders) {
        int matches = 0;
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(factory.createQuery(date));
        Set<Act> set = new HashSet<>(Arrays.asList(reminders));
        while (iterator.hasNext()) {
            Act actual = iterator.next();
            if (set.contains(actual)) {
                matches++;
            }
        }
        return matches;
    }

}
