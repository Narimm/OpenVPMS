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

import org.openvpms.web.component.im.util.ErrorHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Default implementation of the {@link Workflow} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkflowImpl extends AbstractTask implements Workflow {

    /**
     * The tasks to execute.
     */
    private final List<Task> tasks = new ArrayList<Task>();

    /**
     * The task context.
     */
    private TaskContext context;

    /**
     * Iterator over the tasks.
     */
    private Iterator<Task> taskIterator;

    /**
     * Determines if the workflow should cancel.
     */
    private boolean cancel;

    /**
     * The listener to handle task events.
     */
    private final TaskListener taskListener;


    /**
     * Constructs a new <code>WorkflowImpl</code>.
     */
    public WorkflowImpl() {
        taskListener = new TaskListener() {
            public void taskEvent(TaskEvent event) {
                onEvent(event);
            }
        };
    }

    /**
     * Adds a task to the workflow.
     *
     * @param task the task to add
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Starts the workflow.
     */
    public void start() {
        start(new TaskContextImpl());
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
        cancel = false;
        taskIterator = tasks.iterator();
        this.context = context;
        next();
    }

    /**
     * Executes the next task.
     */
    protected void next() {
        if (cancel) {
            notifyCancelled();
        } else if (taskIterator.hasNext()) {
            Task task = taskIterator.next();

            try {
                task.setTaskListener(taskListener);
                task.start(context);
            } catch (Throwable throwable) {
                cancel = true;
                ErrorHelper.show(throwable);
            }
        } else {
            notifyCompleted();
        }
    }

    /**
     * Invoked when a task generates an event.
     *
     * @param event the event
     */
    protected void onEvent(TaskEvent event) {
        switch (event.getType()) {
            case CANCELLED:
                cancel = true;
                if (event.getTask().isRequired()) {
                    notifyCancelled();
                } else {
                    notifyCompleted();
                }
                break;
            case COMPLETED:
                next();
                break;
        }
    }

}
