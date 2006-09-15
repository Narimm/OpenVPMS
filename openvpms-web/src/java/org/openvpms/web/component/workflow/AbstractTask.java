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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Collection;


/**
 * Abstract implementation of the {@link Task} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractTask implements Task {

    /**
     * The task listener to notify on completion or failure of the task. May
     * be <code>null</code>.
     */
    private TaskListener listener;

    /**
     * Determines if this task is required.
     */
    private boolean required = true;


    /**
     * Registers a listener to be notified of task events.
     *
     * @param listener the listener
     */
    public void setTaskListener(TaskListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the task listener.
     *
     * @return the task listener. May be <code>null</code>
     */
    public TaskListener getTaskListener() {
        return listener;
    }

    /**
     * Determines if this is a required or an optional task.
     *
     * @param required if <code>true</code> this is a required task; otherwise
     *                 it is an optional task
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Determines if this is a required or an optional task.
     *
     * @return <code>true</code> if this is a required task; <code>false</code>
     *         if it is an optional task
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Notifies any registered listener that the task has completed.
     */
    protected void notifyCompleted() {
        if (listener != null) {
            listener.taskEvent(new TaskEvent(TaskEvent.Type.COMPLETED, this));
        }
    }

    /**
     * Notifies any registered listener that the task has been cancelled.
     */
    protected void notifyCancelled() {
        if (listener != null) {
            listener.taskEvent(new TaskEvent(TaskEvent.Type.CANCELLED, this));
        }
    }

    /**
     * Helper to concatenate a list of display names together.
     *
     * @param shortNames the short names
     * @return the display names for the short names
     */
    protected static String getType(String[] shortNames) {
        StringBuffer type = new StringBuffer();
        for (int i = 0; i < shortNames.length && i < 2; ++i) {
            if (i != 0) {
                type.append("/");
            }
            type.append(DescriptorHelper.getDisplayName(shortNames[i]));
        }
        if (shortNames.length > 2) {
            type.append("/...");
        }
        return type.toString();
    }

    /**
     * Populates an object.
     *
     * @param object     the object to populate
     * @param properties the properties
     * @param context    the task context
     */
    protected void populate(IMObject object, TaskProperties properties,
                            TaskContext context) {
        Collection<TaskProperty> list = properties.getProperties();
        if (!list.isEmpty()) {
            IMObjectBean bean = new IMObjectBean(object);
            for (TaskProperty property : list) {
                bean.setValue(property.getName(), property.getValue(context));
            }
        }
    }

}
