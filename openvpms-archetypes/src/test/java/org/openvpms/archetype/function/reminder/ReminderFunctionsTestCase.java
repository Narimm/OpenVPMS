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

package org.openvpms.archetype.function.reminder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.patient.reminder.ReminderStatus.CANCELLED;

/**
 * Tests the {@link ReminderFunctions} class.
 *
 * @author Tim Anderson
 */
public class ReminderFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ReminderFunctions#getReminders(Party, int, String)} and
     * {@link ReminderFunctions#getReminders(Party, int, String, boolean)} methods.
     */
    @Test
    public void testGetRemindersByCustomer() {
        Party customer = TestHelper.createCustomer();
        checkGetReminders(customer, customer);
    }

    /**
     * Verifies that when a customer has an inactive patient owner relationship to a patient, the reminders for that
     * patient are excluded.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRemindersByCustomerForInactivePatientRelationship() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Entity reminderType = ReminderTestHelper.createReminderType();

        Act reminder = ReminderTestHelper.createReminderWithDueDate(patient, reminderType, DateRules.getTomorrow());
        JXPathContext ctx = createContext(customer);

        // get reminders excluding any reminders prior to the current date
        List<Act> reminders1 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(1, reminders1.size());
        assertEquals(reminder, reminders1.get(0));

        IMObjectBean bean = new IMObjectBean(customer);
        List<EntityRelationship> owner = bean.getValues("patients", EntityRelationship.class);
        assertEquals(1, owner.size());
        owner.get(0).setActiveEndTime(new Date());
        save(customer, patient);

        List<Act> reminders2 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(0, reminders2.size());
    }

    /**
     * Verifies that if a customer has an inactive patient, their reminders are excluded.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRemindersByCustomerExcludesInactivePatient() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Entity reminderType = ReminderTestHelper.createReminderType();

        Act reminder = ReminderTestHelper.createReminderWithDueDate(patient, reminderType, DateRules.getTomorrow());
        JXPathContext ctx = createContext(customer);

        // get reminders excluding any reminders prior to the current date
        List<Act> reminders1 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(1, reminders1.size());
        assertEquals(reminder, reminders1.get(0));

        patient.setActive(false);
        save(patient);

        List<Act> reminders2 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(0, reminders2.size());
    }

    /**
     * Verifies that if a customer has a deceased patient, their reminders are excluded.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRemindersByCustomerExcludesDeceasedPatient() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Entity reminderType = ReminderTestHelper.createReminderType();

        Act reminder = ReminderTestHelper.createReminderWithDueDate(patient, reminderType, DateRules.getTomorrow());
        JXPathContext ctx = createContext(customer);

        // get reminders excluding any reminders prior to the current date
        List<Act> reminders1 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(1, reminders1.size());
        assertEquals(reminder, reminders1.get(0));

        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("deceased", true);
        bean.save();

        List<Act> reminders2 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(0, reminders2.size());
    }

    /**
     * Tests the {@link ReminderFunctions#getReminders(Act, int, String)} and
     * {@link ReminderFunctions#getReminders(Act, int, String, boolean)} methods.
     */
    @Test
    public void testGetRemindersByAct() {
        Act act = (Act) create("act.customerAccountChargesInvoice");
        Party customer = TestHelper.createCustomer();
        ActBean invoice = new ActBean(act);
        invoice.addNodeParticipation("customer", customer);

        checkGetReminders(act, customer);
    }

    /**
     * Tests the {@link ReminderFunctions#getDocumentFormReminder} method.
     */
    @Test
    public void testGetDocumentFormReminder() {
        Party patient = TestHelper.createPatient();
        DocumentAct form = (DocumentAct) create(PatientArchetypes.DOCUMENT_FORM);
        ActBean formBean = new ActBean(form);
        formBean.addNodeParticipation("patient", patient);
        save(form);

        // verify a form not associated with any invoice item nor product returns null
        assertNull(getDocumentFormReminder(form));

        // create an invoice item and associate the form with it
        Act item = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient,
                                                        TestHelper.createProduct(), BigDecimal.ONE);
        ActBean itemBean = new ActBean(item);
        itemBean.addNodeRelationship("documents", form);
        save(item, form);

        // associate a single reminder with the invoice item, and verify it is returned by getDocumentFormReminder()
        Act reminder = ReminderTestHelper.createReminder(patient, ReminderTestHelper.createReminderType());
        itemBean.addNodeRelationship("reminders", reminder);
        save(item, reminder);
        assertEquals(reminder, getDocumentFormReminder(form));
    }

    /**
     * Tests the {@link ReminderFunctions#getPatientReminders(Party, Date)} method.
     * <br/>
     * Note that this invoked as:
     * <br/>
     * <em>reminder:getReminders(patient, date)</em>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPatientRemindersForDate() {
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType();
        Act reminder1 = createReminder(patient, reminderType, "2016-04-13 11:59:59", IN_PROGRESS);
        Act reminder2 = createReminder(patient, reminderType, "2016-04-14 10:10:10", IN_PROGRESS);
        Act reminder3 = createReminder(patient, reminderType, "2016-04-14 10:10:10", COMPLETED);
        Act reminder4 = createReminder(patient, reminderType, "2016-04-14 10:10:10", CANCELLED);
        Act reminder5 = createReminder(patient, reminderType, "2016-04-15 00:00:00", IN_PROGRESS);

        JXPathContext context = createContext(patient);
        List<Act> acts = getActs((Iterable<Act>) context.getValue(
                "reminder:getReminders(., java.sql.Timestamp.valueOf('2016-04-14 11:00:00'))"));
        assertEquals(3, acts.size());
        assertFalse(acts.contains(reminder1));
        assertTrue(acts.contains(reminder2));
        assertTrue(acts.contains(reminder3));
        assertTrue(acts.contains(reminder4));
        assertFalse(acts.contains(reminder5));
    }

    /**
     * Tests the {@link ReminderFunctions#getRemindersByProductType(Party, String, Date, Date)} method.
     * <br/>
     * Note that this invoked as:
     * <br/>
     * <em>reminder:getReminders(patient, date, date)</em>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPatientRemindersForDateRange() {
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType();
        Act reminder1 = createReminder(patient, reminderType, "2016-04-13 11:59:59", IN_PROGRESS);
        Act reminder2 = createReminder(patient, reminderType, "2016-04-14 10:10:10", IN_PROGRESS);
        Act reminder3 = createReminder(patient, reminderType, "2016-04-14 11:10:10", COMPLETED);
        Act reminder4 = createReminder(patient, reminderType, "2016-04-15 10:10:10", CANCELLED);
        Act reminder5 = createReminder(patient, reminderType, "2016-04-15 11:00:00", IN_PROGRESS);

        JXPathContext context = createContext(patient);
        List<Act> acts = getActs((Iterable<Act>) context.getValue(
                "reminder:getReminders(., java.sql.Timestamp.valueOf('2016-04-14 10:00:00'), "
                + "java.sql.Timestamp.valueOf('2016-04-15 11:00:00'))"));
        assertEquals(3, acts.size());
        assertFalse(acts.contains(reminder1));
        assertTrue(acts.contains(reminder2));
        assertTrue(acts.contains(reminder3));
        assertTrue(acts.contains(reminder4));
        assertFalse(acts.contains(reminder5));
    }

    /**
     * Tests the {@link ReminderFunctions#getRemindersByProductType(Party, String, Date)} method.
     * <br/>
     * Note that this invoked as:
     * <br/>
     * <em>reminder:getReminders(patient, date, productType)</em>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPatientRemindersByProductType() {
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

        JXPathContext context = createContext(patient);
        List<Act> acts1 = getActs((Iterable<Act>) context.getValue(
                "reminder:getReminders(., 'Z Vaccination 1', java.sql.Timestamp.valueOf('2016-04-14 11:00:00'))"));

        assertEquals(1, acts1.size());
        assertFalse(acts1.contains(reminder1));
        assertFalse(acts1.contains(reminder2));
        assertTrue(acts1.contains(reminder3));
        assertFalse(acts1.contains(reminder4));
        assertFalse(acts1.contains(reminder5));

        List<Act> acts2 = getActs((Iterable<Act>) context.getValue(
                "reminder:getReminders(.,  'Z Vaccination*', java.sql.Timestamp.valueOf('2016-04-14 11:00:00'))"));

        assertEquals(2, acts2.size());
        assertTrue(acts2.contains(reminder2));
        assertTrue(acts2.contains(reminder3));
    }

    /**
     * Tests the {@link ReminderFunctions#getRemindersByProductType(Party, String, Date, Date)} method.
     * <br/>
     * Note that this invoked as:
     * <br/>
     * <em>reminder:getReminders(patient, from, to, productType)</em>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPatientRemindersByProductTypeForDateRange() {
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
        Act reminder4 = createReminder(patient, reminderType, product2, "2016-04-15 10:10:10", CANCELLED);
        Act reminder5 = createReminder(patient, reminderType, product3, "2016-04-15 10:45:00", IN_PROGRESS);
        Act reminder6 = createReminder(patient, reminderType, product1, "2016-04-15 11:00:00", IN_PROGRESS);

        JXPathContext context = createContext(patient);
        List<Act> acts1 = getActs((Iterable<Act>) context.getValue(
                "reminder:getReminders(., 'Z Vaccination 1', java.sql.Timestamp.valueOf('2016-04-14 10:00:00'), "
                + "java.sql.Timestamp.valueOf('2016-04-15 11:00:00'))"));
        assertEquals(1, acts1.size());
        assertFalse(acts1.contains(reminder1));
        assertFalse(acts1.contains(reminder2));
        assertTrue(acts1.contains(reminder3));
        assertFalse(acts1.contains(reminder4));
        assertFalse(acts1.contains(reminder5));
        assertFalse(acts1.contains(reminder6));

        List<Act> acts2 = getActs((Iterable<Act>) context.getValue(
                "reminder:getReminders(., 'Z Vacc*', java.sql.Timestamp.valueOf('2016-04-14 10:00:00'), "
                + "java.sql.Timestamp.valueOf('2016-04-15 11:00:00'))"));
        assertEquals(3, acts2.size());
        assertFalse(acts2.contains(reminder1));
        assertTrue(acts2.contains(reminder2));
        assertTrue(acts2.contains(reminder3));
        assertTrue(acts2.contains(reminder4));
        assertFalse(acts2.contains(reminder5));
        assertFalse(acts2.contains(reminder6));
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
     * Tests the {@link ReminderFunctions#getReminders} methods.
     *
     * @param context  the jxpath context. Either a customer or an act with a customer participation
     * @param customer the customer
     */
    @SuppressWarnings("unchecked")
    private void checkGetReminders(Object context, Party customer) {
        final int count = 10;
        Entity reminderType = ReminderTestHelper.createReminderType();

        Calendar calendar = new GregorianCalendar();

        // backdate the calendar 5 days. When excluding overdue reminders, reminders dated prior to the current date
        // will be ignored.
        calendar.add(Calendar.DAY_OF_YEAR, -5);
        for (int i = 0; i < count; ++i) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date dueDate = calendar.getTime();
            Party patient = TestHelper.createPatient(customer);
            ReminderTestHelper.createReminderWithDueDate(patient, reminderType, dueDate);
        }

        JXPathContext ctx = createContext(context);

        // get reminders excluding any reminders prior to the current date
        List<Act> reminders1 = (List<Act>) ctx.getValue("reminder:getReminders(., 1, 'YEARS')");
        assertEquals(6, reminders1.size());

        // get all reminders (i.e., including overdue)
        List<Act> reminders2 = (List<Act>) ctx.getValue("reminder:getReminders(., 12, 'MONTHS', 'true')");
        assertEquals(count, reminders2.size());
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
     * Invokes the reminder:getDocumentFormReminder() jxpath function with the supplied context.
     *
     * @param context the context object
     * @return the resulting reminder act. May be {@code null}
     */
    private Act getDocumentFormReminder(Object context) {
        JXPathContext ctx = createContext(context);
        return (Act) ctx.getValue("reminder:getDocumentFormReminder(.)");
    }

    /**
     * Creates a new JXPathContext with the reminder functions registered.
     *
     * @param context the context object
     * @return a new JXPathContext
     */
    private JXPathContext createContext(Object context) {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        PatientRules patientRules = new PatientRules(null, service, lookups);
        ReminderFunctions functions = new ReminderFunctions(service,
                                                            new ReminderRules(service, patientRules),
                                                            new CustomerRules(service, lookups, null));
        return JXPathHelper.newContext(context, functions);
    }

}
