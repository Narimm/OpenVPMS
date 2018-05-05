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

package org.openvpms.web.jobs.reminder;

import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailRule;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;

/**
 * Tests the {@link PatientReminderQueueJob}.
 *
 * @author Tim Anderson
 */
public class PatientReminderQueueJobTestCase extends ArchetypeServiceTest {

    /**
     * The practice rules.
     */
    @Autowired
    PracticeRules practiceRules;

    /**
     * The patient rules.
     */
    @Autowired
    PatientRules patientRules;

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * The reminder queueing job.
     */
    private PatientReminderQueueJob job;

    /**
     * The lead times.
     */
    private ReminderConfiguration config;

    /**
     * The reminder type.
     */
    private Entity reminderType;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Entity configuration = (Entity) create(PatientReminderQueueJob.JOB_SHORT_NAME);
        final IArchetypeRuleService service = (IArchetypeRuleService) getArchetypeService();

        reminderType = createReminderType(1, DateUnits.YEARS, 2, DateUnits.YEARS);
        config = new ReminderConfiguration(create(ReminderArchetypes.CONFIGURATION), service);

        // lock records after 36 hours
        PracticeService practiceService = new PracticeService(service, practiceRules, null);
        job = new PatientReminderQueueJob(configuration, service, practiceService, patientRules, transactionManager) {
            @Override
            protected ReminderConfiguration getConfiguration(Party practice) {
                return config;
            }


            /**
             * Creates a new {@link ReminderProcessor}.
             *
             * @param date       process reminders due on or before this date
             * @param config     the reminder configuration
             * @param disableSMS if {@code true}, disable SMS
             * @return a new {@link ReminderProcessor}
             */
            @Override
            protected ReminderProcessor createProcessor(Date date, ReminderConfiguration config, boolean disableSMS) {
                return new ReminderProcessor(date, config, false, service, patientRules) {
                    @Override
                    protected Date now() {
                        // exclude any time component, so that date comparisons are predictable
                        return DateRules.getToday();
                    }
                };
            }
        };

    }

    /**
     * Verifies that a <em>act.patientReminderItemEmail</em> is created when a rule specifies email and the customer
     * has an email contact.
     */
    @Test
    public void testQueueEmail() {
        assertEquals(Period.days(3), config.getEmailPeriod());
        // need to allow 3 days prior to a reminder being due, for emails

        Date today = DateRules.getToday();
        Date threeFromToday = DateRules.getDate(today, 3, DateUnits.DAYS);
        Date fourFromToday = DateRules.getDate(today, 4, DateUnits.DAYS);

        Party customer1 = TestHelper.createCustomer(TestHelper.createEmailContact("foo@bar.com", true, "REMINDER"));
        Party patient = TestHelper.createPatient(customer1);

        Entity documentTemplate = ReminderTestHelper.createDocumentTemplate(true, false);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, documentTemplate, createEmailRule());
        Act reminder1 = createReminder(threeFromToday, patient, reminderType, ReminderStatus.IN_PROGRESS);
        Act reminder2 = createReminder(fourFromToday, patient, reminderType, ReminderStatus.IN_PROGRESS);
        job.execute(null);

        reminder1 = get(reminder1);
        reminder2 = get(reminder2);

        checkEmailItem(reminder1, config.getEmailSendDate(threeFromToday), ReminderItemStatus.PENDING);
        checkEmailItem(reminder2, config.getEmailSendDate(fourFromToday), ReminderItemStatus.PENDING);
    }

    /**
     * Verifies that when a customer has no contacts, or no matching contacts,
     * an <em>act.patientReminderItemList</em> is created.
     */
    @Test
    public void testListForCustomerWithNoContacts() {
        Party customer1 = TestHelper.createCustomer(new Contact[0]); // no contacts
        Party patient1 = TestHelper.createPatient(customer1);
        Party customer2 = TestHelper.createCustomer(TestHelper.createPhoneContact("03", "123456")); // no email contact
        Party patient2 = TestHelper.createPatient(customer2);
        Date today = DateRules.getToday();
        Date due = DateRules.getNextDate(DateRules.plus(today, config.getListPeriod()));

        Entity documentTemplate = ReminderTestHelper.createDocumentTemplate(true, true);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, documentTemplate, createEmailRule());
        Act reminder1 = createReminder(due, patient1, reminderType, ReminderStatus.IN_PROGRESS);
        Act reminder2 = createReminder(due, patient2, reminderType, ReminderStatus.IN_PROGRESS);
        job.execute(null);

        reminder1 = get(reminder1);
        reminder2 = get(reminder2);
        Date sendDate = config.getListSendDate(due);
        checkListItem(reminder1, sendDate, ReminderItemStatus.ERROR);
        checkListItem(reminder2, sendDate, ReminderItemStatus.ERROR);
    }

    /**
     * Verifies that when a reminder has existing items with PENDING or ERROR status, it is not processed again.
     */
    @Test
    public void testReminderWithPendingOrErrorItemsNotProcessed() {
        Date today = DateRules.getToday();
        Date fourFromToday = DateRules.getDate(today, 4, DateUnits.DAYS);

        Party customer1 = TestHelper.createCustomer(TestHelper.createEmailContact("foo@bar.com", true, "REMINDER"));
        Party patient = TestHelper.createPatient(customer1);

        Entity documentTemplate = ReminderTestHelper.createDocumentTemplate(true, false);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, documentTemplate, createEmailRule());
        Act reminder1 = createReminder(fourFromToday, patient, reminderType, ReminderStatus.IN_PROGRESS,
                                       createEmailReminder(DateRules.getTomorrow(), fourFromToday,
                                                           ReminderItemStatus.PENDING, 0));
        Act reminder2 = createReminder(fourFromToday, patient, reminderType, ReminderStatus.IN_PROGRESS,
                                       createEmailReminder(DateRules.getTomorrow(), fourFromToday,
                                                           ReminderItemStatus.ERROR, 0));
        Act reminder3 = createReminder(fourFromToday, patient, reminderType, ReminderStatus.IN_PROGRESS,
                                       createEmailReminder(DateRules.getTomorrow(), fourFromToday,
                                                           ReminderItemStatus.COMPLETED, 0));
        Act reminder4 = createReminder(fourFromToday, patient, reminderType, ReminderStatus.IN_PROGRESS,
                                       createEmailReminder(DateRules.getTomorrow(), fourFromToday,
                                                           ReminderItemStatus.CANCELLED, 0));
        job.execute(null);

        checkItems(reminder1, 1);
        checkItems(reminder2, 1);
        checkItems(reminder3, 2); // NOTE: for reminder3 and reminder4, the reminder count should be incremented on
        checkItems(reminder4, 2); // the reminder to avoid re-processing
    }

    /**
     * Verifies that reminders whose due date are passed the cancel interval are cancelled.
     */
    @Test
    public void testCancelReminderForPastDue() {
        Party customer1 = TestHelper.createCustomer(TestHelper.createEmailContact("foo@bar.com", true, "REMINDER"));
        Party patient = TestHelper.createPatient(customer1);

        // create a reminder due over 2 years ago. This is prior to the 2 year cancellation interval of the reminder
        // type
        Act reminder = createReminder(DateRules.getDate(DateRules.getYesterday(), -2, DateUnits.YEARS),
                                      patient, reminderType);
        job.execute(null);
        reminder = get(reminder);
        assertEquals(ReminderStatus.CANCELLED, reminder.getStatus());
        checkItems(reminder, 0);
    }

    /**
     * Verifies that reminders for deceased patients are set to cancel.
     */
    @Test
    public void testCancelForDeceased() {
        Date today = DateRules.getToday();
        Date fourFromToday = DateRules.getDate(today, 4, DateUnits.DAYS);

        Party customer1 = TestHelper.createCustomer(TestHelper.createEmailContact("foo@bar.com", true, "REMINDER"));
        Party patient = TestHelper.createPatient(customer1);
        patientRules.setDeceased(patient);

        Act reminder = createReminder(fourFromToday, patient, reminderType, ReminderStatus.IN_PROGRESS);
        job.execute(null);

        reminder = get(reminder);
        assertEquals(ReminderStatus.CANCELLED, reminder.getStatus());
        checkItems(reminder, 0);
    }

    /**
     * Verifies that if a reminder has a reminder type with no reminder count, it is skipped.
     */
    @Test
    public void testSkipReminderWithNoReminderCount() {
        assertEquals(Period.days(3), config.getEmailPeriod());
        // need to allow 3 days prior to a reminder being due, for emails

        Date today = DateRules.getToday();
        Date send = DateRules.getDate(today, 3, DateUnits.DAYS);

        Party customer1 = TestHelper.createCustomer(TestHelper.createEmailContact("foo@bar.com", true, "REMINDER"));
        Party patient = TestHelper.createPatient(customer1);

        // set up 2 reminders:
        // reminder1 - has reminderCount=0 and should have an email item generated when the job runs
        // reminder2 - has reminderCount=1 and should be skipped when the job runs
        Entity documentTemplate = ReminderTestHelper.createDocumentTemplate(true, false);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, documentTemplate, createEmailRule());
        Act reminder1 = createReminder(send, patient, reminderType, ReminderStatus.IN_PROGRESS);
        Act reminder2 = createReminder(send, patient, reminderType, ReminderStatus.IN_PROGRESS);
        ActBean bean = new ActBean(reminder2);
        bean.setValue("reminderCount", 1);
        bean.save();
        job.execute(null);

        // check the reminders
        reminder1 = get(reminder1);
        reminder2 = get(reminder2);

        checkEmailItem(reminder1, config.getEmailSendDate(send), ReminderItemStatus.PENDING);
        assertEquals(ActStatus.IN_PROGRESS, reminder2.getStatus());
        bean = new ActBean(reminder2);
        assertEquals(0, bean.getNodeActs("items").size());
    }

    /**
     * Verifies that a reminder has the expected number of items.
     *
     * @param reminder the reminder
     * @param count    the expected number of items
     */
    private void checkItems(Act reminder, int count) {
        reminder = get(reminder);
        ActBean bean = new ActBean(reminder);
        assertEquals(count, bean.getNodeActs("items").size());
    }

    /**
     * Verifies a reminder has an email item.
     *
     * @param reminder the reminder
     * @param sendDate the expected item send date
     * @param status   the expected item status
     */
    private void checkEmailItem(Act reminder, Date sendDate, String status) {
        checkItem(reminder, ReminderArchetypes.EMAIL_REMINDER, sendDate, status);
    }

    /**
     * Verifies a reminder has a list item.
     *
     * @param reminder the reminder
     * @param sendDate the expected item send date
     * @param status   the expected item status
     */
    private void checkListItem(Act reminder, Date sendDate, String status) {
        checkItem(reminder, ReminderArchetypes.LIST_REMINDER, sendDate, status);
    }

    /**
     * Verifies a reminder has an item.
     *
     * @param reminder  the reminder
     * @param shortName the expected item archetype short name
     * @param sendDate  the expected item send date
     * @param status    the expected item status
     */
    private Act checkItem(Act reminder, String shortName, Date sendDate, String status) {
        Act result = null;
        ActBean bean = new ActBean(reminder);
        List<Act> items = bean.getNodeActs("items");
        assertFalse(items.isEmpty());
        int found = 0;
        for (Act item : items) {
            if (TypeHelper.isA(item, shortName)) {
                found++;
                assertEquals(0, DateRules.compareTo(sendDate, item.getActivityStartTime()));
                assertEquals(reminder.getActivityEndTime(), item.getActivityEndTime()); // same due dates
                assertEquals(status, item.getStatus());
                result = item;
            }
        }
        assertEquals(1, found);
        return result;
    }

}
