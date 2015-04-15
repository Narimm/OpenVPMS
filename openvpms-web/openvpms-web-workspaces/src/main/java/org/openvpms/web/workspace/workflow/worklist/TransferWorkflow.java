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

package org.openvpms.web.workspace.workflow.worklist;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.query.EntityObjectSetQuery;
import org.openvpms.web.component.im.query.EntityObjectSetResultSet;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.workflow.checkin.AbstractPrintPatientDocumentsTask;
import org.openvpms.web.workspace.workflow.checkin.PrintPatientActTask;


/**
 * Transfers a task from one worklist to another.
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
     * @param task    the task
     * @param context the context
     * @param help    the help context
     */
    public TransferWorkflow(Act task, Context context, HelpContext help) {
        super(help);

        ActBean bean = new ActBean(task);
        Party customer = (Party) bean.getNodeParticipant("customer");
        Party patient = (Party) bean.getNodeParticipant("patient");
        IMObjectReference workList = bean.getNodeParticipantRef("worklist");

        // copy rather than inherit the context to avoid falling back to global customer/patient if these are missing
        // in the task. This is required for printing/mailing.
        context = LocalContext.copy(context);
        context.setTask(task);
        context.setCustomer(customer);
        context.setPatient(patient);

        initial = new DefaultTaskContext(context, help);

        // exclude the work list being transferred from
        Query<Party> query = new EntityQuery<Party>(new WorkListQuery(workList), initial);

        addTask(new SelectIMObjectTask<Party>(query, help.topic("worklist")));
        addTask(new UpdateWorkListTask(task));

        addTask(new PrintPatientDocumentsTask(getHelpContext()));
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Sets the work list on an <em>act.customerTask</em>.
     */
    private static class UpdateWorkListTask extends EditIMObjectTask {

        /**
         * Creates a new {@code UpdateWorkListTask}.
         * The object is saved on update.
         *
         * @param act the task to update
         */
        public UpdateWorkListTask(Act act) {
            super(act, false);
        }

        /**
         * Edits an object in the background.
         *
         * @param editor  the editor
         * @param context the task context
         */
        @Override
        protected void edit(IMObjectEditor editor, TaskContext context) {
            super.edit(editor, context);
            if (editor instanceof TaskActEditor) {
                ((TaskActEditor) editor).setWorkList(context.getWorkList());
            }
        }
    }

    private static class PrintPatientDocumentsTask extends AbstractPrintPatientDocumentsTask {

        public PrintPatientDocumentsTask(HelpContext help) {
            super(PrintIMObjectTask.PrintMode.DEFAULT, help);
            setRequired(false);
        }

        /**
         * Determines if the task can be cancelled.
         *
         * @return {@code false}
         */
        @Override
        protected boolean canCancel() {
            return false;
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
         * @return the schedule, or {@code null} if there is no schedule
         */
        @Override
        protected Entity getSchedule(TaskContext context) {
            return null;
        }

        /**
         * Creates a task to print a document.
         *
         * @param document    the document to print
         * @param mailContext the mail context
         * @param printMode   the print mode
         * @return a new task
         */
        @Override
        protected PrintActTask createPrintTask(Act document, CustomerMailContext mailContext,
                                               PrintIMObjectTask.PrintMode printMode) {
            return new PrintTask(document, mailContext, printMode);
        }
    }

    private static class PrintTask extends PrintPatientActTask {

        /**
         * Constructs a {@link PrintTask}.
         *
         * @param act       the act to print
         * @param context   the mail context. May be {@code null}
         * @param printMode the print mode
         */
        public PrintTask(Act act, MailContext context, PrintMode printMode) {
            super(act, context, printMode);
        }

        /**
         * Returns the patient clinical event.
         *
         * @param document the document
         * @param context  the task context
         * @return the patient clinical event from the context, or {@code null} if none is present
         */
        @Override
        protected Act getEvent(Act document, TaskContext context) {
            Act event = null;
            ActBean bean = new ActBean(document);
            IMObjectReference patient = bean.getNodeParticipantRef("patient");
            if (patient != null) {
                MedicalRecordRules rules = ServiceHelper.getBean(MedicalRecordRules.class);
                event = rules.getEvent(patient, document.getActivityStartTime());
            }
            return event;
        }
    }

    private static class WorkListQuery extends EntityObjectSetQuery {

        private static final String[] SHORT_NAMES = new String[]{ScheduleArchetypes.ORGANISATION_WORKLIST};

        private IMObjectReference exclude;

        /**
         * Constructs a {@link WorkListQuery}.
         *
         * @param exclude the work list to exclude from results. May be {@code null}
         */
        public WorkListQuery(IMObjectReference exclude) {
            super(SHORT_NAMES);
            this.exclude = exclude;
            setAuto(true);
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
                                                getMaxResults(), isDistinct()) {
                @Override
                protected ArchetypeQuery createQuery() {
                    ArchetypeQuery query = super.createQuery();
                    if (exclude != null) {
                        query.add(Constraints.not(new ObjectRefConstraint("entity", exclude)));
                    }
                    return query;
                }
            };
        }
    }

}
