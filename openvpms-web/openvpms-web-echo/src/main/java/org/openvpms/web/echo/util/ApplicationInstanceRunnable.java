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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.util;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.TaskQueueHandle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.ref.WeakReference;

/**
 * A {@link Runnable} that runs an underlying instance via an {@link ApplicationInstance}
 * {@link ApplicationInstance#enqueueTask(TaskQueueHandle, Runnable) task queue}.
 * <p/>
 * This allows the {@link Runnable} to update the user interface.
 *
 * @author Tim Anderson
 */
public class ApplicationInstanceRunnable implements Runnable {

    /**
     * The runnable to delegate to.
     */
    private final Runnable runnable;

    /**
     * Reference to the application.
     */
    private final WeakReference<ApplicationInstance> appRef;

    /**
     * Reference to the application task queue. This is a WeakReference as the TaskQueueHandle has a reference to
     * the ApplicationInstance.
     */
    final WeakReference<TaskQueueHandle> taskQueueRef;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ApplicationInstanceRunnable.class);


    /**
     * Constructs an {@link ApplicationInstanceRunnable}.
     *
     * @param runnable the runnable
     */
    public ApplicationInstanceRunnable(Runnable runnable) {
        this.runnable = runnable;
        ApplicationInstance app = ApplicationInstance.getActive();
        if (app == null) {
            throw new IllegalStateException("No current ApplicationInstance");
        }
        TaskQueueHandle taskQueue = app.createTaskQueue();
        appRef = new WeakReference<>(app);
        taskQueueRef = new WeakReference<>(taskQueue);
    }

    /**
     * Schedules the runnable.
     */
    @Override
    public void run() {
        ApplicationInstance app = appRef.get();
        TaskQueueHandle taskQueue = taskQueueRef.get();
        if (app != null && taskQueue != null) {
            app.enqueueTask(taskQueue, new Runnable() {
                public void run() {
                    execute();
                }
            });
        }
    }

    /**
     * Disposes of this instance.
     */
    public void dispose() {
        ApplicationInstance app = appRef.get();
        TaskQueueHandle taskQueue = taskQueueRef.get();
        if (app != null && taskQueue != null) {
            app.removeTaskQueue(taskQueue);
        }
        appRef.clear();
        taskQueueRef.clear();
    }

    /**
     * Executes the runnable.
     */
    private void execute() {
        try {
            runnable.run();
        } catch (Throwable exception) {
            log.warn("ApplicationInstanceRunnable callback threw exception", exception);
        }
    }

}
