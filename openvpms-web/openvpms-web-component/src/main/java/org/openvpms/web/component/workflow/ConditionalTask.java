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
 * Task that executes a task if a condition evaluates true.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ConditionalTask extends AbstractTask {

    /**
     * The condition.
     */
    private final EvalTask<Boolean> condition;

    /**
     * The task to execute if the condition evaluates true.
     */
    private final Task task;

    /**
     * The task to execute if the condition evaluates false.
     */
    private final Task elseTask;

    /**
     * The task context.
     */
    private TaskContext context;


    /**
     * Constructs a new <code>ConditionalTask</code>.
     *
     * @param condition the condition
     * @param task      the task to execute if the condition evaluates true
     */
    public ConditionalTask(EvalTask<Boolean> condition, Task task) {
        this(condition, task, null);
    }

    /**
     * Constructs a new <code>ConditionalTask</code>.
     *
     * @param condition the condition
     * @param task      the task to execute if the condition evaluates true
     * @param elseTask  the task to execute if the condition evalates false.
     *                  May be <code>null</code>
     */
    public ConditionalTask(EvalTask<Boolean> condition, Task task,
                           Task elseTask) {
        this.condition = condition;
        this.task = task;
        this.elseTask = elseTask;
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
        this.context = context;

        condition.addTaskListener(new DefaultTaskListener() {
            @Override
            public void taskEvent(TaskEvent event) {
                onConditionEvent(event);
            }
        });
        task.addTaskListener(new DefaultTaskListener() {
            @Override
            public void taskEvent(TaskEvent event) {
                onTaskEvent(event);
            }
        });
        if (elseTask != null) {
            elseTask.addTaskListener(new DefaultTaskListener() {
                @Override
                public void taskEvent(TaskEvent event) {
                    onTaskEvent(event);
                }
            });
        }
        start(condition, context);
    }

    /**
     * Invoked when the condition task completes or is cancelled.
     *
     * @param event the event
     */
    private void onConditionEvent(TaskEvent event) {
        switch (event.getType()) {
            case SKIPPED:
                notifySkipped();
                break;
            case CANCELLED:
                notifyCancelled();
                break;
            case COMPLETED:
                if (condition.getValue()) {
                    start(task, context);
                } else if (elseTask != null) {
                    start(elseTask, context);
                } else {
                    notifyCompleted();
                }
                break;
        }
    }

    /**
     * Invoked when a task completes or is cancelled.
     *
     * @param event the event
     */
    private void onTaskEvent(TaskEvent event) {
        switch (event.getType()) {
            case SKIPPED:
                notifySkipped();
                break;
            case CANCELLED:
                notifyCancelled();
                break;
            case COMPLETED:
                notifyCompleted();
                break;
        }
    }
}
