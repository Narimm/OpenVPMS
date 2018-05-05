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

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.patient.reminder.ReminderStatus.CANCELLED;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createAlert;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailRule;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createPrintReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderWithDueDate;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link ReminderRules} class.
 *
 * @author Tim Anderson
 */
public class ReminderRulesTestCase extends ArchetypeServiceTest {

    /**
     * The reminder rules.
     */
    private ReminderRules rules;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeService service = getArchetypeService();
        PracticeRules practiceRules = new PracticeRules(service, null);
        PatientRules patientRules = new PatientRules(practiceRules, service, getLookupService(), null);
        rules = new ReminderRules(service, patientRules);
    }

    /**
     * Tests the {@link ReminderRules#markMatchingRemindersCompleted(Act)}
     * method.
     */
    @Test
    public void testMarkMatchingRemindersCompleted() {
        Lookup group1 = ReminderTestHelper.createReminderGroup();
        Lookup group2 = ReminderTestHelper.createReminderGroup();

        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        // create a reminder for patient1, with an entity.reminderType with
        // no lookup.reminderGroup
        Act reminder0 = createReminder(patient1);
        checkReminder(reminder0, IN_PROGRESS);
        rules.markMatchingRemindersCompleted(reminder0);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it has not changed reminder0
        Act reminder1 = createReminder(patient1, group1);
        rules.markMatchingRemindersCompleted(reminder1);
        checkReminder(reminder1, IN_PROGRESS);
        checkReminder(reminder0, IN_PROGRESS);

        // create a reminder for patient2, with an entity.reminderType with
        // group2 lookup.reminderGroup. Verify it has not changed reminder1
        Act reminder2 = createReminder(patient2, group2);
        rules.markMatchingRemindersCompleted(reminder2);
        checkReminder(reminder2, IN_PROGRESS);
        checkReminder(reminder1, IN_PROGRESS);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it marks reminder1 COMPLETED.
        Act reminder3 = createReminder(patient1, group1);
        rules.markMatchingRemindersCompleted(reminder3);
        checkReminder(reminder3, IN_PROGRESS);
        checkReminder(reminder1, COMPLETED);

        // create a reminder for patient2, with an entity.reminderType with
        // both group1 and group2 lookup.reminderGroup. Verify it marks
        // reminder2 COMPLETED.
        Act reminder4 = createReminder(patient2, group1, group2);
        rules.markMatchingRemindersCompleted(reminder4);
        checkReminder(reminder4, IN_PROGRESS);
        checkReminder(reminder2, COMPLETED);

        // create a reminder type with no group, and create 2 reminders using it.
        Entity reminderType = ReminderTestHelper.createReminderType();
        Act reminder5 = createReminder(patient1, reminderType);
        Act reminder6 = createReminder(patient2, reminderType);
        rules.markMatchingRemindersCompleted(reminder5);
        rules.markMatchingRemindersCompleted(reminder6);
        checkReminder(reminder5, IN_PROGRESS);
        checkReminder(reminder6, IN_PROGRESS);

        // now create a reminder for patient1. Verify it marks reminder5 COMPLETED
        Act reminder7 = createReminder(patient1, reminderType);
        rules.markMatchingRemindersCompleted(reminder7);
        checkReminder(reminder5, COMPLETED);
        checkReminder(reminder6, IN_PROGRESS);
        checkReminder(reminder7, IN_PROGRESS);
    }

    /**
     * Tests the {@link ReminderRules#markMatchingRemindersCompleted(List)} method.
     */
    @Test
    public void testMarkMatchingRemindersCompletedForList() {
        Entity reminderType = ReminderTestHelper.createReminderType();

        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        // create reminders for patient1 and patient2
        Act reminder0 = ReminderTestHelper.createReminder(patient1, reminderType);
        Act reminder1 = ReminderTestHelper.createReminder(patient2, reminderType);
        save(reminder0, reminder1);
        checkReminder(reminder0, IN_PROGRESS);
        checkReminder(reminder1, IN_PROGRESS);

        Act reminder2 = ReminderTestHelper.createReminder(patient1, reminderType);
        Act reminder3 = ReminderTestHelper.createReminder(patient2, reminderType);
        Act reminder3dup = ReminderTestHelper.createReminder(patient2, reminderType); // duplicates reminder3
        final List<Act> reminders = Arrays.asList(reminder2, reminder3, reminder3dup);
        PlatformTransactionManager mgr = (PlatformTransactionManager) applicationContext.getBean("txnManager");
        TransactionTemplate template = new TransactionTemplate(mgr);
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                save(reminders);
                rules.markMatchingRemindersCompleted(reminders);
                return null;
            }
        });

        checkReminder(reminder0, COMPLETED);
        checkReminder(reminder1, COMPLETED);
        checkReminder(reminder2, IN_PROGRESS);
        checkReminder(reminder3, IN_PROGRESS);
        checkReminder(reminder3dup, COMPLETED); // as it duplicates reminder3
    }

    /**
     * Tests the {@link ReminderRules#calculateReminderDueDate(Date, Entity)}
     * method.
     */
    @Test
    public void testCalculateReminderDueDate() {
        checkCalculateReminderDueDate(1, DateUnits.DAYS, "2007-01-01", "2007-01-02");
        checkCalculateReminderDueDate(2, DateUnits.WEEKS, "2007-01-01", "2007-01-15");
        checkCalculateReminderDueDate(2, DateUnits.MONTHS, "2007-01-01", "2007-03-01");
        checkCalculateReminderDueDate(5, DateUnits.YEARS, "2007-01-01", "2012-01-01");
    }

    /**
     * Tests the {@link ReminderRules#calculateProductReminderDueDate} method.
     */
    @Test
    public void testCalculateProductReminderDueDate() {
        checkCalculateProductReminderDueDate(1, DateUnits.DAYS, "2007-01-01", "2007-01-02");
        checkCalculateProductReminderDueDate(2, DateUnits.WEEKS, "2007-01-01", "2007-01-15");
        checkCalculateProductReminderDueDate(2, DateUnits.MONTHS, "2007-01-01", "2007-03-01");
        checkCalculateProductReminderDueDate(5, DateUnits.YEARS, "2007-01-01", "2012-01-01");
    }

    /**
     * Tests the {@link ReminderRules#shouldCancel(Act, Date)} method.
     */
    @Test
    public void testShouldCancel() {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType(
                1, DateUnits.MONTHS, 0, DateUnits.MONTHS, group);
        Date start = java.sql.Date.valueOf("2007-01-01");
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType);
        reminder.setActivityStartTime(start);
        Date due = rules.calculateReminderDueDate(start, reminderType);
        reminder.setActivityStartTime(due);
        reminder.setActivityEndTime(due);

        checkShouldCancel(reminder, "2007-01-01", false);
        checkShouldCancel(reminder, "2007-01-31", false);
        checkShouldCancel(reminder, "2007-02-01", true);

        // Now add a cancel interval to the reminderType, due 2 weeks after the
        // current due date.
        IMObjectBean bean = new IMObjectBean(reminderType);
        bean.setValue("cancelInterval", 2);
        bean.setValue("cancelUnits", DateUnits.WEEKS.toString());
        bean.save();

        checkShouldCancel(reminder, "2007-02-01", false);
        checkShouldCancel(reminder, "2007-02-14", false);
        checkShouldCancel(reminder, "2007-02-15", true);

        // Now set patient to deceased
        EntityBean patientBean = new EntityBean(patient);
        patientBean.setValue("deceased", true);
        patientBean.save();
        checkShouldCancel(reminder, "2007-02-01", true);
    }

    /**
     * Tests the {@link ReminderRules#updateReminder(Act, Act)} method.
     */
    @Test
    public void testUpdateReminder() {
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType(3, DateUnits.MONTHS);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, null, createEmailRule());
        addReminderCount(reminderType, 1, 1, DateUnits.MONTHS, null, createEmailRule());

        Act reminder = createReminder(patient, reminderType, "2016-01-01 00:00:00", IN_PROGRESS);
        Date due = TestHelper.getDate("2016-04-01");
        assertEquals(due, reminder.getActivityStartTime());
        assertEquals(due, reminder.getActivityEndTime());
        Act item1 = createEmailReminder(due, due, ReminderItemStatus.PENDING, 0);
        Act item2 = createPrintReminder(due, due, ReminderItemStatus.PENDING, 0);
        ActBean bean = new ActBean(reminder);
        bean.addNodeRelationship("items", item1);
        bean.addNodeRelationship("items", item2);
        save(reminder, item1, item2);
        assertFalse(rules.updateReminder(reminder, item1));
        item2.setStatus(ReminderItemStatus.COMPLETED);
        save(item2);
        assertTrue(rules.updateReminder(reminder, item1));
        assertEquals(getDate("2016-05-01"), reminder.getActivityStartTime());
    }

    /**
     * Tests the {@link ReminderRules#getDocumentFormReminder} method, when the <em>act.patientDocumentForm</em> is
     * linked to an invoice item.
     */
    @Test
    public void testGetDocumentFormReminderForInvoiceItem() {
        Party patient = TestHelper.createPatient();
        DocumentAct form = PatientTestHelper.createDocumentForm(patient);

        // verify a form not associated with any invoice item nor product returns null
        assertNull(rules.getDocumentFormReminder(form));

        // create an invoice item and associate the form with it
        Act item = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient,
                                                        TestHelper.createProduct(), BigDecimal.ONE);
        ActBean itemBean = new ActBean(item);
        itemBean.addNodeRelationship("documents", form);
        save(item, form);

        // should return null as neither the invoice nor product associated with the form have reminders
        assertNull(rules.getDocumentFormReminder(form));

        // associate a single reminder with the invoice item, and verify it is returned by getDocumentFormReminder()
        Entity reminderType1 = createReminderType();
        Act reminder1 = createReminderWithDueDate(patient, reminderType1, getDate("2012-01-12"));
        itemBean.addNodeRelationship("reminders", reminder1);
        save(item, reminder1);
        assertEquals(reminder1, rules.getDocumentFormReminder(form));

        // associate another reminder with the invoice item, with a closer due date. This should be returned
        Entity reminderType2 = createReminderType();
        Act reminder2 = createReminderWithDueDate(patient, reminderType2, getDate("2012-01-11"));
        itemBean.addNodeRelationship("reminders", reminder2);
        save(item, reminder2);
        assertEquals(reminder2, rules.getDocumentFormReminder(form));

        // associate another reminder with the invoice item, with the same due date. The reminder with the lower id
        // should be returned
        Entity reminderType3 = createReminderType();
        Act reminder3 = createReminderWithDueDate(patient, reminderType3, getDate("2012-01-11"));
        itemBean.addNodeRelationship("reminders", reminder3);
        save(item, reminder3);
        assertEquals(reminder2, rules.getDocumentFormReminder(form));
    }

    /**
     * Tests the {@link ReminderRules#getDocumentFormReminder} method, when the <em>act.patientDocumentForm</em> is
     * linked to product with reminder types.
     */
    @Test
    public void testGetDocumentFormReminderForProduct() {
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();
        DocumentAct form = PatientTestHelper.createDocumentForm(patient, product);

        // verify a form not associated with a product with reminders returns null
        assertNull(rules.getDocumentFormReminder(form));

        EntityBean productBean = new EntityBean(product);
        Entity reminderType1 = createReminderType();
        EntityRelationship productReminder1 = productBean.addNodeRelationship("reminders", reminderType1);
        IMObjectBean prodReminder1Bean = new IMObjectBean(productReminder1);
        prodReminder1Bean.setValue("period", 2); // due date will fall 2 years from start time

        save(product, reminderType1);

        Act reminder1 = rules.getDocumentFormReminder(form);
        assertNotNull(reminder1);
        assertTrue(reminder1.isNew()); // reminders from products should not be persistent
        Date dueDate1 = rules.calculateProductReminderDueDate(form.getActivityStartTime(), productReminder1);
        checkReminder(reminder1, reminderType1, patient, product, dueDate1);

        Entity reminderType2 = createReminderType();
        EntityRelationship productReminder2 = productBean.addNodeRelationship("reminders", reminderType2);
        save(product, reminderType2);

        Date dueDate2 = rules.calculateProductReminderDueDate(form.getActivityStartTime(), productReminder2);
        assertTrue(dueDate2.compareTo(dueDate1) < 0);

        Act reminder2 = rules.getDocumentFormReminder(form);
        assertNotNull(reminder2);
        assertTrue(reminder2.isNew()); // reminders from products should not be persistent
        checkReminder(reminder2, reminderType2, patient, product, dueDate2);
    }

    /**
     * Tests the {@link ReminderRules#getDocumentFormReminder} method to verify that the reminder associated with an
     * invoice item is returned in preference to one created from a product's reminder types.
     */
    @Test
    public void testGetDocumentFormReminderForInvoiceAndProduct() {
        Product product = TestHelper.createProduct();
        EntityBean productBean = new EntityBean(product);
        Entity reminderType1 = createReminderType();
        EntityRelationship relationship = productBean.addNodeRelationship("reminders", reminderType1);
        save(product, reminderType1);

        Party patient = TestHelper.createPatient();
        DocumentAct form = PatientTestHelper.createDocumentForm(patient, product);

        Date dueDate1 = rules.calculateProductReminderDueDate(form.getActivityStartTime(), relationship);
        Act reminder1 = rules.getDocumentFormReminder(form);
        assertNotNull(reminder1);
        assertTrue(reminder1.isNew()); // reminders from products should not be persistent
        checkReminder(reminder1, reminderType1, patient, product, dueDate1);

        // create an invoice item and associate the form with it
        Act item = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient, product, Money.ONE
        );
        ActBean itemBean = new ActBean(item);
        itemBean.addNodeRelationship("documents", form);
        save(item, form);

        // associate a single reminder with the invoice item, and verify it is returned by getDocumentFormReminder()
        Entity reminderType2 = createReminderType();
        Act reminder2 = createReminderWithDueDate(patient, reminderType2, getDate("2012-01-12"));
        itemBean.addNodeRelationship("reminders", reminder2);
        save(item, reminder2);
        assertEquals(reminder2, rules.getDocumentFormReminder(form));
    }

    /**
     * Tests the {@link ReminderRules#getDueState(Act, Date)} method.
     */
    @Test
    public void testGetDueState() {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType(1, DateUnits.MONTHS, group);
        Date start = getDate("2012-01-01");
        Date due = rules.calculateReminderDueDate(start, reminderType);
        Act reminder = createReminderWithDueDate(patient, reminderType, due);

        assertEquals(ReminderRules.DueState.NOT_DUE, rules.getDueState(reminder, getDate("2012-01-01")));
        assertEquals(ReminderRules.DueState.NOT_DUE, rules.getDueState(reminder, getDate("2012-01-31")));
        assertEquals(ReminderRules.DueState.DUE, rules.getDueState(reminder, getDate("2012-02-01")));
        assertEquals(ReminderRules.DueState.OVERDUE, rules.getDueState(reminder, getDate("2012-02-02")));

        // change the sensitivity from the default (0 DAYS)
        IMObjectBean bean = new IMObjectBean(reminderType);
        bean.setValue("sensitivityUnits", DateUnits.DAYS.toString());
        bean.setValue("sensitivityInterval", 5);
        bean.save();

        assertEquals(ReminderRules.DueState.NOT_DUE, rules.getDueState(reminder, getDate("2012-01-01")));
        assertEquals(ReminderRules.DueState.DUE, rules.getDueState(reminder, getDate("2012-01-27")));
        assertEquals(ReminderRules.DueState.DUE, rules.getDueState(reminder, getDate("2012-02-01")));
        assertEquals(ReminderRules.DueState.DUE, rules.getDueState(reminder, getDate("2012-02-06")));
        assertEquals(ReminderRules.DueState.OVERDUE, rules.getDueState(reminder, getDate("2012-02-07")));
    }

    /**
     * Verifies that if a entityRelationship.productReminder is missing the periodUom, it is treated as YEARS.
     */
    @Test
    public void testCalculateProductReminderDueDateForMissingPeriodUOM() {
        Product product = TestHelper.createProduct();
        EntityBean productBean = new EntityBean(product);
        Entity reminderType = createReminderType();
        EntityRelationship productReminder = productBean.addNodeRelationship("reminders", reminderType);
        IMObjectBean bean = new IMObjectBean(productReminder);

        bean.setValue("period", 1);
        bean.setValue("periodUom", "MONTHS");

        Date start = getDate("2015-03-25");
        Date due1 = rules.calculateProductReminderDueDate(start, productReminder);
        assertEquals(getDate("2015-04-25"), due1);

        bean.setValue("periodUom", null);

        Date due2 = rules.calculateProductReminderDueDate(start, productReminder);
        assertEquals(getDate("2016-03-25"), due2);
    }

    /**
     * Tests the {@link ReminderRules#getReminders(Party, Date, Date)} method.
     */
    @Test
    public void testGetReminders() {
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType();
        Act reminder1 = createReminder(patient, reminderType, "2016-04-13 11:59:59", IN_PROGRESS);
        Act reminder2 = createReminder(patient, reminderType, "2016-04-14 10:10:10", IN_PROGRESS);
        Act reminder3 = createReminder(patient, reminderType, "2016-04-14 11:10:10", COMPLETED);
        Act reminder4 = createReminder(patient, reminderType, "2016-04-15 10:10:10", CANCELLED);
        Act reminder5 = createReminder(patient, reminderType, "2016-04-15 11:00:00", IN_PROGRESS);

        List<Act> acts = getActs(rules.getReminders(patient, getDatetime("2016-04-14 10:00:00"),
                                                    getDatetime("2016-04-15 11:00:00")));
        assertEquals(3, acts.size());
        assertFalse(acts.contains(reminder1));
        assertTrue(acts.contains(reminder2));
        assertTrue(acts.contains(reminder3));
        assertTrue(acts.contains(reminder4));
        assertFalse(acts.contains(reminder5));
    }

    /**
     * Tests the {@link ReminderRules#getReminders(Party, String, Date, Date)} method.
     */
    @Test
    public void testGetRemindersForProductType() {
        Party patient = TestHelper.createPatient();
        Entity productType1 = ProductTestHelper.createProductType("Z Vaccination 1");
        Entity productType2 = ProductTestHelper.createProductType("Z Vaccination 2");
        Product product1 = ProductTestHelper.createMedication(productType1);
        Product product2 = ProductTestHelper.createMedication(productType2);
        Product product3 = TestHelper.createProduct();
        Entity reminderType = ReminderTestHelper.createReminderType();
        Act reminder1 = createReminder(patient, reminderType, product1, "2016-04-13 11:59:59", IN_PROGRESS);
        Act reminder2 = createReminder(patient, reminderType, product2, "2016-04-14 10:10:10", IN_PROGRESS);
        Act reminder3 = createReminder(patient, reminderType, product1, "2016-04-14 11:10:10", COMPLETED);
        Act reminder4 = createReminder(patient, reminderType, product3, "2016-04-15 10:10:10", CANCELLED);
        Act reminder5 = createReminder(patient, reminderType, product1, "2016-04-15 11:00:00", IN_PROGRESS);

        List<Act> acts1 = getActs(rules.getReminders(patient, productType1.getName(), getDatetime("2016-04-14 10:00:00"),
                                                     getDatetime("2016-04-15 11:00:00")));
        assertEquals(1, acts1.size());
        assertFalse(acts1.contains(reminder1));
        assertFalse(acts1.contains(reminder2));
        assertTrue(acts1.contains(reminder3));
        assertFalse(acts1.contains(reminder4));
        assertFalse(acts1.contains(reminder5));

        List<Act> acts2 = getActs(rules.getReminders(patient, productType2.getName(), getDatetime("2016-04-14 10:00:00"),
                                                     getDatetime("2016-04-15 11:00:00")));
        assertEquals(1, acts2.size());
        assertTrue(acts2.contains(reminder2));

        List<Act> acts3 = getActs(rules.getReminders(patient, "Z Vacc*", getDatetime("2016-04-14 10:00:00"),
                                                     getDatetime("2016-04-15 11:00:00")));
        assertEquals(2, acts3.size());
        assertTrue(acts3.contains(reminder2));
        assertTrue(acts3.contains(reminder3));
    }

    /**
     * Tests the {@link ReminderRules#markMatchingAlertsCompleted(Act)} method.
     */
    @Test
    public void testMarkMatchingAlertsCompleted() {
        Entity alertTypeA = ReminderTestHelper.createAlertType("Z Alert A");
        Entity alertTypeB = ReminderTestHelper.createAlertType("Z Alert B");
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        // create an alert for patient1, and mark matching alerts completed. The alert should still be IN_PROGRESS
        Act alert0 = createAlert(patient1, alertTypeA);
        rules.markMatchingAlertsCompleted(alert0);
        checkAlert(alert0, IN_PROGRESS);

        // create another alert for patient1, with a different reminder type. Verify it has not changed alert0
        Act alert1 = createAlert(patient1, alertTypeB);
        rules.markMatchingAlertsCompleted(alert1);
        checkAlert(alert1, IN_PROGRESS);
        checkAlert(alert0, IN_PROGRESS);

        // create an alert for patient2. Marking matching alerts completed should not affect patient1 alerts
        Act alert2 = createAlert(patient2, alertTypeA);
        rules.markMatchingAlertsCompleted(alert2);
        checkAlert(alert2, IN_PROGRESS);
        checkAlert(alert1, IN_PROGRESS);
        checkAlert(alert0, IN_PROGRESS);

        // create another alert for patient1 for alertB. Verify it marks reminder1 COMPLETED.
        Act alert3 = createAlert(patient1, alertTypeB);
        rules.markMatchingAlertsCompleted(alert3);
        checkAlert(alert3, IN_PROGRESS);
        checkAlert(alert1, COMPLETED);
    }

    /**
     * Tests the {@link ReminderRules#markMatchingAlertsCompleted(List)} method.
     */
    @Test
    public void testMarkMatchingAlertsCompletedForList() {
        Entity alertType = ReminderTestHelper.createAlertType("Z Alert");

        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        // create alerts for patient1 and patient2
        Act alert0 = ReminderTestHelper.createAlert(patient1, alertType);
        Act alert1 = ReminderTestHelper.createAlert(patient2, alertType);
        save(alert0, alert1);

        Act alert2 = ReminderTestHelper.createAlert(patient1, alertType);
        Act alert3 = ReminderTestHelper.createAlert(patient2, alertType);
        Act alert3dup = ReminderTestHelper.createAlert(patient2, alertType); // duplicates alert3
        final List<Act> alerts = Arrays.asList(alert2, alert3, alert3dup);
        PlatformTransactionManager mgr = (PlatformTransactionManager) applicationContext.getBean("txnManager");
        TransactionTemplate template = new TransactionTemplate(mgr);
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                rules.markMatchingAlertsCompleted(alerts);
                return null;
            }
        });

        checkAlert(alert0, COMPLETED);
        checkAlert(alert1, COMPLETED);
        checkAlert(alert2, IN_PROGRESS);
        checkAlert(alert3, IN_PROGRESS);
        checkAlert(alert3dup, COMPLETED); // as it duplicates alert3
    }

    /**
     * Helper to convert an iterable of acts to a list.
     *
     * @param acts the acts
     * @return the list of acts
     */
    private List<Act> getActs(Iterable<Act> acts) {
        List<Act> result = new ArrayList<>();
        CollectionUtils.addAll(result, acts);
        return result;
    }

    /**
     * Creates a reminder.
     *
     * @param patient      the reminder
     * @param reminderType the reminder type
     * @param startTime    the start time
     * @param status       the status
     * @return a new reminder
     */
    private Act createReminder(Party patient, Entity reminderType, String startTime, String status) {
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType, TestHelper.getDatetime(startTime));
        reminder.setStatus(status);
        save(reminder);
        return reminder;
    }

    /**
     * Creates a reminder.
     *
     * @param patient      the reminder
     * @param reminderType the reminder type
     * @param product      the product
     * @param startTime    the start time
     * @param status       the status
     * @return a new reminder
     */
    private Act createReminder(Party patient, Entity reminderType, Product product, String startTime, String status) {
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType, TestHelper.getDatetime(startTime));
        ActBean bean = new ActBean(reminder);
        bean.addNodeParticipation("product", product);
        reminder.setStatus(status);
        save(reminder);
        return reminder;
    }

    /**
     * Verifies a reminder matches that expected.
     *
     * @param reminder     the reminder
     * @param reminderType the expected reminder type
     * @param patient      the expected patient
     * @param product      the expected product
     * @param dueDate      the expected due date
     */
    private void checkReminder(Act reminder, Entity reminderType, Party patient, Product product, Date dueDate) {
        ActBean bean = new ActBean(reminder);
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(reminderType, bean.getNodeParticipant("reminderType"));
        assertEquals(product, bean.getNodeParticipant("product"));
        assertEquals(dueDate, reminder.getActivityStartTime());
        assertEquals(dueDate, reminder.getActivityEndTime());
    }

    /**
     * Helper to create a reminder.
     *
     * @param patient the patient
     * @param groups  the reminder group classifications
     * @return a new reminder
     */
    private Act createReminder(Party patient, Lookup... groups) {
        Entity reminderType = ReminderTestHelper.createReminderType(groups);
        return createReminder(patient, reminderType);
    }

    /**
     * Helper to create a reminder.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @return a new reminder
     */
    private Act createReminder(Party patient, Entity reminderType) {
        return createReminderWithDueDate(patient, reminderType, new Date());
    }

    /**
     * Verifies a reminder has the expected state.
     * For COMPLETED status, checks that the 'completedDate' node is non-null.
     *
     * @param reminder the reminder
     * @param status   the expected reminder status
     */
    private void checkReminder(Act reminder, String status) {
        reminder = get(reminder);
        assertNotNull(reminder);
        assertEquals(status, reminder.getStatus());
        ActBean bean = new ActBean(reminder);
        Date date = bean.getDate("completedDate");
        if (COMPLETED.equals(status)) {
            assertNotNull(date);
        } else {
            assertNull(date);
        }
    }

    /**
     * Verifies an alert has the expected state.
     * For COMPLETED status, checks that the 'endTime' node is non-null.
     *
     * @param alert  the reminder
     * @param status the expected alert status
     */
    private void checkAlert(Act alert, String status) {
        alert = get(alert);
        assertNotNull(alert);
        assertEquals(status, alert.getStatus());
        if (COMPLETED.equals(status)) {
            assertNotNull(alert.getActivityEndTime());
        }
    }

    /**
     * Checks the {@link ReminderRules#calculateReminderDueDate(Date, Entity)}
     * method.
     *
     * @param defaultInterval the default reminder interval
     * @param defaultUnits    the interval units
     * @param startDate       the reminder start date
     * @param expectedDate    the expected due date
     */
    private void checkCalculateReminderDueDate(int defaultInterval,
                                               DateUnits defaultUnits,
                                               String startDate,
                                               String expectedDate) {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Entity reminderType = ReminderTestHelper.createReminderType(
                defaultInterval, defaultUnits, group);
        Date start = java.sql.Date.valueOf(startDate);
        Date expected = java.sql.Date.valueOf(expectedDate);
        Date to = rules.calculateReminderDueDate(start, reminderType);
        assertEquals(expected, to);
    }

    /**
     * Checks the {@link ReminderRules#calculateProductReminderDueDate} method.
     *
     * @param period       the reminder interval
     * @param units        the interval units
     * @param startDate    the reminder start date
     * @param expectedDate the expected due date
     */
    private void checkCalculateProductReminderDueDate(int period, DateUnits units, String startDate,
                                                      String expectedDate) {
        EntityRelationship relationship = (EntityRelationship) create("entityRelationship.productReminder");
        IMObjectBean bean = new IMObjectBean(relationship);
        bean.setValue("period", period);
        bean.setValue("periodUom", units.toString());
        Date start = getDate(startDate);
        Date expected = getDate(expectedDate);
        Date to = rules.calculateProductReminderDueDate(start, relationship);
        assertEquals(expected, to);
    }

    /**
     * Checks if a reminder should be cancelled using
     * {@link ReminderRules#shouldCancel(Act, Date)}.
     *
     * @param reminder the reminder
     * @param date     the date
     * @param expected the expected shouldCancel result
     */
    private void checkShouldCancel(Act reminder, String date,
                                   boolean expected) {
        assertEquals(expected, rules.shouldCancel(reminder,
                                                  java.sql.Date.valueOf(date)));
    }

}
