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

package org.openvpms.web.workspace.reporting.reminder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.report.ReportFactory;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailException;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailRule;

/**
 * Tests the {@link ReminderEmailProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderEmailProcessorTestCase extends AbstractPatientReminderProcessorTest {

    /**
     * Practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * The mailer
     */
    private Mailer mailer;

    /**
     * The reminder processor.
     */
    private ReminderEmailProcessor processor;

    /**
     * Constructs a {@link ReminderEmailProcessorTestCase}.
     */
    public ReminderEmailProcessorTestCase() {
        super(ContactArchetypes.EMAIL);
    }

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        reminderType.setName("Vaccination Reminder");
        save(reminderType);
        Entity emailTemplate = ReminderTestHelper.createEmailTemplate("openvpms:get(., 'reminderType.entity.name')",
                                                                      "text");
        IMObjectBean bean = new IMObjectBean(emailTemplate);
        bean.setValue("subjectType", "XPATH");
        bean.save();
        Entity documentTemplate = ReminderTestHelper.createDocumentTemplate(emailTemplate, null);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, documentTemplate, createEmailRule());

        MailerFactory mailerFactory = Mockito.mock(MailerFactory.class);
        mailer = Mockito.mock(Mailer.class);
        Mockito.when(mailerFactory.create(Mockito.<MailContext>any())).thenReturn(mailer);

        IArchetypeService service = getArchetypeService();
        EmailTemplateEvaluator evaluator = new EmailTemplateEvaluator(getArchetypeService(), getLookupService(),
                                                                      null, Mockito.mock(ReportFactory.class),
                                                                      Mockito.mock(Converter.class));
        ReminderTypes reminderTypes = new ReminderTypes(service);
        CommunicationLogger logger = Mockito.mock(CommunicationLogger.class);
        ReporterFactory reporterFactory = Mockito.mock(ReporterFactory.class);
        ReminderConfiguration config = createConfiguration();
        processor = new ReminderEmailProcessor(mailerFactory, evaluator, reporterFactory, reminderTypes, practice,
                                               reminderRules, patientRules, practiceRules, service, config, logger);
    }

    /**
     * Verifies that emails are sent to the REMINDER contact.
     */
    @Test
    public void testReminderContact() {
        Contact email1 = TestHelper.createEmailContact("x@test.com", false, "REMINDER");
        Contact email2 = TestHelper.createEmailContact("y@test.com", true, null);
        Contact email3 = TestHelper.createEmailContact("z@test.com");
        customer.addContact(email1);
        customer.addContact(email2);
        customer.addContact(email3);

        checkSend(null, "x@test.com");
    }

    /**
     * Verifies that emails can be sent to a contact different to the default.
     */
    @Test
    public void testOverrideContact() {
        Contact email1 = TestHelper.createEmailContact("x@test.com", true, "REMINDER");
        Contact email2 = TestHelper.createEmailContact("y@test.com", false, null);
        customer.addContact(email1);
        customer.addContact(email2);

        checkSend(email2, "y@test.com");
    }

    /**
     * Verifies that {@link ReminderEmailProcessor#failed(PatientReminders, Throwable)} updates reminders with the
     * failure message.
     */
    @Test
    public void testFailed() {
        Contact email = TestHelper.createEmailContact("x@test.com", true, "REMINDER");
        customer.addContact(email);

        Date tomorrow = DateRules.getTomorrow();
        Act item = ReminderTestHelper.createSMSReminder(DateRules.getToday(), tomorrow, ReminderItemStatus.PENDING, 0);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);

        doThrow(new MailException(MailException.ErrorCode.FailedToSend, "x@test.com", "some error"))
                .when(mailer).send();

        PatientReminders reminders = prepare(item, reminder, null);
        try {
            processor.process(reminders);
            fail("Expected exception to be thrown");
        } catch (ReportingException expected) {
            assertTrue(processor.failed(reminders, expected));
            checkItem(get(item), ReminderItemStatus.ERROR,
                      "Failed to process reminder: Failed to send email to x@test.com: some error");
        }
    }

    /**
     * Returns the reminder processor.
     *
     * @return the reminder processor
     */
    @Override
    protected PatientReminderProcessor getProcessor() {
        return processor;
    }

    /**
     * Creates a PENDING reminder item for reminder count 0.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @return a new reminder item
     */
    @Override
    protected Act createReminderItem(Date send, Date dueDate) {
        return ReminderTestHelper.createEmailReminder(send, dueDate, ReminderItemStatus.PENDING, 0);
    }

    /**
     * Sends an email reminder.
     *
     * @param contact the contact to use. May be {@code null}
     * @param to      the expected to address
     */
    private void checkSend(Contact contact, String to) {
        PatientReminders reminders = prepare(contact);
        processor.process(reminders);
        Mockito.verify(mailer).setTo(new String[]{to});
        Mockito.verify(mailer).setSubject("Vaccination Reminder");
    }

    /**
     * Prepares a reminder for send.
     *
     * @param contact the contact to use. May be {@code null}
     * @return the reminders
     */
    private PatientReminders prepare(Contact contact) {
        Date tomorrow = DateRules.getTomorrow();
        Act item = createReminderItem(DateRules.getToday(), tomorrow);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);
        return prepare(item, reminder, contact);
    }

}
