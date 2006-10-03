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

package org.openvpms.web.app.workflow.checkout;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.CreateIMObjectTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.EvalTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskContextImpl;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;

import java.util.ArrayList;
import java.util.List;


/**
 * Check-out workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CheckOutWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;

    /**
     * The invoice short name.
     */
    private static final String INVOICE_SHORTNAME
            = "act.customerAccountChargesInvoice";


    /**
     * Constructs a new <code>CheckOutWorkflow</code> from a task.
     *
     * @param task the task
     */
    public CheckOutWorkflow(Act task) {
        ActBean bean = new ActBean(task);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        final User clinician
                = (User) bean.getParticipant("participation.clinician");

        initialise(customer, patient, clinician);

        // update the task status
        TaskProperties appProps = new TaskProperties();
        appProps.add("status", "Completed");
        addTask(new UpdateIMObjectTask(task, appProps));
    }

    /**
     * Initialise the workflow.
     *
     * @param customer  the customer
     * @param patient   the patient
     * @param clinician the clinician. May be <code>null</code>
     */
    private void initialise(Party customer, Party patient, User clinician) {
        initial = new TaskContextImpl();
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);

        // get/create the invoice, and edit it
        addTask(new InvoiceTask());
        addTask(new EditIMObjectTask(INVOICE_SHORTNAME));

        // on save, determine if the user wants to post the invoice
        Tasks postTasks = new Tasks();
        TaskProperties invoiceProps = new TaskProperties();
        invoiceProps.add("status", "Posted");
        postTasks.addTask(
                new UpdateIMObjectTask(INVOICE_SHORTNAME, invoiceProps));
        postTasks.addTask(new ConditionalTask(
                new ConfirmationTask("Pay", "Pay"),
                new EditIMObjectTask("act.customerAccountPayment", true)));
        ConditionalTask post = new ConditionalTask(new ConfirmationTask(
                "Post invoice?", "Do you wish to post the invoice?"),
                                                   postTasks);
        addTask(post);
        addTask(new PrintUnprintedDocs());
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    private static class InvoiceTask extends CreateIMObjectTask {

        public InvoiceTask() {
            super(INVOICE_SHORTNAME);
        }

        /**
         * Starts the task.
         * <p/>
         * The registered {@link TaskListener} will be notified on completion or
         * failure.
         *
         * @param context the task context
         */
        @Override
        public void start(final TaskContext context) {
            ArchetypeQuery query = new ArchetypeQuery(getShortNames(), false,
                                                      true);
            query.setFirstRow(0);
            query.setNumOfRows(1);

            Party customer = context.getCustomer();
            CollectionNodeConstraint participations
                    = new CollectionNodeConstraint("customer",
                                                   "participation.customer",
                                                   false, true);
            participations.add(new ObjectRefNodeConstraint(
                    "entity", customer.getObjectReference()));

            query.add(participations);
            OrConstraint or = new OrConstraint();
            or.add(new NodeConstraint("status", "In Progress"));
            or.add(new NodeConstraint("status", "Completed"));
            query.add(or);

            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            List<IMObject> result = service.get(query).getRows();
            if (result.isEmpty()) {
                super.start(context);
            } else {
                Act invoice = (Act) result.get(0);
                context.addObject(invoice);
            }
        }
    }

    private static class PrintUnprintedDocs extends EvalTask<List<IMObject>> {

        /**
         * Starts the task.
         * <p/>
         * The registered {@link TaskListener} will be notified on completion or
         * failure.
         *
         * @param context the task context
         */
        public void start(TaskContext context) {
            List<IMObject> unprinted = new ArrayList<IMObject>();
            unprinted.addAll(getCustomerActs(context));
            unprinted.addAll(getPatientActs(context));
            if (unprinted.isEmpty()) {
                notifyCompleted();
            } else {
                BatchPrintDialog dialog = new BatchPrintDialog("Print",
                                                               unprinted);
                dialog.addWindowPaneListener(new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent event) {
                        notifyCompleted();
                    }
                });
                dialog.show();
            }
        }

        private List<IMObject> getCustomerActs(TaskContext context) {
            String[] shortNames = {"act.customerAccountCharges*",
                                   "act.customerAccountPayment"};
            Party customer = context.getCustomer();
            String node = "customer";
            String participation = "participation.customer";
            return getUnprintedActs(shortNames, customer, node, participation);
        }

        private List<IMObject> getPatientActs(TaskContext context) {
            String[] shortNames = {"act.patientDocument*"};
            Party patient = context.getPatient();
            String node = "patient";
            String participation = "participation.patient";
            return getUnprintedActs(shortNames, patient, node, participation);
        }

        private List<IMObject> getUnprintedActs(String[] shortNames,
                                                IMObject entity,
                                                String node,
                                                String participation) {
            ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true);
            query.setFirstRow(0);
            query.setNumOfRows(ArchetypeQuery.ALL_ROWS);

            CollectionNodeConstraint participations
                    = new CollectionNodeConstraint(node, participation,
                                                   false, true);
            participations.add(new ObjectRefNodeConstraint(
                    "entity", entity.getObjectReference()));

            query.add(participations);
            query.add(new NodeConstraint("printed", false));

            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            return service.get(query).getRows();

        }
    }
}
