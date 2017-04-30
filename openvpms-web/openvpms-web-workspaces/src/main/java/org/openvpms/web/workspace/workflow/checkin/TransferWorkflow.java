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

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.EntityObjectSetQuery;
import org.openvpms.web.component.im.query.EntityObjectSetResultSet;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.Variable;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.workflow.AppointmentStatus.ADMITTED;
import static org.openvpms.archetype.rules.workflow.WorkflowStatus.BILLED;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;


/**
 * Transfers the patient associated with an appointment to a work-list.
 *
 * @author Tim Anderson
 */
public class TransferWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;


    /**
     * Constructs a {@link TransferWorkflow}.
     *
     * @param appointment the appointment
     * @param context     the context
     * @param help        the help context
     */
    public TransferWorkflow(Act appointment, Context context, HelpContext help) {
        super(help);
        String status = appointment.getStatus();
        ActBean bean = new ActBean(appointment);
        Party customer = (Party) bean.getNodeParticipant("customer");
        Party patient = (Party) bean.getNodeParticipant("patient");
        Party location = context.getLocation();
        if (customer == null) {
            throw new ContextException(ContextException.ErrorCode.NoCustomer);
        }
        if (patient == null) {
            throw new ContextException(ContextException.ErrorCode.NoPatient);
        }
        if (location == null) {
            throw new ContextException(ContextException.ErrorCode.NoLocation);
        }

        context = LocalContext.copy(context);
        context.setAppointment(appointment);
        context.setCustomer(customer);
        context.setPatient(patient);
        context.setLocation(location);
        context.setWorkList(null);
        context.setObject(PatientArchetypes.CLINICAL_EVENT, null);

        initial = new DefaultTaskContext(context, help);

        // select a work list
        Query<Party> query = new EntityQuery<>(new WorkListQuery(location), initial);
        addTask(new SelectIMObjectTask<>(query, help.topic("worklist")));

        // display a prompt to print documents associate with the work list. This allows the user to cancel
        // the work flow if the wrong work list was selected.
        addTask(new PrintPatientDocumentsTask(getHelpContext()));

        // create and edit an act.customerTask
        TaskProperties taskProps = new TaskProperties();
        String taskStatus = (IN_PROGRESS.equals(status) || BILLED.equals(status) || COMPLETED.equals(status))
                            ? status : IN_PROGRESS;
        taskProps.add("status", taskStatus);
        taskProps.add(new Variable("startTime") {
            public Object getValue(TaskContext context) {
                return new Date();
            }
        });
        addTask(new EditIMObjectTask(ScheduleArchetypes.TASK, taskProps, false));

        // add a Flow Sheet for ADMITTED/IN_PROGRESS appointments, if required by the work list
        if (IN_PROGRESS.equals(status) || ADMITTED.equals(status)) {
            FlowSheetServiceFactory factory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
            if (factory.isSmartFlowSheetEnabled(location)) {
                addTask(new AddFlowSheetTask(factory, help));
            }
        }

        // update the appointment status to ADMITTED if it is not ADMITTED, BILLED, or COMPLETED
        if (!ADMITTED.equals(status) && !BILLED.equals(status) && !COMPLETED.equals(status)) {
            TaskProperties properties = new TaskProperties();
            properties.add("status", ADMITTED);
            addTask(new UpdateIMObjectTask(appointment, properties));
        }
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    private static class PrintPatientDocumentsTask extends AbstractPrintPatientDocumentsTask {

        public PrintPatientDocumentsTask(HelpContext help) {
            super(PrintIMObjectTask.PrintMode.DEFAULT, help);
            setRequired(false);
        }

        /**
         * Returns the work list to use to locate templates.
         *
         * @param context the context
         * @return the work list, or {@code null} if there is no work list
         */
        @Override
        protected Entity getWorkList(TaskContext context) {
            return context.getWorkList();
        }

        /**
         * Returns the schedule to use to locate templates.
         *
         * @param context the context
         * @return {@code null}. The work list is used instead
         */
        @Override
        protected Entity getSchedule(TaskContext context) {
            return null;
        }
    }

    /**
     * Queries work lists, constraining them to those available at the specified location.
     */
    private static class WorkListQuery extends EntityObjectSetQuery {

        /**
         * The location.
         */
        private final Party location;

        /**
         * The archetypes to query.
         */
        private static final String[] SHORT_NAMES = new String[]{ScheduleArchetypes.ORGANISATION_WORKLIST};

        /**
         * Constructs a {@link WorkListQuery}.
         *
         * @param location the location to restrict work lists to
         */
        public WorkListQuery(Party location) {
            super(SHORT_NAMES);
            setAuto(true);
            this.location = location;
        }

        /**
         * Creates the result set.
         *
         * @param sort the sort criteria. May be {@code null}
         * @return a new result set
         */
        @Override
        protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
            return new EntityObjectSetResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), sort,
                                                getMaxResults(), true) {
                @Override
                protected ArchetypeQuery createQuery() {
                    ArchetypeQuery query = super.createQuery();
                    ObjectRefConstraint locationFilter = new ObjectRefConstraint("location",
                                                                                 location.getObjectReference());
                    locationFilter.add(join("workListViews").add(join("target", "wlv").add(join("workLists", "wl").add(
                            idEq("wl.target", "entity")))));
                    query.add(locationFilter);
                    return query;
                }
            };
        }
    }

}
