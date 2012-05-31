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

package org.openvpms.web.app.workflow;

import nextapp.echo2.app.Table;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.visit.VisitEditorDialog;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.ConfirmationTask;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.test.EchoTestHelper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Helper to run workflows.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class WorkflowRunner<T extends WorkflowImpl> {
    /**
     * The task tracker.
     */
    private TaskTracker tracker = new TaskTracker();

    /**
     * The workflow.
     */
    private T workflow;

    /**
     * Returns the current executing task.
     *
     * @return the current task. May be <tt>null</tt>
     */
    public Task getTask() {
        return tracker.getCurrent();
    }

    /**
     * Returns the workflow.
     *
     * @return the workflow
     */
    public T getWorkflow() {
        return workflow;
    }

    /**
     * Returns the workflow task context.
     *
     * @return the context
     */
    public TaskContext getContext() {
        return getWorkflow().getContext();
    }

    /**
     * Starts the workflow.
     */
    public void start() {
        workflow.start();
    }

    /**
     * Returns the current selection browser dialog.
     * <p/>
     * The current task must be a {@link SelectIMObjectTask}.
     *
     * @return the selection browser dialog
     */
    @SuppressWarnings("unchecked")
    public <T extends IMObject> BrowserDialog<T> getSelectionDialog() {
        Task current = getTask();
        assertTrue(current instanceof SelectIMObjectTask);
        return ((SelectIMObjectTask<T>) current).getBrowserDialog();
    }

    /**
     * Selects the specified button of a confirmation dialog.
     * <p/>
     * The current task must be a {@link ConfirmationTask}.
     *
     * @param button the button identifier. If <tt>null</tt>, use the <tt>userClose</tt> method.
     */
    public void confirm(String button) {
        Task current = tracker.getCurrent();
        assertTrue(current instanceof ConfirmationTask);
        ConfirmationTask post = (ConfirmationTask) current;
        ConfirmationDialog dialog = post.getConfirmationDialog();
        if (button != null) {
            EchoTestHelper.fireDialogButton(dialog, button);
        } else {
            dialog.userClose();
        }
    }

    /**
     * Returns the edit dialog of the current task.
     * <p/>
     * The current task must be an {@link EditIMObjectTask}.
     *
     * @return the edit dialog
     */
    public EditDialog getEditDialog() {
        EditIMObjectTask edit = getEditTask();
        return edit.getEditDialog();
    }

    /**
     * Returns the current edit task.
     *
     * @return the edit task
     */
    public EditIMObjectTask getEditTask() {
        Task current = getTask();
        assertTrue(current instanceof EditIMObjectTask);
        return (EditIMObjectTask) current;
    }

    /**
     * Returns the clinial event dialog.
     * <p/>
     * The current task must be an {@link EditVisitTask}.
     *
     * @return the dialog
     */
    public VisitEditorDialog editVisit() {
        Task current = getTask();
        assertTrue(current instanceof EditVisitTask);
        EditVisitTask edit = (EditVisitTask) current;
        return edit.getVisitDialog();
    }

    /**
     * Generates a table row selection event in a browser.
     *
     * @param browser the browser
     * @param object  the object to select. <em>Note:</em> must be present in the table
     */
    public void fireSelection(Browser<Party> browser, Party object) {
        browser.setSelected(object);

        // this is a bit brittle... TODO
        Table table = EchoTestHelper.findComponent(browser.getComponent(), Table.class);
        assertNotNull(table);
        table.processInput(Table.INPUT_ACTION, null);
    }

    /**
     * Registers the workflow.
     *
     * @param workflow the workflow
     */
    protected void setWorkflow(T workflow) {
        this.workflow = workflow;
        workflow.addTaskListener(tracker);
    }

    /**
     * Helper to reload an object.
     *
     * @param object the object to reload. May be <tt>null</tt>
     * @return the reloaded object. May be <tt>null</tt>
     */
    protected <T extends IMObject> T get(T object) {
        return IMObjectHelper.reload(object);
    }
}
