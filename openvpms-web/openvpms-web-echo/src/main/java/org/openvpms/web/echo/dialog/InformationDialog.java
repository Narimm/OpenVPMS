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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.dialog;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.webcontainer.ContainerContext;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Modal information dialog box.
 *
 * @author Tim Anderson
 */
public class InformationDialog extends MessageDialog {

    /**
     * The task queue handle, used to automatically close the dialog.
     */
    private TaskQueueHandle taskQueue;

    /**
     * The time when the dialog was displayed. Used to avoid prematurely closing the dialog when it is set
     * to be displayed for a period of time.
     */
    private long time;


    /**
     * Constructs a {@link InformationDialog}.
     *
     * @param message the message to display
     */
    public InformationDialog(String message) {
        this(Messages.get("informationdialog.title"), message);
    }

    /**
     * Constructs a {@link InformationDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    public InformationDialog(String title, String message) {
        super(title, message, "InformationDialog", OK);
        setDefaultButton(OK_ID);
    }

    /**
     * Helper to show a new information dialog.
     *
     * @param message dialog message
     */
    public static void show(String message) {
        InformationDialog dialog = new InformationDialog(message);
        dialog.show();
    }

    /**
     * Helper to show a new information dialog.
     *
     * @param title   the dialog title
     * @param message dialog message
     */
    public static void show(String title, String message) {
        InformationDialog dialog = new InformationDialog(title, message);
        dialog.show();
    }

    /**
     * Shows the message for a limited time.
     *
     * @param time the time to show the window for in milliseconds
     */
    public void show(final int time) {
        super.show();
        this.time = System.currentTimeMillis();
        ApplicationInstance app = getApplicationInstance();
        if (taskQueue == null) {
            taskQueue = app.createTaskQueue();
        }
        queueClose(time);
        setTaskQueueInterval(time);
    }

    /**
     * Processes a user request to close the window (via the close button).
     * <p/>
     * This restores the previous focus
     */
    @Override
    public void userClose() {
        if (taskQueue != null) {
            // set the queue interval to something large, otherwise the old interval may be used until the handle
            // is garbage collected
            setTaskQueueInterval((int) DateUtils.MILLIS_PER_HOUR);
            ApplicationInstance app = getApplicationInstance();
            app.removeTaskQueue(taskQueue);
            taskQueue = null;
        }
        super.userClose();
    }

    /**
     * Closes the dialog, if it has been displayed at least {@code time} milliseconds.
     * If not, queues a task to close it.
     *
     * @param time the time
     */
    private void close(int time) {
        long now = System.currentTimeMillis();
        if (now - time >= this.time) {
            userClose();
        } else {
            queueClose(time);
        }
    }

    /**
     * Queues the dialog to close in {@code time} milliseconds.
     *
     * @param time the time to close the dialog
     */
    private void queueClose(final int time) {
        ApplicationInstance app = getApplicationInstance();
        if (app != null && taskQueue != null) {
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    close(time);
                }
            };
            app.enqueueTask(taskQueue, task);
        }
    }

    /**
     * Sets the interval for the task queue.
     * Note that if there are other task queues, enqueued tasks may be invoked earlier.
     *
     * @param time the time, in milliseconds
     */
    private void setTaskQueueInterval(int time) {
        ApplicationInstance app = getApplicationInstance();
        if (app != null) {
            ContainerContext context = (ContainerContext) app.getContextProperty(
                    ContainerContext.CONTEXT_PROPERTY_NAME);
            if (context != null) {
                context.setTaskQueueCallbackInterval(taskQueue, time);
            }
        }
    }

}
