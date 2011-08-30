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

import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Workflow task.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface Task {

    /**
     * Registers a listener to be notified of task events.
     *
     * @param listener the listener to add
     */
    void addTaskListener(TaskListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeTaskListener(TaskListener listener);

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    void start(TaskContext context);

    /**
     * Determines if this is a required or an optional task.
     *
     * @return <code>true</code> if this is a required task; <code>false</code>
     *         if it is an optional task
     */
    boolean isRequired();
}
