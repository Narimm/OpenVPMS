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

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Date;


/**
 * Tests the {@link ReminderRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderRulesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ReminderRules#markMatchingRemindersCompleted(Act)}
     * method, when invoked via the
     * <em>archetypeService.save.act.patientReminder.before</em> rule.
     */
    public void testMarkMatchingRemindersCompleted() {
        Lookup group1 = ReminderTestHelper.createReminderGroup();
        Lookup group2 = ReminderTestHelper.createReminderGroup();

        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        // create a reminder for patient1, with an entity.reminderType with
        // no lookup.reminderGroup
        Act reminder0 = createReminder(patient1);
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it has not changed
        // reminder0
        Act reminder1 = createReminder(patient1, group1);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS, true);

        // create a reminder for patient2, with an entity.reminderType with
        // group2 lookup.reminderGroup. Verify it has not changed
        // reminder1
        Act reminder2 = createReminder(patient2, group2);
        checkReminder(reminder2, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS, true);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it marks reminder1
        // COMPLETED.
        Act reminder3 = createReminder(patient1, group1);
        checkReminder(reminder3, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.COMPLETED, true);

        // create a reminder for patient2, with an entity.reminderType with
        // both group1 and group2 lookup.reminderGroup. Verify it marks
        // reminder2 COMPLETED.
        Act reminder4 = createReminder(patient2, group1, group2);
        checkReminder(reminder4, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder2, ReminderStatus.COMPLETED, true);
    }

    /**
     * Helper to create a reminder.
     *
     * @param patient the patient
     * @param groups  the reminder group classifications
     * @return a new reminder
     */
    private Act createReminder(Party patient, Lookup ... groups) {
        Entity reminderType = ReminderTestHelper.createReminderType(groups);
        return ReminderTestHelper.createReminder(patient, reminderType,
                                                 new Date());
    }

    /**
     * Verifies a reminder has the expected state.
     * For COMPLETED status, checks that the 'completedDate' node is non-null.
     *
     * @param reminder the reminder
     * @param status   the expected reminder status
     */
    private void checkReminder(Act reminder, String status) {
        checkReminder(reminder, status, false);
    }

    /**
     * Verifies a reminder has the expected state.
     * For COMPLETED status, checks that the 'completedDate' node is non-null.
     *
     * @param reminder the reminder
     * @param status   the expected reminder status
     * @param reload   if <code>true</code>, reload the act before checking
     */
    private void checkReminder(Act reminder, String status,
                               boolean reload) {
        if (reload) {
            reminder = (Act) get(reminder);
            assertNotNull(reminder);
        }
        assertEquals(status, reminder.getStatus());
        ActBean bean = new ActBean(reminder);
        Date date = bean.getDate("completedDate");
        if (ReminderStatus.COMPLETED.equals(status)) {
            assertNotNull(date);
        } else {
            assertNull(date);
        }
    }
}
