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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.reminder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.report.ReportFactory;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.component.service.MailService;
import org.openvpms.web.component.service.PracticeMailService;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.reminder.ReminderSMSEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;

/**
 * Tests the {@link PatientReminderSenderJob}.
 *
 * @author Tim Anderson
 */
public class PatientReminderSenderJobTestCase extends ArchetypeServiceTest {

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
     * The reminder rules.
     */
    @Autowired
    ReminderRules reminderRules;

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The job.
     */
    private PatientReminderSenderJob job;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        practice = (Party) create(PracticeArchetypes.PRACTICE);
        practice.setName("Test Practice");
        practice.addContact(TestHelper.createEmailContact("foo@bar.com"));

        Entity settings = (Entity) create("entity.mailServer");
        IMObjectBean bean = new IMObjectBean(settings);
        bean.setValue("name", "Default Mail Server");
        bean.setValue("host", "localhost");
        bean.save();

        IMObjectBean practiceBean = new IMObjectBean(practice);
        practiceBean.setTarget("mailServer", settings);

        PracticeService practiceService = new PracticeService(getArchetypeService(), practiceRules, null) {
            @Override
            public synchronized Party getPractice() {
                return practice;
            }
        };

        PracticeMailService mailService = Mockito.mock(PracticeMailService.class);

        IArchetypeRuleService service = (IArchetypeRuleService) getArchetypeService();
        DocumentHandlers handlers = new DocumentHandlers(service);

        Entity configuration = (Entity) create("entity.jobPatientReminderSender");

        EmailTemplateEvaluator evaluator = new EmailTemplateEvaluator(getArchetypeService(), getLookupService(),
                                                                      null, Mockito.mock(ReportFactory.class),
                                                                      Mockito.mock(Converter.class));

        MailerFactory mailerFactory = Mockito.mock(MailerFactory.class);
        Mailer mailer = Mockito.mock(Mailer.class);
        Mockito.when(mailerFactory.create(any(), any())).thenReturn(mailer);

        job = new PatientReminderSenderJob(configuration, service, practiceService,
                                           reminderRules, patientRules, practiceRules,
                                           mailService, handlers, evaluator,
                                           Mockito.mock(ReporterFactory.class),
                                           Mockito.mock(ConnectionFactory.class),
                                           Mockito.mock(ReminderSMSEvaluator.class),
                                           Mockito.mock(CommunicationLogger.class)) {
            @Override
            MailerFactory getMailerFactory(MailService mailService, DocumentHandlers handlers) {
                return mailerFactory;
            }
        };
    }

    /**
     * Verifies that reminders can be sent if the reminder configuration is set to cancel them 1 day after they
     * due to be sent.
     */
    @Test
    public void test1DayCancelInterval() {
        // set the reminder configuration cancel reminders 1 day after they are due to send
        Party location = TestHelper.createLocation();
        Entity reminderConfig = (Entity) create(ReminderArchetypes.CONFIGURATION);
        IMObjectBean configBean = new IMObjectBean(reminderConfig);
        configBean.setValue("emailCancelInterval", 1);
        configBean.setValue("emailCancelUnits", DateUnits.DAYS.toString());
        configBean.setValue("smsCancelInterval", 1);
        configBean.setValue("smsCancelUnits", DateUnits.DAYS.toString());
        configBean.setValue("emailAttachments", false);
        configBean.setTarget("location", location);
        configBean.save();
        IMObjectBean practiceBean = new IMObjectBean(practice);
        practiceBean.setTarget("reminderConfiguration", reminderConfig);

        // create a reminder type
        Entity reminderType = ReminderTestHelper.createReminderType();
        Entity template = ReminderTestHelper.createDocumentTemplate();
        ReminderTestHelper.addEmailTemplate(template, ReminderTestHelper.createEmailTemplate("Foo", "Bar"));
        ReminderTestHelper.addSMSTemplate(template, ReminderTestHelper.createSMSTemplate("TEXT", "Some text"));
        ReminderTestHelper.addReminderCount(reminderType, 0, -30, DateUnits.DAYS, template);

        // create a customer that can receive email and SMS reminders
        Party customer = TestHelper.createCustomer(TestHelper.createEmailContact("foo@bar.com"),
                                                   TestHelper.createPhoneContact(null, "04123456789", true));
        Party patient = TestHelper.createPatient(customer);

        Date yesterday = DateRules.getYesterday();
        Date today = DateRules.getToday();
        Date tomorrow = DateRules.getTomorrow();

        // create a reminder with email and SMS items to be sent today
        Act email1 = ReminderTestHelper.createEmailReminder(today, today, ReminderItemStatus.PENDING, 0);
        Act sms1 = ReminderTestHelper.createEmailReminder(today, today, ReminderItemStatus.PENDING, 0);
        Act reminder1 = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, email1, sms1);
        checkReminder(reminder1, 0);

        // create a reminder with email and SMS items that should have been sent yesterday
        Act email2 = ReminderTestHelper.createEmailReminder(yesterday, yesterday, ReminderItemStatus.PENDING, 0);
        Act sms2 = ReminderTestHelper.createEmailReminder(yesterday, yesterday, ReminderItemStatus.PENDING, 0);
        Act reminder2 = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, email2, sms2);
        checkReminder(reminder2, 0);

        // execute the job
        job.execute(null);

        // items associated with reminder1 should have been sent
        checkItem(email1, ReminderItemStatus.COMPLETED, null);
        checkItem(sms1, ReminderItemStatus.COMPLETED, null);
        checkReminder(reminder1, 1);

        // items associated with reminder1 should have been cancelled as they needed to have been sent by 12am today.
        checkItem(email2, ReminderItemStatus.CANCELLED, "Reminder not processed in time");
        checkItem(sms2, ReminderItemStatus.CANCELLED, "Reminder not processed in time");
        checkReminder(reminder2, 1);
    }

    /**
     * Verifies a reminder item matches that expected.
     *
     * @param item   the reminder item
     * @param status the expected status
     * @param error  the expected error message. May be {@code null}
     */
    private void checkItem(Act item, String status, String error) {
        item = get(item);
        assertNotNull(item);
        assertEquals(status, item.getStatus());
        IMObjectBean bean = new IMObjectBean(item);
        assertEquals(error, bean.getString("error"));
    }

    /**
     * Verifies a reminder matches that expected.
     *
     * @param reminder the reminder
     * @param count    the expected reminder count
     */
    private void checkReminder(Act reminder, int count) {
        reminder = get(reminder);
        assertNotNull(reminder);
        assertEquals(ReminderStatus.IN_PROGRESS, reminder.getStatus());
        IMObjectBean bean = new IMObjectBean(reminder);
        assertEquals(count, bean.getInt("reminderCount"));
    }
}
