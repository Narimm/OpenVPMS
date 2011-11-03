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

package org.openvpms.web.component.workflow;


/**
 * A collection of {@link Task}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Tasks extends AbstractTask {

    /**
     * The workflow.
     */
    private WorkflowImpl workflow = new WorkflowImpl();

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
     * Starts the task, using a {@link DefaultTaskContext} that inherits
     * from the global context.
     */
    public void start() {
        start(new DefaultTaskContext());
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
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
