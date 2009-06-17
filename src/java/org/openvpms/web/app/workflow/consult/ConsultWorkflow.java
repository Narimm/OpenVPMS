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
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.workflow.GetClinicalEventTask;
import org.openvpms.web.app.workflow.GetInvoiceTask;
import static org.openvpms.web.app.workflow.GetInvoiceTask.INVOICE_SHORTNAME;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.workflow.ConditionalCreateTask;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditAccountActTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.NodeConditionTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;


/**
 * Consult workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ConsultWorkflow extends WorkflowImpl {

    /**
     * The event short name.
     */
    public static final String EVENT_SHORTNAME = "act.patientClinicalEvent";

    /**
     * The initial context.
     */
    private TaskContext initial;


    /**
     * Constructs a new <code>ConsultWorkflow</code> from an
     * <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act the act
     */
    public ConsultWorkflow(Act act) {
        // update the act status
        TaskProperties appProps = new TaskProperties();
        appProps.add("status", ActStatus.IN_PROGRESS);
        addTask(new UpdateIMObjectTask(act, appProps));

        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        User clinician = (User) bean.getParticipant("participation.clinician");
        final GlobalContext global = GlobalContext.getInstance();
        if (clinician == null) {
            clinician = global.getClinician();
        }

        initial = new DefaultTaskContext(false);
        initial.setCustomer(customer);
        initial.setPatient(patient);
        initial.setClinician(clinician);
        initial.setUser(global.getUser());
        initial.setPractice(global.getPractice());
        initial.setLocation(global.getLocation());

        // get the latest clinical event, or create one if none is available
        // and edit it.
        addTask(new GetClinicalEventTask());
        addTask(new ConditionalCreateTask(EVENT_SHORTNAME));
        addTask(new EditIMObjectTask(EVENT_SHORTNAME));

        // get the latest invoice, or create one if none is available, and edit
        // it
        addTask(new GetInvoiceTask());
        addTask(new ConditionalCreateTask(INVOICE_SHORTNAME));
        addTask(new EditAccountActTask(INVOICE_SHORTNAME));

        // update the task/appointment status to BILLED if the invoice
        // is COMPLETED
        NodeConditionTask<String> invoiceCompleted
                = new NodeConditionTask<String>(INVOICE_SHORTNAME,
                                                "status", ActStatus.COMPLETED);
        TaskProperties billProps = new TaskProperties();
        billProps.add("status", WorkflowStatus.BILLED);
        UpdateIMObjectTask billTask = new UpdateIMObjectTask(act, billProps,
                                                             true);
        addTask(new ConditionalTask(invoiceCompleted, billTask));

        // add a task to update the global context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                global.setCustomer(context.getCustomer());
                global.setPatient(context.getPatient());
                global.setClinician(context.getClinician());
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

}
