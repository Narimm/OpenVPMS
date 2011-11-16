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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a list of {@link TaskListener}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-11 04:09:07Z $
 */
public class TaskListeners implements TaskListener {

    /**
     * The listeners.
     */
    private List<TaskListener> listeners;


    /**
     * Add a listener.
     *
     * @param listener the listener to add
     */
    public void addListener(TaskListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<TaskListener>();
        }
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(TaskListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Invoked prior to a task starting.
     * Notifies all registered listeners.
     *
     * @param task the task
     */
    public void starting(Task task) {
        for (TaskListener listener : getListeners()) {
            listener.starting(task);
        }
    }

    /**
     * Invoked when a task event occurs.
     * Notifies all registered listeners.
     *
     * @param event the event
     */
    public void taskEvent(TaskEvent event) {
        for (TaskListener listener : getListeners()) {
            listener.taskEvent(event);
        }
    }

    /**
     * Helper to return the listeners as an array, to avoid concurrent modification exceptions.
     *
     * @return the listeners
     */
    private TaskListener[] getListeners() {
        return (listeners != null) ? listeners.toArray(new TaskListener[listeners.size()]) : new TaskListener[0];
    }

}
