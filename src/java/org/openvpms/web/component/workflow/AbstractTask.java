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

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.event.WindowPaneListener;

import java.util.Collection;


/**
 * Abstract implementation of the {@link Task} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractTask implements Task {

    /**
     * The task listeners to notify on completion or failure of the task.
     */
    private TaskListeners listeners = new TaskListeners();

    /**
     * Determines if this task is required.
     */
    private boolean required = true;

    /**
     * Determines if the task has finished.
     */
    private boolean finished;


    /**
     * Registers a listener to be notified of task events.
     *
     * @param listener the listener
     */
    public void addTaskListener(TaskListener listener) {
        listeners.addListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeTaskListener(TaskListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Returns the task listeners.
     *
     * @return the task listeners
     */
    public TaskListeners getTaskListeners() {
        return listeners;
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
     * Notifies any registered listener that the task has been skipped.
     *
     * @throws IllegalStateException if notification has already occurred
     */
    protected void notifySkipped() {
        notifyEvent(TaskEvent.Type.SKIPPED);
    }

    /**
     * Notifies any registered listener that the task has completed.
     *
     * @throws IllegalStateException if notification has already occurred
     */
    protected void notifyCompleted() {
        notifyEvent(TaskEvent.Type.COMPLETED);
    }

    /**
     * Notifies any registered listener that the task has been cancelled.
     *
     * @throws IllegalStateException if notification has already occurred
     */
    protected void notifyCancelled() {
        notifyEvent(TaskEvent.Type.CANCELLED);
    }

    /**
     * Determines if the task has finished.
     *
     * @return <tt>true</tt> if any of the <tt>notify*</tt> methods have been
     *         invoked
     */
    protected boolean isFinished() {
        return finished;
    }

    /**
     * Helper to display an error and nofity that the task has been cancelled.
     *
     * @param cause the cause of the error
     * @throws IllegalStateException if notification has already occurred
     */
    protected void notifyCancelledOnError(Throwable cause) {
        ErrorHelper.show(cause, new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                notifyCancelled();
            }
        });
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
                String name = property.getName();
                NodeDescriptor descriptor = bean.getDescriptor(name);
                Object value = property.getValue(context);
                // todo - better error handling
                if (descriptor != null && descriptor.isCollection()) {
                    bean.addValue(name, (IMObject) value);
                } else {
                    bean.setValue(name, value);
                }
            }
        }
    }

    /**
     * Notifies the registered listener of the completion state of the task.
     *
     * @param type the event type
     * @throws IllegalStateException if notification has already occurred
     */
    private void notifyEvent(TaskEvent.Type type) {
        if (finished) {
            throw new IllegalStateException(
                    "Listener has already been notified");
        }
        finished = true;
        if (listeners != null) {
            listeners.taskEvent(new TaskEvent(type, this));
        }
    }

}
