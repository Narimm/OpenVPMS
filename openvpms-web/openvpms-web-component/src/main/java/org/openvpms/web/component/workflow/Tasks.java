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
 */

package org.openvpms.web.component.workflow;


import org.openvpms.web.echo.help.HelpContext;

/**
 * A collection of {@link Task}s.
 *
 * @author Tim Anderson
 */
public class Tasks extends AbstractTask {

    /**
     * The workflow.
     */
    private final WorkflowImpl workflow;

    /**
     * Constructs a {@code Tasks}.
     *
     * @param help the help context
     */
    public Tasks(HelpContext help) {

        workflow = new WorkflowImpl(help);
    }

    /**
     * Adds a task to execute.
     *
     * @param task the task to execute
     */
    public void addTask(Task task) {
        workflow.addTask(task);
    }

    /**
     * Determines if skipping a task should cause the workflow to terminate.
     *
     * @param breakOnSkip if <code>true</code> terminate the workflow if a task
     *                    is skipped
     */
    public void setBreakOnSkip(boolean breakOnSkip) {
        workflow.setBreakOnSkip(breakOnSkip);
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    public void start(TaskContext context) {
        workflow.addTaskListener(new DefaultTaskListener() {

            @Override
            public void taskEvent(TaskEvent event) {
                switch (event.getType()) {
                    case SKIPPED:
                        notifySkipped();
                        break;
                    case CANCELLED:
                        notifyCancelled();
                        break;
                    case COMPLETED:
                        notifyCompleted();
                }
            }
        });
        start(workflow, context);
    }

}
