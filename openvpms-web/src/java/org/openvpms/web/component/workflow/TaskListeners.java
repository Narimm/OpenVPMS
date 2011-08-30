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
     * Invoked when a task event occurs.
     * Notifies all registered listeners.
     *
     * @param event the event
     */
    public void taskEvent(TaskEvent event) {
        if (listeners != null) {
            TaskListener[] l = listeners.toArray(new TaskListener[0]);
            for (TaskListener listener : l) {
                listener.taskEvent(event);
            }
        }
    }

}
