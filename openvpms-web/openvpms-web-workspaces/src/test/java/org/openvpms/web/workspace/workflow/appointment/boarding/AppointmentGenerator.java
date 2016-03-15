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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.boarding;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Tool to generate appointments for all schedules linked to a schedule view.
 *
 * @author Tim Anderson
 */
public class AppointmentGenerator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The patient iterator.
     */
    private IMObjectQueryIterator<Party> patientIterator;

    /**
     * The available clinicians.
     */
    private List<User> clinicians = new ArrayList<>();

    /**
     * Iterator over the clinicians.
     */
    private Iterator<User> clinicianIterator;

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * Associates a customer and patient.
     */
    private static class Patient {

        private final Party patient;
        private final Party customer;

        public Patient(Party patient, Party customer) {
            this.patient = patient;
            this.customer = customer;
        }
    }

    /**
     * Constructs an {@link AppointmentGenerator}.
     *
     * @param service   the archetype service
     * @param rules     the patient rules
     * @param userRules the user rules
     */
    public AppointmentGenerator(IArchetypeService service, PatientRules rules, UserRules userRules) {
        this.service = service;
        this.rules = rules;
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER);
        IMObjectQueryIterator<User> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (userRules.isClinician(user)) {
                clinicians.add(user);
            }
        }
    }

    /**
     * Generates appointments for each schedule in a schedule view.
     *
     * @param name the view name
     * @param days the number of days to generate appointments for
     */
    public void generate(String name, int days) {
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.SCHEDULE_VIEW);
        query.add(Constraints.eq("name", name));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
        Date startDate = DateRules.getToday();
        if (iterator.hasNext()) {
            Entity view = iterator.next();
            IMObjectBean scheduleBean = new IMObjectBean(view, service);
            List<Entity> schedules = scheduleBean.getNodeTargetObjects("schedules", Entity.class,
                                                                       SequenceComparator.INSTANCE);
            for (Entity schedule : schedules) {
                System.out.println("Generating appointments for schedule: " + schedule.getName());
                Date date = startDate;
                for (int i = 0; i < days; ++i) {
                    if (i != 0) {
                        date = DateRules.getNextDate(date);
                    }
                    Patient patient = nextPatient();
                    Entity appointmentType = getAppointmentType(schedule);
                    Act appointment = ScheduleTestHelper.createAppointment(date, DateRules.getNextDate(date),
                                                                           schedule, appointmentType, patient.customer,
                                                                           patient.patient, nextClinician(), null);
                    appointment.setStatus(WorkflowStatus.PENDING);
                    service.save(appointment);
                }
            }
        } else {
            System.out.println("No view named " + name);
        }
    }

    /**
     * Main line.
     *
     * @param args command line arguments
     * @throws Exception for any error
     */
    public static void main(String[] args) throws Exception {
        JSAP parser = createParser();
        JSAPResult config = parser.parse(args);
        if (!config.success()) {
            displayUsage(parser);
        } else {
            String contextPath = config.getString("context");
            ApplicationContext context;
            if (!new File(contextPath).exists()) {
                context = new ClassPathXmlApplicationContext(contextPath);
            } else {
                context = new FileSystemXmlApplicationContext(contextPath);
            }

            AppointmentGenerator generator = new AppointmentGenerator(context.getBean(IArchetypeService.class),
                                                                      context.getBean(PatientRules.class),
                                                                      context.getBean(UserRules.class));
            generator.generate(config.getString("view"), config.getInt("days"));
        }
    }

    /**
     * Returns an appointment type for a schedule.
     *
     * @param schedule the schedule
     * @return the appointment type
     */
    private Entity getAppointmentType(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule, service);
        return (Entity) bean.getNodeTargetObject("appointmentTypes");
    }

    /**
     * Returns the next patient.
     *
     * @return the next patient
     */
    private Patient nextPatient() {
        boolean all = false;
        if (patientIterator == null || !patientIterator.hasNext()) {
            all = true;
            patientIterator = createPatientIterator();
        }
        Patient result = getNext(patientIterator);
        if (result == null && !all) {
            patientIterator = createPatientIterator();
            result = getNext(patientIterator);
        }
        if (result == null) {
            throw new IllegalStateException("No patients found");
        }

        return result;
    }

    /**
     * Returns the next clinician.
     *
     * @return the next clinician. May be {@code null}
     */
    private User nextClinician() {
        if (clinicianIterator == null || !clinicianIterator.hasNext()) {
            clinicianIterator = clinicians.iterator();
        }
        if (clinicianIterator.hasNext()) {
            return clinicianIterator.next();
        }
        return null;
    }

    /**
     * Returns the next patient.
     *
     * @param patients the patient iterator
     * @return the next patient
     */
    private Patient getNext(IMObjectQueryIterator<Party> patients) {
        Patient result = null;
        while (patients.hasNext()) {
            Party patient = patients.next();
            Party customer = rules.getOwner(patient);
            if (customer != null && customer.isActive()) {
                result = new Patient(patient, customer);
                break;
            }
        }
        return result;
    }

    /**
     * Creates a new patient iterator.
     *
     * @return the patient iterator
     */
    private IMObjectQueryIterator<Party> createPatientIterator() {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.PATIENT);
        query.add(Constraints.sort("id"));
        return new IMObjectQueryIterator<>(service, query);
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                                         .setLongFlag("context")
                                         .setDefault(APPLICATION_CONTEXT)
                                         .setHelp("Application context path"));
        parser.registerParameter(new FlaggedOption("view").setShortFlag('v')
                                         .setLongFlag("view").setHelp("The schedule view."));
        parser.registerParameter(new FlaggedOption("days").setShortFlag('d')
                                         .setLongFlag("days")
                                         .setStringParser(IntegerStringParser.getParser())
                                         .setHelp("The no. of days to generate appointments for."));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err
                .println("Usage: java " + AppointmentGenerator.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
