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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.otc;

import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.workflow.WorkflowRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

/**
 * Runs an {@link OverTheCounterWorkflow}, for testing purposes.
 *
 * @author Tim Anderson
 */
public class OverTheCounterWorkflowRunner extends WorkflowRunner<OverTheCounterWorkflowRunner.TestOTCWorkflow> {

    /**
     * Constructs an {@link OverTheCounterWorkflow}.
     *
     * @param context the context
     */
    public OverTheCounterWorkflowRunner(Context context) {
        setWorkflow(new TestOTCWorkflow(context, new HelpContext("foo", null)));
    }

    /**
     * Returns the charge task.
     *
     * @return the charge task
     */
    public TestOTCChargeTask getChargeTask() {
        return (TestOTCChargeTask) getEditTask();
    }

    /**
     * Returns the charge editor.
     *
     * @return the charge editor
     */
    public OTCChargeEditor getChargeEditor() {
        return (OTCChargeEditor) getChargeTask().getEditDialog().getEditor();
    }

    /**
     * Returns the payment task.
     *
     * @return the payment task
     */
    public OTCPaymentTask getPaymentTask() {
        return (OTCPaymentTask) getEditTask();
    }

    /**
     * Returns the payment editor.
     *
     * @return the payment editor
     */
    public OTCPaymentEditor getPaymentEditor() {
        return (OTCPaymentEditor) getPaymentTask().getEditDialog().getEditor();
    }

    /**
     * Verifies that the current task is a {@link PrintIMObjectTask}, and skips the dialog.
     */
    public void print() {
        TestOTCPrintTask task = (TestOTCPrintTask) getTask();
        assertNotNull(task.getPrintDialog());
        fireDialogButton(task.getPrintDialog(), PopupDialog.SKIP_ID);
    }

    /**
     * Verifies that the workflow is completed.
     */
    public void checkComplete() {
        assertNull(getTask());
    }

    public static class TestOTCWorkflow extends OverTheCounterWorkflow {

        /**
         * Constructs a {@code TestOTCWorkflow}.
         *
         * @param parent the parent context
         * @param help   the help context
         * @throws ArchetypeServiceException for any archetype service error
         * @throws OTCException              for any OTC error
         */
        public TestOTCWorkflow(Context parent, HelpContext help) {
            super(parent, help);
        }

        /**
         * Creates a task to edit the charge.
         *
         * @return a new task
         */
        @Override
        protected EditIMObjectTask createChargeTask() {
            return new TestOTCChargeTask();
        }

        /**
         * Creates a task to print the charge.
         *
         * @param parent the parent context
         * @return a new task
         */
        @Override
        protected PrintIMObjectTask createPrintTask(Context parent) {
            return new TestOTCPrintTask(parent);
        }

    }

}
