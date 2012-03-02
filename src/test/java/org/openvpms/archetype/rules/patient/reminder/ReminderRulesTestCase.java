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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderWithDueDate;
import static org.openvpms.archetype.test.TestHelper.getDate;


/**
 * Tests the {@link ReminderRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderRulesTestCase extends ArchetypeServiceTest {

    /**
     * The reminder rules.
     */
    private ReminderRules rules;


    /**
     * Tests the {@link ReminderRules#markMatchingRemindersCompleted(Act)}
     * method, when invoked via the
     * <em>archetypeService.save.act.patientReminder.before</em> rule.
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
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it has not changed reminder0
        Act reminder1 = createReminder(patient1, group1);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS);

        // create a reminder for patient2, with an entity.reminderType with
        // group2 lookup.reminderGroup. Verify it has not changed reminder1
        Act reminder2 = createReminder(patient2, group2);
        checkReminder(reminder2, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it marks reminder1 COMPLETED.
        Act reminder3 = createReminder(patient1, group1);
        checkReminder(reminder3, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.COMPLETED);

        // create a reminder for patient2, with an entity.reminderType with
        // both group1 and group2 lookup.reminderGroup. Verify it marks
        // reminder2 COMPLETED.
        Act reminder4 = createReminder(patient2, group1, group2);
        checkReminder(reminder4, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder2, ReminderStatus.COMPLETED);

        // create a reminder type with no group, and create 2 reminders using it.
        Entity reminderType = ReminderTestHelper.createReminderType();
        Act reminder5 = createReminder(patient1, reminderType);
        Act reminder6 = createReminder(patient2, reminderType);
        checkReminder(reminder5, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder6, ReminderStatus.IN_PROGRESS);

        // now create a reminder for patient1. Verify it marks reminder5 COMPLETED
        Act reminder7 = createReminder(patient1, reminderType);
        checkReminder(reminder5, ReminderStatus.COMPLETED);
        checkReminder(reminder6, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder7, ReminderStatus.IN_PROGRESS);
    }

    /**
     * Verifies that {@link ReminderRules#markMatchingRemindersCompleted(Act)} cannot mark matching reminders
     * completed if they are saved within the same transaction.
     * This is because the new reminders cannot be queried until the transaction completes.
     * If this occurs, consider using {@link ReminderRules#markMatchingRemindersCompleted(List)}.
     */
    @Test
    public void testMarkMatchingRemindersCompletedInTxn() {
        Entity reminderType = ReminderTestHelper.createReminderType();

        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        // verify that when reminders are saved in different transactions, they are updated correctly
        Act reminder1 = ReminderTestHelper.createReminder(patient1, reminderType);
        Act reminder1dup = ReminderTestHelper.createReminder(patient1, reminderType);
        save(reminder1dup);
        save(reminder1);

        checkReminder(reminder1dup, ReminderStatus.COMPLETED);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);

        // verify that when reminders are saved in the same transaction, they don't update
        Act reminder2 = ReminderTestHelper.createReminder(patient2, reminderType);
        Act reminder2dup = ReminderTestHelper.createReminder(patient2, reminderType);

        // save in same transaction. The rule won't be able to query the new acts
        save(reminder2, reminder2dup);

        // verify all acts (including duplicate) are in progress
        checkReminder(reminder2, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder2dup, ReminderStatus.IN_PROGRESS);

        // now save reminder2 again. As it is no longer new, it shouldn't update reminder2dup
        save(reminder2);
        checkReminder(reminder2, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder2dup, ReminderStatus.IN_PROGRESS);
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
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);

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

        checkReminder(reminder0, ReminderStatus.COMPLETED);
        checkReminder(reminder1, ReminderStatus.COMPLETED);
        checkReminder(reminder2, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder3, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder3dup, ReminderStatus.COMPLETED); // as it duplicates reminder3
    }

    /**
     * Tests the {@link ReminderRules#calculateReminderDueDate(Date, Entity)}
     * method.
     */
    @Test
    public void testCalculateReminderDueDate() {
        checkCalculateReminderDueDate(1, DateUnits.DAYS, "2007-01-01",
                                      "2007-01-02");
        checkCalculateReminderDueDate(2, DateUnits.WEEKS, "2007-01-01",
                                      "2007-01-15");
        checkCalculateReminderDueDate(2, DateUnits.MONTHS, "2007-01-01",
                                      "2007-03-01");
        checkCalculateReminderDueDate(5, DateUnits.YEARS, "2007-01-01",
                                      "2012-01-01");
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
     * Tests the {@link ReminderRules#countReminders(Party)} method.
     * Requires <em>Reminder.hbm.xml</em>.
     */
    @Test
    public void testCountReminders() {
        Party patient = TestHelper.createPatient();
        assertEquals(0, rules.countReminders(patient));
        int count = 5;
        Act[] reminders = new Act[count];
        for (int i = 0; i < count; ++i) {
            reminders[i] = createReminder(patient);
        }
        assertEquals(count, rules.countReminders(patient));

        Act reminder0 = reminders[0];
        reminder0.setStatus(ActStatus.COMPLETED);
        save(reminder0);
        assertEquals(count - 1, rules.countReminders(patient));

        Act reminder1 = reminders[1];
        reminder1.setStatus(ActStatus.CANCELLED);
        save(reminder1);
        assertEquals(count - 2, rules.countReminders(patient));
    }

    /**
     * Tests the {@link ReminderRules#countAlerts} method.
     * Requires <em>Reminder.hbm.xml</em>.
     */
    @Test
    public void testCountAlerts() {
        Party patient = TestHelper.createPatient();
        Date date = new Date();
        assertEquals(0, rules.countAlerts(patient, date));
        int count = 5;
        Act[] alerts = new Act[count];
        for (int i = 0; i < count; ++i) {
            alerts[i] = createAlert(patient);
        }
        assertEquals(count, rules.countAlerts(patient, date));

        Act alert0 = alerts[0];
        alert0.setStatus(ActStatus.COMPLETED);
        save(alert0);
        assertEquals(count - 1, rules.countAlerts(patient, date));

        Act alert1 = alerts[1];
        alert1.setActivityEndTime(date);
        save(alert1);
        assertEquals(count - 2, rules.countAlerts(patient, date));
    }

    /**
     * Tests the {@link ReminderRules#isDue(Act, Date, Date)} method.
     */
    @Test
    public void testIsDue() {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType(
                1, DateUnits.MONTHS, group);
        Date start = java.sql.Date.valueOf("2007-01-01");
        Date due = rules.calculateReminderDueDate(start, reminderType);
        Act reminder = createReminderWithDueDate(patient, reminderType,
                                                 due);

        checkDue(reminder, null, null, true);
        checkDue(reminder, null, "2007-01-01", false);
        checkDue(reminder, "2007-01-01", null, true);
        checkDue(reminder, "2007-01-01", "2007-01-31", false);
        checkDue(reminder, "2007-01-01", "2007-02-01", true);

        // Now add a template to the reminderType, due 2 weeks after the current
        // due date.
        EntityRelationship reminderTypeTemplate = (EntityRelationship) create(
                ReminderArchetypes.REMINDER_TYPE_TEMPLATE);
        Entity template = (Entity) create("entity.documentTemplate");
        template.setName("XTestTemplate_" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(reminderTypeTemplate);
        bean.setValue("reminderCount", 0);
        bean.setValue("interval", 2);
        bean.setValue("units", DateUnits.WEEKS);
        bean.setValue("source", reminderType.getObjectReference());
        bean.setValue("target", template.getObjectReference());
        save(reminderTypeTemplate, template);
        checkDue(reminder, "2007-01-01", "2007-02-14", false);
        checkDue(reminder, "2007-01-01", "2007-02-15", true);
    }

    /**
     * Tests the {@link ReminderRules#shouldCancel(Act, Date)} method.
     */
    @Test
    public void testShouldCancel() {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType(
                1, DateUnits.MONTHS, group);
        Date start = java.sql.Date.valueOf("2007-01-01");
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType);
        reminder.setActivityStartTime(start);
        rules.calculateReminderDueDate(reminder);

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
     * Tests the {@link ReminderRules#getContact(Set)} method.
     */
    @Test
    public void testGetContact() {
        // create a patient, and owner. Remove default contacts from owner
        Party owner = TestHelper.createCustomer();
        Contact[] contacts = owner.getContacts().toArray(new Contact[owner.getContacts().size()]);
        for (Contact contact : contacts) {
            owner.removeContact(contact);
        }

        // add an email contact to the owner, and verify it is returned
        Contact email = createEmail();
        checkContact(owner, email, email);

        // add a location contact to the owner, and verify it is returned
        // instead of the email contact
        Contact location = createLocation(false);
        checkContact(owner, location, location);

        // add a preferred phone contact to the owner, and verify the location
        // contact is still returned
        Contact phone = createPhone(true);
        checkContact(owner, phone, location);

        // add a preferred location contact to the owner, and verify it is
        // returned instead of the non-preferred location contact
        Contact preferredLocation = createLocation(true);
        checkContact(owner, preferredLocation, preferredLocation);

        // add a REMINDER classification to the email contact and verify it is
        // returned instead of the preferred location contact
        Lookup reminder = TestHelper.getLookup("lookup.contactPurpose",
                                               "REMINDER");
        email.addClassification(reminder);
        checkContact(owner, email, email);

        // add a REMINDER classification to the location contact and verify it
        // is returned instead of the email contact
        preferredLocation.addClassification(reminder);
        checkContact(owner, preferredLocation, preferredLocation);
    }

    /**
     * Tests the {@link ReminderRules#getDocumentFormReminder} method, when the <em>act.patientDocumentForm</em> is
     * linked to an invoice item.
     */
    @Test
    public void testGetDocumentFormReminderForInvoiceItem() {
        Party patient = TestHelper.createPatient();
        DocumentAct form = (DocumentAct) create(PatientArchetypes.DOCUMENT_FORM);
        ActBean formBean = new ActBean(form);
        formBean.addNodeParticipation("patient", patient);
        save(form);

        // verify a form not associated with any invoice item nor product returns null
        assertNull(rules.getDocumentFormReminder(form));

        // create an invoice item and associate the form with it
        Act item = FinancialTestHelper.createItem(CustomerAccountArchetypes.INVOICE_ITEM, Money.ONE,
                                                  patient, TestHelper.createProduct());
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
        DocumentAct form = (DocumentAct) create(PatientArchetypes.DOCUMENT_FORM);
        ActBean formBean = new ActBean(form);
        formBean.addNodeParticipation("patient", patient);
        formBean.addNodeParticipation("product", product);
        save(form);

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
        checkReminder(reminder1, reminderType1, patient, product, form, dueDate1);

        Entity reminderType2 = createReminderType();
        EntityRelationship productReminder2 = productBean.addNodeRelationship("reminders", reminderType2);
        save(product, reminderType2);

        Date dueDate2 = rules.calculateProductReminderDueDate(form.getActivityStartTime(), productReminder2);
        assertTrue(dueDate2.compareTo(dueDate1) < 0);

        Act reminder2 = rules.getDocumentFormReminder(form);
        assertNotNull(reminder2);
        assertTrue(reminder2.isNew()); // reminders from products should not be persistent
        checkReminder(reminder2, reminderType2, patient, product, form, dueDate2);
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
        DocumentAct form = (DocumentAct) create(PatientArchetypes.DOCUMENT_FORM);
        ActBean formBean = new ActBean(form);
        formBean.addNodeParticipation("patient", patient);
        formBean.addNodeParticipation("product", product);
        save(form);

        Date dueDate1 = rules.calculateProductReminderDueDate(form.getActivityStartTime(), relationship);
        Act reminder1 = rules.getDocumentFormReminder(form);
        assertNotNull(reminder1);
        assertTrue(reminder1.isNew()); // reminders from products should not be persistent
        checkReminder(reminder1, reminderType1, patient, product, form, dueDate1);

        // create an invoice item and associate the form with it
        Act item = FinancialTestHelper.createItem(CustomerAccountArchetypes.INVOICE_ITEM, Money.ONE,
                                                  patient, product);
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

    private void checkReminder(Act reminder, Entity reminderType, Party patient, Product product, DocumentAct form,
                               Date dueDate) {
        ActBean bean = new ActBean(reminder);
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(reminderType, bean.getNodeParticipant("reminderType"));
        assertEquals(product, bean.getNodeParticipant("product"));
        assertEquals(form.getActivityStartTime(), reminder.getActivityStartTime());
        assertEquals(dueDate, reminder.getActivityEndTime());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new ReminderRules();
    }

    /**
     * Adds a contact to a customer and verifies the expected contact returned by {@link ReminderRules#getContact(Set)}.
     *
     * @param customer the customer
     * @param contact  the contact to add
     * @param expected the expected contact
     */
    private void checkContact(Party customer, Contact contact,
                              Contact expected) {
        customer.addContact(contact);
        Contact c = rules.getContact(customer.getContacts());
        assertEquals(expected, c);
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
        if (ReminderStatus.COMPLETED.equals(status)) {
            assertNotNull(date);
        } else {
            assertNull(date);
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
     * Checks if a reminder is due using
     * {@link ReminderRules#isDue(Act, Date, Date)}.
     *
     * @param reminder the reminder
     * @param fromDate the from date. May be <tt>null</tt>
     * @param toDate   the to date. May be <tt>null</tt>
     * @param expected the expected isDue result
     */
    private void checkDue(Act reminder, String fromDate, String toDate,
                          boolean expected) {
        Date from = (fromDate != null) ? java.sql.Date.valueOf(fromDate) : null;
        Date to = (toDate != null) ? java.sql.Date.valueOf(toDate) : null;
        assertEquals(expected, rules.isDue(reminder, from, to));
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

    /**
     * Helper to create an email contact.
     *
     * @return a new email contact
     */
    private Contact createEmail() {
        Contact contact = (Contact) create(ContactArchetypes.EMAIL);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("emailAddress", "foo@bar.com");
        bean.save();
        return contact;
    }

    /**
     * Helper to create a phone contact.
     *
     * @param preferred determines if it is the preferred contact
     * @return a new phone contact
     */
    private Contact createPhone(boolean preferred) {
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("preferred", preferred);
        save(contact);
        return contact;
    }

    /**
     * Helper to create a location contact.
     *
     * @param preferred determines if it is the preferred contact
     * @return a new location contact
     */
    private Contact createLocation(boolean preferred) {
        Contact contact = (Contact) create(ContactArchetypes.LOCATION);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("preferred", preferred);
        save(contact);
        return contact;
    }

    /**
     * Helper to create and save an <em>act.patientAlert</tt> for a patient.
     *
     * @param patient the patient
     * @return a new alert
     */
    private Act createAlert(Party patient) {
        Act act = (Act) create("act.patientAlert");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        Lookup alertType = TestHelper.getLookup("lookup.patientAlertType", "OTHER", false);
        IMObjectBean lookupBean = new IMObjectBean(alertType);
        lookupBean.setValue("colour", "0xFFFFFF");
        lookupBean.save();
        bean.setValue("alertType", alertType.getCode());
        bean.save();
        return act;
    }

}
