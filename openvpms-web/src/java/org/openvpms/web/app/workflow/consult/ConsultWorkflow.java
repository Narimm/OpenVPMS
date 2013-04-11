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

package org.openvpms.web.app.workflow.consult;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.workflow.EditVisitTask;
import org.openvpms.web.app.workflow.GetClinicalEventTask;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.NodeConditionTask;
import org.openvpms.web.component.workflow.NodeInTask;
import org.openvpms.web.component.workflow.ReloadTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;


/**
 * Consult workflow.
 *
 * @author Tim Anderson
 */
public class ConsultWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;


    /**
     * Constructs a {@code ConsultWorkflow} from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act      the act
     * @param external the external context to access and update
     * @param help     the help context
     */
    public ConsultWorkflow(Act act, final Context external, HelpContext help) {
        super(help);
        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        User clinician = external.getClinician();

        initial = new DefaultTaskContext(help, false);
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);
        initial.setUser(external.getUser());
        initial.setPractice(external.getPractice());
        initial.setLocation(external.getLocation());
        initial.addObject(act);

        // update the act status to IN_PROGRESS if its not BILLED or COMPLETED
        addTask(createInProgressTask(act));

        addTask(new GetClinicalEventTask(act.getActivityStartTime()));

        // get the latest invoice, possibly associated with the event. If none exists, creates a new one
        addTask(new GetConsultInvoiceTask());
        addTask(new ConditionalCreateTask(CustomerAccountArchetypes.INVOICE));

        // get the latest clinical event and edit it.
        addTask(createEditVisitTask());

        // Reload the task to refresh the context with any edits made
        addTask(new ReloadTask(PatientArchetypes.CLINICAL_EVENT));

        // update the task/appointment status to BILLED if the invoice is COMPLETED
        addTask(createBilledTask(act));

        // add a task to update the global context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                external.setCustomer(context.getCustomer());
                external.setPatient(context.getPatient());
                external.setClinician(context.getClinician());
            }
        });
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Creates a new {@link EditVisitTask}.
     *
     * @return a new task to edit the visit
     */
    protected EditVisitTask createEditVisitTask() {
        return new EditVisitTask();
    }


    /**
     * Creates a task to update the appointment/task act status to {@code IN_PROGRESS} if it is not {@code BILLED} or
     * {@code COMPLETED}.
     *
     * @param act the appointment or task act
     * @return a new task
     */
    private Task createInProgressTask(Act act) {
        String shortName = act.getArchetypeId().getShortName();
        NodeInTask<String> notBilledOrCompleted
                = new NodeInTask<String>(shortName, "status", true, WorkflowStatus.BILLED, WorkflowStatus.COMPLETED);
        TaskProperties billProps = new TaskProperties();
        billProps.add("status", WorkflowStatus.IN_PROGRESS);
        UpdateIMObjectTask billTask = new UpdateIMObjectTask(shortName, billProps, true);
        return new ConditionalTask(notBilledOrCompleted, billTask);
    }

    /**
     * Creates a task to update the appointment/task act status to {@code BILLED} if the invoice is {@code COMPLETED}.
     *
     * @param act the appointment or task act
     * @return a new task
     */
    private Task createBilledTask(Act act) {
        String shortName = act.getArchetypeId().getShortName();
        NodeConditionTask<String> invoiceCompleted
                = new NodeConditionTask<String>(CustomerAccountArchetypes.INVOICE, "status", ActStatus.COMPLETED);
        TaskProperties billProps = new TaskProperties();
        billProps.add("status", WorkflowStatus.BILLED);
        UpdateIMObjectTask billTask = new UpdateIMObjectTask(shortName, billProps, true);
        return new ConditionalTask(invoiceCompleted, billTask);
    }

}
