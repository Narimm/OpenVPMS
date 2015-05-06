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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ReminderProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderProcessorTestCase extends ArchetypeServiceTest {

    /**
     * Patient rules.
     */
    private PatientRules rules;

    /**
     * Reminder rules.
     */
    private ReminderRules reminderRules;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The reminder type.
     */
    private Entity reminderType;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeService service = getArchetypeService();
        rules = new PatientRules(new PracticeRules(service, null), service, getLookupService());
        reminderRules = new ReminderRules(service, rules);
        customer = TestHelper.createCustomer(false);
        customer.getContacts().clear();
        save(customer);
        patient = TestHelper.createPatient(customer);
        reminderType = ReminderTestHelper.createReminderType(1, DateUnits.MONTHS, 1, DateUnits.DAYS);
    }

    /**
     * Verifies that a reminder for a customer with a single location contact is set to PRINT.
     */
    @Test
    public void testLocationContact() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        Act reminder = createReminderDueTomorrow();

        Contact contact = createLocation();
        checkProcess(contact, ReminderEvent.Action.PRINT, reminder, reminderType, customer, patient, template);
    }

    /**
     * Verifies that a reminder for a customer with a single phone contact is set to PHONE.
     */
    @Test
    public void testPhoneContact() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        Act reminder = createReminderDueTomorrow();

        Contact contact = createPhone(false);
        checkProcess(contact, ReminderEvent.Action.PHONE, reminder, reminderType, customer, patient, template);
    }

    /**
     * Verifies that a reminder for a customer with a single phone contact with SMS enabled is set to SMS.
     */
    @Test
    public void testSMSContact() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("sms", "Test SMS");
        bean.save();
        ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        Act reminder = createReminderDueTomorrow();

        Contact contact = createPhone(true);
        checkProcess(contact, ReminderEvent.Action.SMS, reminder, reminderType, customer, patient, template);
    }

    /**
     * Verifies that if a template has no SMS text, the reminders will have PHONE action rather than SMS.
     */
    @Test
    public void testProcessSMSForNoSMText() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        IMObjectBean bean = new IMObjectBean(template);
        assertNull(bean.getString("sms"));
        ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        Act reminder = createReminderDueTomorrow();

        Contact contact = createPhone(true);
        checkProcess(contact, ReminderEvent.Action.PHONE, reminder, reminderType, customer, patient, template);
    }

    /**
     * Verifies that a reminder for a customer with an email contact is set to EMAIL.
     */
    @Test
    public void testProcessEmail() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        Act reminder = createReminderDueTomorrow();

        Contact contact = createEmail();
        checkProcess(contact, ReminderEvent.Action.EMAIL, reminder, reminderType, customer, patient, template);
    }

    /**
     * Verifies that if a reminder type relationship has {@code list = true}, reminders are set to {@code LIST}.
     */
    @Test
    public void testProcessList() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        EntityRelationship relationship = ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        IMObjectBean bean = new IMObjectBean(relationship);
        bean.setValue("list", true);
        bean.save();
        Act reminder = createReminderDueTomorrow();

        Contact contact = createEmail();
        checkProcess(contact, ReminderEvent.Action.LIST, reminder, reminderType, customer, patient, template);
    }

    /**
     * Verifies that if a reminder type relationship has {@code export = true}, and the customer has a location contact,
     * reminders are set to {@code EXPORT}.
     */
    @Test
    public void testProcessExport() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        EntityRelationship relationship = ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        IMObjectBean bean = new IMObjectBean(relationship);
        bean.setValue("export", true);
        bean.save();

        Act reminder1 = createReminderDueTomorrow();
        checkProcess(createLocation(), ReminderEvent.Action.EXPORT, reminder1, reminderType, customer, patient,
                     template);

        customer.getContacts().clear();

        // with no location contact, export=true is ignored
        Act reminder2 = createReminderDueTomorrow();
        checkProcess(createEmail(), ReminderEvent.Action.EMAIL, reminder2, reminderType, customer, patient, template);
    }

    /**
     * Verifies that if a reminder type relationship has {@code sms = true}, and the customer has an SMS contact,
     * reminders are set to {@code SMS}.
     */
    @Test
    public void testProcessSMS() {
        Entity template = ReminderTestHelper.createDocumentTemplate();
        IMObjectBean templateBean = new IMObjectBean(template);
        templateBean.setValue("sms", "Test SMS");
        templateBean.save();
        EntityRelationship relationship = ReminderTestHelper.addTemplate(reminderType, template, 0, 0, DateUnits.DAYS);
        IMObjectBean bean = new IMObjectBean(relationship);
        bean.setValue("sms", true);
        bean.save();
        Act reminder = createReminderDueTomorrow();

        customer.addContact(createLocation());
        checkProcess(createPhone(true), ReminderEvent.Action.SMS, reminder, reminderType, customer, patient, template);

        customer.getContacts().clear();

        // with no SMS contact, sms=true is ignored
        checkProcess(createLocation(), ReminderEvent.Action.PRINT, reminder, reminderType, customer, patient, template);
    }

    /**
     * Verifies that a reminder type without a template is skipped.
     */
    @Test
    public void testSkip() {
        Act reminder = createReminderDueTomorrow();
        Contact contact = createLocation();
        checkProcess(contact, ReminderEvent.Action.SKIP, reminder, reminderType, null, null, null);
    }

    /**
     * Verifies that reminders whose due date are passed are cancelled.
     */
    @Test
    public void testCancelForPastDue() {
        Act reminder = createReminder(DateRules.getYesterday());
        assertTrue(reminderRules.shouldCancel(reminder, new Date()));

        Contact contact = createLocation();
        checkProcess(contact, ReminderEvent.Action.CANCEL, reminder, reminderType, null, null, null);
    }

    /**
     * Verifies that reminders for deceased patients are set to cancel.
     */
    @Test
    public void testCancelForDeceased() {
        Act reminder = createReminderDueTomorrow();
        rules.setDeceased(patient);
        save(patient);

        Contact contact = createLocation();
        checkProcess(contact, ReminderEvent.Action.CANCEL, reminder, reminderType, null, null, null);
    }

    /**
     * Creates a reminder.
     *
     * @param due the the due date
     * @return a new reminder
     */
    private Act createReminder(Date due) {
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType);
        reminder.setActivityEndTime(due);
        return reminder;
    }

    /**
     * Creates a reminder due tomorrow.
     *
     * @return a new reminder
     */
    private Act createReminderDueTomorrow() {
        return createReminder(DateRules.getTomorrow());
    }

    /**
     * Processes a reminder and verifies the result matches that expected.
     *
     * @param contact          the customer contact
     * @param action           the expected action
     * @param reminder         the reminder to process
     * @param reminderType     the expected reminderType
     * @param customer         the expected customer. May be {@code null}
     * @param patient          the expected patient. May be {@code null}
     * @param documentTemplate the expected document template. May be {@code null}
     */
    private void checkProcess(Contact contact, ReminderEvent.Action action, Act reminder, Entity reminderType,
                              Party customer, Party patient, Entity documentTemplate) {
        this.customer.addContact(contact);
        save(this.customer);
        ReminderEvent event = process(reminder);
        if (action == ReminderEvent.Action.CANCEL || action == ReminderEvent.Action.SKIP) {
            contact = null;
        }
        checkEvent(event, action, reminder, reminderType, customer, contact, patient, documentTemplate);
    }

    /**
     * Processes a reminder.
     *
     * @param reminder the reminder
     * @return the reminder event
     */
    private ReminderEvent process(Act reminder) {
        ReminderProcessor processor = new ReminderProcessor(null, null, new Date(), false, getArchetypeService(),
                                                            rules);
        final List<ReminderEvent> events = new ArrayList<ReminderEvent>();
        processor.addListener(new ProcessorListener<ReminderEvent>() {
            @Override
            public void process(ReminderEvent event) {
                events.add(event);
            }
        });

        processor.process(reminder);
        assertEquals(1, events.size());
        return events.get(0);
    }

    /**
     * Verifies that the attributes of an event match that expected.
     *
     * @param event            the event to check
     * @param action           the expected action
     * @param reminder         the expected reminder
     * @param reminderType     the expected reminderType
     * @param customer         the expected customer. May be {@code null}
     * @param contact          the expected contact. May be {@code null}
     * @param patient          the expected patient. May be {@code null}
     * @param documentTemplate the expected document template. May be {@code null}
     */
    private void checkEvent(ReminderEvent event, ReminderEvent.Action action, Act reminder, Entity reminderType,
                            Party customer, Contact contact, Party patient, Entity documentTemplate) {
        assertEquals(action, event.getAction());
        assertEquals(reminder, event.getReminder());
        assertEquals(reminderType, event.getReminderType().getEntity());
        assertEquals(customer, event.getCustomer());
        assertEquals(contact, event.getContact());
        assertEquals(patient, event.getPatient());
        assertEquals(documentTemplate, event.getDocumentTemplate());
    }

    /**
     * Creates a location contact.
     *
     * @return a new contact
     */
    private Contact createLocation() {
        return TestHelper.createLocationContact("Foo", "ELTHAM", "VIC", "AU", "3095");
    }

    /**
     * Creates an email contact.
     *
     * @return a new contact
     */
    private Contact createEmail() {
        return TestHelper.createEmailContact("foo@bar.com");
    }

    /**
     * Creates a phone contact.
     *
     * @param sms if {@code true}, enables SMS messages
     * @return a new contact
     */
    private Contact createPhone(boolean sms) {
        Contact contact = TestHelper.createPhoneContact("03", "1234566789");
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("sms", sms);
        return contact;
    }

}
