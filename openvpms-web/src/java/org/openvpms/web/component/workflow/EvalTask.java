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
 * A task that evaluates to produce a value.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class EvalTask<T> extends AbstractTask {

    /**
     * The evaluated value.
     */
    private T value;

    /**
     * Returns the value of this task.
     *
     * @return the value of this task
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value of the task, and notifies the task listener of
     * completion.
     *
     * @param value the value
     */
    protected void setValue(T value) {
        this.value = value;
        notifyCompleted();
    }

}
