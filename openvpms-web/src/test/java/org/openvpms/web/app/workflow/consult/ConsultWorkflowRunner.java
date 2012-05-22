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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.workflow.consult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.workflow.FinancialWorkflowRunner;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.EditIMObjectTask;


/**
 * Runs the {@link ConsultWorkflow}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class ConsultWorkflowRunner extends FinancialWorkflowRunner<ConsultWorkflowRunner.TestWorkflow> {

    /**
     * The appointment/task.
     */
    private Act act;

    /**
     * Constructs a <tt>ConsultWorkflowRunner</tt> with an appointment/task.
     *
     * @param act      the appointment/task
     * @param practice the practice, used to determine tax rates
     * @param context  the context
     */
    public ConsultWorkflowRunner(Act act, Party practice, Context context) {
        super(practice);
        this.act = act;
        setWorkflow(new TestWorkflow(act, context));
    }

    /**
     * Verifies that the workflow is complete, and the appointment/task status matches that expected.
     *
     * @param status the expected status
     */
    public void checkComplete(String status) {
        assertNull(getTask());
        act = get(act);
        assertNotNull(act);
        assertEquals(status, act.getStatus());
    }

    /**
     * Verifies the context matches that expected
     *
     * @param context   the context to check
     * @param customer  the expected context customer. May be <tt>null</tt>
     * @param patient   the expected context patient. May be <tt>null</tt>
     * @param clinician the expected clinician. May be <tt>null</tt>
     */
    public void checkContext(Context context, Party customer, Party patient, User clinician) {
        assertEquals(customer, context.getCustomer());
        assertEquals(patient, context.getPatient());
        assertEquals(clinician, context.getClinician());
    }

    protected static class TestWorkflow extends ConsultWorkflow {

        /**
         * Constructs a new <tt>TestWorkflow</tt> from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
         *
         * @param act     the act
         * @param context the external context to access and update
         */
        public TestWorkflow(Act act, Context context) {
            super(act, context);
        }

        /**
         * Creates a new task to edit the invoice.
         *
         * @return a new task
         */
        protected EditIMObjectTask createEditInvoiceTask() {
            return new EditInvoiceTask();
        }
    }
}
