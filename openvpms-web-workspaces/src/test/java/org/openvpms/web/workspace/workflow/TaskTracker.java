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

package org.openvpms.web.workspace.workflow;

import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper to track the currently executing task in a workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class TaskTracker implements TaskListener {

    /**
     * The current tasks.
     */
    private List<Task> current = new ArrayList<Task>();

    /**
     * Returns the current task.
     *
     * @return the current task, or <tt>null</tt> if there are no tasks.
     */
    public Task getCurrent() {
        return current.size() > 0 ? current.get(current.size() - 1) : null;
    }

    /**
     * Invoked prior to a task starting.
     *
     * @param task the task
     */
    public void starting(Task task) {
        task.addTaskListener(this);
        current.add(task);
    }

    /**
     * Invoked when a task event occurs.
     *
     * @param event the event
     */
    public void taskEvent(TaskEvent event) {
        current.remove(event.getTask());
    }
}
