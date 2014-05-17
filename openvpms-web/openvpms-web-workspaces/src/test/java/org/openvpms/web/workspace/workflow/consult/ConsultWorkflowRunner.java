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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.consult;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.patient.visit.VisitEditor;
import org.openvpms.web.workspace.patient.visit.VisitEditorDialog;
import org.openvpms.web.workspace.workflow.EditVisitTask;
import org.openvpms.web.workspace.workflow.FinancialWorkflowRunner;
import org.openvpms.web.workspace.workflow.TestEditVisitTask;
import org.openvpms.web.workspace.workflow.TestVisitCRUDWindow;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Runs the {@link ConsultWorkflow}.
 *
 * @author Tim Anderson
 */
class ConsultWorkflowRunner extends FinancialWorkflowRunner<ConsultWorkflowRunner.TestWorkflow> {

    /**
     * The appointment/task.
     */
    private Act act;

    /**
     * Constructs a {@code ConsultWorkflowRunner} with an appointment/task.
     *
     * @param act      the appointment/task
     * @param practice the practice, used to determine tax rates
     * @param context  the context
     */
    public ConsultWorkflowRunner(Act act, Party practice, Context context) {
        super(practice);
        context.setPractice(practice);
        this.act = act;
        setWorkflow(new TestWorkflow(act, context));
    }

    public void addNote() {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        VisitEditorDialog dialog = task.getVisitDialog();

        // get the editor and add an item
        VisitEditor visitEditor = dialog.getEditor();
        TestVisitCRUDWindow window = (TestVisitCRUDWindow) visitEditor.getHistoryWindow();
        assertNotNull(window);
        window.addNote();
    }

    /**
     * Performs the "Add Visit & Note" operation.
     *
     * @return the added note
     */
    public Act addVisitAndNote() {
        TestEditVisitTask task = (TestEditVisitTask) getTask();
        VisitEditorDialog dialog = task.getVisitDialog();

        // get the editor
        VisitEditor visitEditor = dialog.getEditor();
        TestVisitCRUDWindow window = (TestVisitCRUDWindow) visitEditor.getHistoryWindow();
        assertNotNull(window);
        Act note = window.addVisitAndNote();
        return get(note);  // need to reload as the Retryer loads it before linking it to the event
    }

    /**
     * Verifies that the current task is an EditVisitTask, and adds an invoice item.
     *
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return the invoice total
     */
    public BigDecimal addVisitInvoiceItem(Party patient, User clinician) {
        BigDecimal amount = BigDecimal.valueOf(20);
        addVisitInvoiceItem(patient, amount, clinician);
        return getInvoice().getTotal();
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
     * @param customer  the expected context customer. May be {@code null}
     * @param patient   the expected context patient. May be {@code null}
     * @param clinician the expected clinician. May be {@code null}
     */
    public void checkContext(Context context, Party customer, Party patient, User clinician) {
        assertEquals(customer, context.getCustomer());
        assertEquals(patient, context.getPatient());
        assertEquals(clinician, context.getClinician());
    }

    protected static class TestWorkflow extends ConsultWorkflow {

        /**
         * Constructs a new {@code TestWorkflow} from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
         *
         * @param act     the act
         * @param context the external context to access and update
         */
        public TestWorkflow(Act act, Context context) {
            super(act, context, new HelpContext("foo", null));
        }

        /**
         * Creates a new {@link EditVisitTask}.
         *
         * @return a new task to edit the visit
         */
        @Override
        protected EditVisitTask createEditVisitTask() {
            return new TestEditVisitTask();
        }
    }


}
