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

package org.openvpms.web.jobs.appointment;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.macro.Macros;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;
import org.openvpms.web.component.service.SMSService;
import org.openvpms.web.workspace.workflow.appointment.reminder.AppointmentReminderEvaluator;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link ArchetypeServiceTest}.
 *
 * @author Tim Anderson
 */
public class AppointmentReminderJobTestCase extends ArchetypeServiceTest {

    /**
     * The practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * The customer rules.
     */
    private CustomerRules customerRules;

    /**
     * The patient rules.
     */
    private PatientRules patientRules;

    /**
     * The practice service.
     */
    private TestPracticeService practiceService;

    /**
     * Test practice location 1.
     */
    private Party location1;

    /**
     * Test practice location 2.
     */
    private Party location2;

    /**
     * Test appointment schedule 1.
     */
    private Entity schedule1;

    /**
     * Test appointment schedule 2.
     */
    private Entity schedule2;

    /**
     * The date to base tests around.
     */
    private Date dateFrom;

    /**
     * The default appointment reminder template.
     */
    private Entity template;

    /**
     * The evaluator.
     */
    private AppointmentReminderEvaluator evaluator;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeService service = getArchetypeService();
        SMSTemplateEvaluator smsEvaluator = new SMSTemplateEvaluator(service, getLookupService(),
                                                                     applicationContext.getBean(Macros.class));
        evaluator = new AppointmentReminderEvaluator(service, smsEvaluator);
        customerRules = new CustomerRules(service, getLookupService());
        patientRules = new PatientRules(practiceRules, service, getLookupService());
        dateFrom = TestHelper.getDate("2015-11-01");

        Entity scheduleView1 = ScheduleTestHelper.createScheduleView();
        Entity scheduleView2 = ScheduleTestHelper.createScheduleView();

        location1 = createLocation("Vets R Us", scheduleView1);
        location2 = createLocation("Vets Be Us", scheduleView2);

        schedule1 = createSchedule(location1);
        schedule2 = createSchedule(location2);
        ScheduleTestHelper.addSchedules(scheduleView1, schedule1);
        ScheduleTestHelper.addSchedules(scheduleView2, schedule2);

