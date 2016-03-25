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
 */

package org.openvpms.web.component.workflow;

import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Task that executes synchronously.
 *
 * @author Tim Anderson
 */
public abstract class SynchronousTask extends AbstractTask {

    /**
     * Starts the task.
     * <p/>
     * Delegates to {@link #execute} and invokes {@link #notifyCompleted}
     * if no other notify method has been invoked.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    public final void start(TaskContext context) {
        execute(context);
        if (!isFinished()) {
            notifyCompleted();
        }
    }

    /**
     * Executes the task.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    public abstract void execute(TaskContext context);
}
