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

package org.openvpms.web.workspace.workflow.worklist;

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
 * Tool to generate tasks for all work lists linked to a work list view.
 *
 * @author Tim Anderson
 */
public class TaskGenerator {

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
     * Constructs an {@link TaskGenerator}.
     *
     * @param service   the archetype service
     * @param rules     the patient rules
     * @param userRules the user rules
     */
    public TaskGenerator(IArchetypeService service, PatientRules rules, UserRules userRules) {
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
     * Generates tasks for each work list in a work list view.
     *
     * @param name the view name
     * @param days the number of days to generate tasks for
     */
    public void generate(String name, int days) {
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.WORK_LIST_VIEW);
        query.add(Constraints.eq("name", name));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
        Date startDate = DateRules.getToday();
        if (iterator.hasNext()) {
            Entity view = iterator.next();
            IMObjectBean workListBean = new IMObjectBean(view, service);
            List<Entity> workLists = workListBean.getNodeTargetObjects("workLists", Entity.class,
                                                                       SequenceComparator.INSTANCE);
            for (Entity workList : workLists) {
                System.out.println("Generating tasks for work list: " + workList.getName());
                Date date = startDate;
                for (int i = 0; i < days; ++i) {
                    if (i != 0) {
                        date = DateRules.getNextDate(date);
                    }
                    for (int j = 0; j < 100; ++j) {
                        Patient patient = nextPatient();
                        Entity taskType = getTaskType(workList);
                        Act task = ScheduleTestHelper.createTask(date, null, workList, patient.customer,
                                                                 patient.patient, taskType, nextClinician(), null);
                        task.setStatus(WorkflowStatus.PENDING);
                        service.save(task);
                    }
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

            TaskGenerator generator = new TaskGenerator(context.getBean(IArchetypeService.class),
                                                        context.getBean(PatientRules.class),
                                                        context.getBean(UserRules.class));
            generator.generate(config.getString("view"), config.getInt("days"));
        }
    }

    /**
     * Returns a task type for a work list.
     *
     * @param workList the work list
     * @return the task type
     */
    private Entity getTaskType(Entity workList) {
        IMObjectBean bean = new IMObjectBean(workList, service);
        return (Entity) bean.getNodeTargetObject("taskTypes");
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
                                         .setLongFlag("view").setHelp("The work list view."));
        parser.registerParameter(new FlaggedOption("days").setShortFlag('d')
                                         .setLongFlag("days")
                                         .setStringParser(IntegerStringParser.getParser())
                                         .setHelp("The no. of days to generate tasks for."));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err
                .println("Usage: java " + TaskGenerator.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