        // remove SMS reminders for existing acts for the test period
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT);
        Date disableFrom = DateRules.getDate(dateFrom, -2, DateUnits.WEEKS);
        Date disableTo = TestHelper.getDate("2015-11-20");
        query.add(Constraints.between("startTime", disableFrom, disableTo));
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);

        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            service.remove(act);
        }

        template = createTemplate();

        practiceService = new TestPracticeService(template, location1, location2);
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        practiceService.dispose();
    }

    /**
     * Tests the job.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJob() throws Exception {
        Entity config = createJobConfig();

        // create 15 appointments from dateFrom, with every even appointment flagged to have reminders sent
        Date date = dateFrom;
        List<Act> appointments = new ArrayList<>();
        for (int i = 0; i < 15; ++i) {
            Act appointment = createAppointment(date, schedule1, i % 2 == 0);
            appointments.add(appointment);
            date = DateRules.getDate(date, 1, DateUnits.DAYS);
        }

        // start sending reminders 3 days prior to the first reminder. The first 6 appointments flagged should be sent
        final Date startDate = DateRules.getDate(dateFrom, -3, DateUnits.DAYS);

        SMSService smsService = createSMSService();

        runJob(config, startDate, smsService);
        for (int i = 0; i < appointments.size(); ++i) {
            Act appointment = get(appointments.get(i));
            if (i % 2 == 0 && i < 12) {
                checkSent(appointment);
            } else {
                checkNotSent(appointment, null);
            }
        }

        Mockito.verify(smsService, Mockito.times(6)).send(Mockito.eq("Reminder: Vets R Us"), Mockito.<Contact>any(),
                                                          Mockito.<Party>any(), Mockito.eq("SMS appointment reminder"),
                                                          Mockito.eq("APPOINTMENT_REMINDER"), Mockito.eq(location1));
        practiceService.dispose();
    }

    /**
     * Verifies that when a schedule has {@code sendReminders == false}, no reminders are sent.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDisableSendRemindersOnSchedule() throws Exception {
        Entity config = createJobConfig();

        Date date = dateFrom;
        List<Act> appointments = new ArrayList<>();
        for (int i = 0; i < 12; ++i) {
            Entity schedule = (i % 2 == 0) ? schedule1 : schedule2;
            Act appointment = createAppointment(date, schedule, true);
            appointments.add(appointment);
            date = DateRules.getDate(date, 1, DateUnits.DAYS);
        }
        IMObjectBean bean = new IMObjectBean(schedule1);
        bean.setValue("sendReminders", false);
        bean.save();

        // start sending reminders 2 days prior to the first reminder. All schedule2 reminders should be sent
        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();
        runJob(config, startDate, smsService);

        for (int i = 0; i < appointments.size(); ++i) {
            Act appointment = get(appointments.get(i));
            if (i % 2 == 0) {
                checkNotSent(appointment, null);
            } else {
                checkSent(appointment);
            }
        }

        Mockito.verify(smsService, Mockito.times(6)).send(Mockito.eq("Reminder: Vets Be Us"), Mockito.<Contact>any(),
                                                          Mockito.<Party>any(), Mockito.eq("SMS appointment reminder"),
                                                          Mockito.eq("APPOINTMENT_REMINDER"), Mockito.eq(location2));
    }

    /**
     * Verifies that the reminderError node is populated if a customer is inactive.
     *
     * @throws Exception for any error
     */
    @Test
    public void testInactiveCustomer() throws Exception {
        Entity config = createJobConfig();

        Party customer1 = createCustomer();
        Party customer2 = createCustomer();
        customer2.setActive(false);
        save(customer2);
        Act appointment1 = createAppointment(dateFrom, schedule1, customer1, true);
        Act appointment2 = createAppointment(dateFrom, schedule1, customer2, true);

        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();
        runJob(config, startDate, smsService);

        checkSent(appointment1);
        checkNotSent(appointment2, "Customer is inactive. No SMS will be sent");
    }

    /**
     * Verifies that the reminderError node is populated if a patient is inactive or deceased.
     *
     * @throws Exception for any error
     */
    @Test
    public void testInvalidPatient() throws Exception {
        Entity config = createJobConfig();

        Party customer1 = createCustomer();
        Party patient1 = TestHelper.createPatient(customer1);
        Party patient2 = TestHelper.createPatient(customer1);
        patient2.setActive(false);
        save(patient2);
        Party patient3 = TestHelper.createPatient(customer1);
        patientRules.setDeceased(patient3);
        Act appointment1 = createAppointment(dateFrom, schedule1, customer1, patient1, true);
        Act appointment2 = createAppointment(dateFrom, schedule1, customer1, patient2, true);
        Act appointment3 = createAppointment(dateFrom, schedule1, customer1, patient3, true);

        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();
        runJob(config, startDate, smsService);

        checkSent(appointment1);
        checkNotSent(appointment2, "Patient is inactive. No SMS will be sent");
        checkNotSent(appointment3, "Patient is deceased. No SMS will be sent");
    }


    /**
     * Verifies that the reminderError node is populated if a customer doesn't have an SMS contact.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCustomerNoSMSContact() throws Exception {
        Entity config = createJobConfig();

        Party customer1 = createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Act appointment1 = createAppointment(dateFrom, schedule1, customer1, true);
        Act appointment2 = createAppointment(dateFrom, schedule1, customer2, true);

        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();
        runJob(config, startDate, smsService);

        checkSent(appointment1);
        checkNotSent(appointment2, "Customer has no SMS contact");
    }

    /**
     * Verifies that the reminderError node is populated if no location can be linked to the appointment schedule.
     *
     * @throws Exception for any error
     */
    @Test
    public void testScheduleWithNoLocation() throws Exception {
        Entity config = createJobConfig();

        final Act appointment1 = createAppointment(dateFrom, schedule1, true);
        Act appointment2 = createAppointment(dateFrom, schedule2, true);

        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();

        IArchetypeRuleService archetypeService = (IArchetypeRuleService) getArchetypeService();
        LocationRules locationRules = new LocationRules(getArchetypeService());
        AppointmentReminderJob job = new AppointmentReminderJob(config, smsService, archetypeService, customerRules,
                                                                patientRules, practiceService, locationRules,
                                                                evaluator) {
            @Override
            protected Date getStartDate() {
                return startDate;
            }

            @Override
            protected int getPageSize() {
                return 5;
            }

            @Override
            protected boolean isPast(ActBean bean) {
                return false;
            }

            @Override
            protected Party getLocation(ActBean bean) {
                return bean.getAct().equals(appointment1) ? super.getLocation(bean) : null;
            }
        };
        job.execute(null);

        checkSent(appointment1);
        checkNotSent(appointment2, "Cannot determine the practice location of the appointment");
    }

    /**
     * Verifies that the reminderError node is populated if there is no practice reminder template, and none for the
     * schedule location.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLocationNoTemplate() throws Exception {
        IMObjectBean bean = new IMObjectBean(location1);
        bean.addNodeTarget("smsAppointment", template);
        save(location1, template);

        practiceService.setAppointmentSMSTemplate(null);

        Entity config = createJobConfig();
        Act appointment1 = createAppointment(dateFrom, schedule1, true);
        Act appointment2 = createAppointment(dateFrom, schedule2, true);


        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();
        runJob(config, startDate, smsService);

        checkSent(appointment1);
        checkNotSent(appointment2, "No appointment reminder template has been configured for Vets Be Us and " +
                                   "there is no default template.");
    }

    /**
     * Verifies that the reminderError node is populated if the expression produces no SMS text.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSMSEmptyText() throws Exception {
        practiceService.setAppointmentSMSTemplate(createTemplate("''"));
        Entity config = createJobConfig();
        Act appointment1 = createAppointment(dateFrom, schedule1, true);


        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();
        runJob(config, startDate, smsService);

        checkNotSent(appointment1, "Generated SMS text was empty");
    }

    /**
     * Verifies that the reminderError node is populated if the expression text longer than 160 characters.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSMSTextTooLong() throws Exception {
        String text = StringUtils.repeat("A", 161);
        practiceService.setAppointmentSMSTemplate(createTemplate("'" + text + "'"));
        Entity config = createJobConfig();
        Act appointment1 = createAppointment(dateFrom, schedule1, true);

        final Date startDate = DateRules.getDate(dateFrom, -2, DateUnits.DAYS);
        SMSService smsService = createSMSService();
        runJob(config, startDate, smsService);

        checkNotSent(appointment1, "SMS is too long: " + text);
    }

    /**
     * Creates a mock SMS service.
     *
     * @return a new service
     */
    private SMSService createSMSService() {
        SMSService smsService = Mockito.mock(SMSService.class);
        Mockito.when(smsService.getMaxParts()).thenReturn(1);
        return smsService;
    }

    /**
     * Verifies an appointment reminder has been sent.
     *
     * @param act the appointment act
     */
    private void checkSent(Act act) {
        ActBean bean = new ActBean(get(act));
        assertNotNull("Expected appointment on " + act.getActivityStartTime() + " to be sent",
                      bean.getDate("reminderSent"));
        assertNull("Expected appointment have no error", bean.getString("reminderError"));
    }

    /**
     * Verifies an appointment reminder has not been sent.
     *
     * @param act   the appointment act
     * @param error the expected error, or {@code null} if no error is expected
     */
    private void checkNotSent(Act act, String error) {
        ActBean bean = new ActBean(get(act));
        assertNull("Expected appointment on " + act.getActivityStartTime() + " to be unsent",
                   bean.getDate("reminderSent"));
        assertEquals(error, bean.getString("reminderError"));
    }

    /**
     * Runs the appointment reminder job.
     *
     * @param config     the job configuration
     * @param startDate  the date to start on
     * @param smsService the SMS service
     * @throws JobExecutionException for any job execution error
     */
    private void runJob(Entity config, final Date startDate, SMSService smsService) throws JobExecutionException {
        IArchetypeRuleService archetypeService = (IArchetypeRuleService) getArchetypeService();
        LocationRules locationRules = new LocationRules(getArchetypeService());
        AppointmentReminderJob job = new AppointmentReminderJob(config, smsService, archetypeService, customerRules,
                                                                patientRules, practiceService, locationRules,
                                                                evaluator) {
            @Override
            protected Date getStartDate() {
                return startDate;
            }

            @Override
            protected int getPageSize() {
                return 5;
            }

            @Override
            protected boolean isPast(ActBean bean) {
                return false;
            }
        };
        job.execute(null);
    }

    /**
     * Creates a new job configuration.
     *
     * @return a new job configuration
     */
    private Entity createJobConfig() {
        Entity config = (Entity) create("entity.jobAppointmentReminder");
        IMObjectBean bean = new IMObjectBean(config);
        bean.setValue("smsFrom", 2);
        bean.setValue("smsFromUnits", DateUnits.WEEKS.toString());
        bean.setValue("smsTo", 1);
        bean.setValue("smsToUnits", DateUnits.DAYS.toString());
        return config;
    }

    /**
     * Creates a new practice location linked to a schedule view.
     *
     * @param name         the location name
     * @param scheduleView the schedule view
     * @return a new practice location
     */
    private Party createLocation(String name, Entity scheduleView) {
        Party location = TestHelper.createLocation();
        location.setName(name);
        EntityBean locationBean = new EntityBean(location);
        locationBean.addNodeRelationship("scheduleViews", scheduleView);
        save(location, scheduleView);
        return location;
    }

    /**
     * Creates a new appointment schedule, configured to send reminders.
     *
     * @param location the practice location
     * @return a new schedule
     */
    private Entity createSchedule(Party location) {
        Entity schedule = ScheduleTestHelper.createSchedule(location);
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("sendReminders", true);
        bean.save();
        return schedule;
    }

    /**
     * Creates a new appointment.
     *
     * @param startTime    the appointment start time
     * @param schedule     the schedule
     * @param sendReminder if {@code true} indicates to send reminders
     * @return a new appointment
     */
    private Act createAppointment(Date startTime, Entity schedule, boolean sendReminder) {
        Party customer = createCustomer();
        return createAppointment(startTime, schedule, customer, sendReminder);
    }

    /**
     * Creates a new appointment.
     *
     * @param startTime    the appointment start time
     * @param schedule     the schedule
     * @param customer     the customer
     * @param sendReminder if {@code true} indicates to send reminders
     * @return a new appointment
     */
    private Act createAppointment(Date startTime, Entity schedule, Party customer, boolean sendReminder) {
        Party patient = TestHelper.createPatient(customer);
        return createAppointment(startTime, schedule, customer, patient, sendReminder);
    }

    /**
     * Creates a new appointment.
     *
     * @param startTime    the appointment start time
     * @param schedule     the schedule
     * @param customer     the customer
     * @param patient      the patient
     * @param sendReminder if {@code true} indicates to send reminders
     * @return a new appointment
     */
    private Act createAppointment(Date startTime, Entity schedule, Party customer, Party patient, boolean sendReminder) {
        Date endTime = DateRules.getDate(startTime, 1, DateUnits.HOURS);
        Act act = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, customer, patient);
        act.setStatus(AppointmentStatus.PENDING);
        ActBean bean = new ActBean(act);
        bean.setValue("sendReminder", sendReminder);
        bean.save();
        return act;
    }

    /**
     * Creates a new customer with an SMS contact.
     *
     * @return the new customer
     */
    private Party createCustomer() {
        Party customer = TestHelper.createCustomer();
        Contact telephone = customerRules.getContact(customer, ContactArchetypes.PHONE, null);
        assertNotNull(telephone);
        IMObjectBean phoneBean = new IMObjectBean(telephone);
        phoneBean.setValue("sms", true);
        phoneBean.setValue("telephoneNumber", "123456789");
        save(customer);
        return customer;
    }

    /**
     * Helper to create an appointment reminder SMS template.
     *
     * @return a new template
     */
    private Entity createTemplate() {
        return createTemplate("concat('Reminder: ', $location.name)");
    }

    /**
     * Helper to create an appointment reminder SMS template.
     *
     * @return a new template
     */
    private Entity createTemplate(String expression) {
        Entity template = (Entity) create(DocumentArchetypes.APPOINTMENT_SMS_TEMPLATE);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("name", "Test Appointment Reminder SMS Template");
        bean.setValue("contentType", "XPATH");
        bean.setValue("content", expression);
        bean.save();
        return template;
    }

    private class TestPracticeService extends PracticeService {

        /**
         * The practice locations.
         */
        private final List<Party> locations;

        /**
         * The default appointment reminder SMS template.
         */
        private Entity template;

        public TestPracticeService(Entity template, Party... locations) {
            super(getArchetypeService(), practiceRules, createPool());
            this.locations = Arrays.asList(locations);
            setAppointmentSMSTemplate(template);
        }

        @Override
        public List<Party> getLocations() {
            return locations;
        }

        @Override
        public Entity getAppointmentSMSTemplate() {
            return template;
        }

        public void setAppointmentSMSTemplate(Entity template) {
            this.template = template;
        }
    }

    private static ThreadPoolTaskExecutor createPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.afterPropertiesSet();
        return executor;
    }
}
