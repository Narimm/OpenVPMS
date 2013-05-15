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

package org.openvpms.web.component.workflow;


import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.LocalContext;

/**
 * Runs a task in a local context, to prevent it from updating the parent.
 * <p/>
 * Context objects can be propagated to the parent on completion.
 *
 * @author Tim Anderson
 */
public class LocalTask extends AbstractTask {

    /**
     * The task to run.
     */
    private final Task task;

    /**
     * The context object keys to propagate on completion.
     */
    private final String[] keys;


    /**
     * Constructs an {@link LocalTask}.
     *
     * @param task the task to run
     * @param keys the keys of the context objects to propagate to the parent context on completion
     */
    public LocalTask(Task task, String... keys) {
        this.task = task;
        this.keys = keys;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    public void start(final TaskContext context) {
        final LocalContext local = new LocalContext();
        TaskContext subContext = new DefaultTaskContext(local, context, context.getHelpContext());
        task.addTaskListener(new DefaultTaskListener() {
            public void taskEvent(TaskEvent event) {
                switch (event.getType()) {
                    case SKIPPED:
                        notifySkipped();
                        break;
                    case CANCELLED:
                        notifyCancelled();
                        break;
                    case COMPLETED:
                        onCompleted(local, context);
                        break;
                }
            }
        });
        task.start(subContext);
    }

    /**
     * Returns the task.
     *
     * @return the task
     */
    public Task getTask() {
        return task;
    }

    /**
     * Invoked when the task completes.
     * <p/>
     * Propagates requested objects from the local to the parent context, and notifys any listener of task completion
     *
     * @param local  the local context
     * @param parent the parent context
     */
    private void onCompleted(LocalContext local, TaskContext parent) {
        for (String key : keys) {
            IMObject object = local.getObject(key);
            if (object != null) {
                parent.setObject(key, object);
            }
        }
        notifyCompleted();
    }

}
