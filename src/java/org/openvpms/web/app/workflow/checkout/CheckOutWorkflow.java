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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.workflow.ConditionalTask;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskContextImpl;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.resource.util.Messages;


/**
 * Check-out workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CheckOutWorkflow extends WorkflowImpl {

    /**
     * The invoice short name.
     */
    public static final String INVOICE_SHORTNAME
            = "act.customerAccountChargesInvoice";

    /**
     * The initial context.
     */
    private TaskContext initial;


    /**
     * Constructs a new <code>CheckOutWorkflow</code> from an
     * <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act the act
     */
    public CheckOutWorkflow(Act act) {
        ActBean bean = new ActBean(act);
        Party customer = (Party) bean.getParticipant("participation.customer");
        Party patient = (Party) bean.getParticipant("participation.patient");
        final User clinician
                = (User) bean.getParticipant("participation.clinician");

        initialise(customer, patient, clinician);

        // update the act status
        TaskProperties appProps = new TaskProperties();
        appProps.add("status", "Completed");
        addTask(new UpdateIMObjectTask(act, appProps));
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
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

        String payTitle = Messages.get("workflow.checkout.payaccount.title");
        String payMsg = Messages.get("workflow.checkout.payaccount.message");
        postTasks.addTask(new ConditionalTask(
                new ConfirmationTask(payTitle, payMsg),
                new EditIMObjectTask("act.customerAccountPayment", true)));

        String invoiceTitle = Messages.get(
                "workflow.checkout.postinvoice.title");
        String invoiceMsg = Messages.get(
                "workflow.checkout.postinvoice.message");
        ConditionalTask post = new ConditionalTask(new ConfirmationTask(
                invoiceTitle, invoiceMsg), postTasks);
        addTask(post);
        addTask(new PrintDocumentsTask());
    }

}
