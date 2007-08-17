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

package org.openvpms.web.component.processor;

import echopointng.ProgressBar;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TaskQueueHandle;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.component.processor.AbstractAsynchronousBatchProcessor;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.util.Vetoable;

import java.util.List;


/**
 * A {BatchProcessor} that displays the current progress in a progress bar.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ProgressBarProcessor<T>
        extends AbstractAsynchronousBatchProcessor<T>
        implements BatchProcessorComponent {

    /**
     * The items to process.
     */
    private Iterable<T> items;

    /**
     * Determines how often the progress bar is updated.
     */
    private int step;

    /**
     * The processor title
     */
    private final String title;

    /**
     * The progress bar.
     */
    private ProgressBar bar;

    /**
     * The task queue, in order to asynchronously trigger processing.
     */
    private TaskQueueHandle taskQueue;

    /**
     * The last time a refresh occurred.
     */
    private long lastRefresh = 0;

    /**
     * Determines how often to re-schedule the processor, to force a refresh.
     */
    private long refreshInterval = DateUtils.MILLIS_IN_SECOND * 2;

    /**
     * The retry listener.
     */
    private RetryListener<T> retryListener;


    /**
     * Constructs a new <tt>ProgressBarProcessor</tt>.
     *
     * @param items the items to process
     * @param title the processor title. May be <tt>null</tt>
     */
    public ProgressBarProcessor(List<T> items, String title) {
        this(items, items.size(), title);
    }

    /**
     * Constructs a new <tt>ProgressBarProcessor</tt>.
     *
     * @param items the items
     * @param size  the expected no. of items. This need not be exact
     * @param title the progress bar title
     */
    public ProgressBarProcessor(Iterable<T> items, int size, String title) {
        this(title);
        setItems(items, size);
    }

    /**
     * Constructs a new <tt>ProgressBarProcessor</tt>.
     * The {@link #setItems} method must be invoked prior to starting
     * processing.
     *
     * @param title the progress bar title. May be <tt>null</tt>
     */
    public ProgressBarProcessor(String title) {
        bar = new ProgressBar();
        bar.setCompletedColor(Color.GREEN);
        this.title = title;
    }

    /**
     * Returns the processor title.
     *
     * @return the processor title. May be <tt>null</tt>
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return bar;
    }

    /**
     * Restarts processing.
     */
    public void restart() {
        setIterator(items.iterator());
        lastRefresh = 0;
    }

    /**
     * Returns the expected no. of items to process.
     *
     * @return the expected no. of items to process
     */
    public int getCount() {
        return bar.getMaximum();
    }

    /**
     * Sets a listener to handle retries.
     *
     * @param listener the retry listener
     */
    public void setRetryListener(RetryListener<T> listener) {
        retryListener = listener;
    }

    /**
     * Sets the items to iterate.
     *
     * @param items the items.
     * @param size  the expected no. of items. This need not be exact
     */
    protected void setItems(Iterable<T> items, int size) {
        this.items = items;
        setIterator(items.iterator());
        bar.setMaximum(size);
        step = size / 10;
        if (step == 0) {
            step = 1;
        }
    }

    /**
     * Invoked when batch processing has completed.
     */
    @Override
    protected void processingCompleted() {
        removeTaskQueue();
        bar.setValue(getProcessed());
        super.processingCompleted();
    }

    /**
     * Invoked when batch processing has terminated due to error.
     */
    @Override
    protected void processingError(Throwable exception) {
        removeTaskQueue();
        super.processingError(exception);
    }

    /**
     * To be invoked when processing of an object is complete.
     * This periodically updates the progress bar.
     *
     * @param object the processed object
     */
    protected void processCompleted(T object) {
        incProcessed();
        long time = System.currentTimeMillis();
        int processed = getProcessed();
        if (processed > bar.getMaximum()) {
            // processed more than expected, so update the maximum
            bar.setMaximum(processed);
        }
        if (processed % step == 0) {
            bar.setValue(processed);
        }
        if (!isSuspended() && (lastRefresh == 0
                || ((time - lastRefresh) > refreshInterval))) {
            // enable a refresh of the progress bar
            setSuspend(true);
            final ApplicationInstance app = ApplicationInstance.getActive();
            app.enqueueTask(getTaskQueue(), new Runnable() {
                public void run() {
                    process();
                }
            });
            lastRefresh = time;
        }
    }

    /**
     * To be invoked when processing of an object fails.
     * Suspends processing.
     * If a {@link RetryListener} is registered, the listener is notified to
     * handle retries, otherwise {@link #notifyError} will be invoked.
     *
     * @param object  the object that failed
     * @param message formatted message indicating the reason for the failure
     * @param cause   the cause of the failure
     */
    protected void processFailed(final T object, String message,
                                 Throwable cause) {
        setSuspend(true);
        if (retryListener != null) {
            Vetoable veto = new Vetoable() {
                public void veto(boolean veto) {
                    if (!veto) {
                        retry(object);
                    }
                }
            };

            retryListener.retry(object, veto, message);
        } else {
            notifyError(cause);
        }
    }

    /**
     * Retries an object.
     *
     * @param object the object to retry
     */
    protected void retry(T object) {
        try {
            setSuspend(false);
            process(object);
            if (!isSuspended()) {
                process();
            }
        } catch (OpenVPMSException exception) {
            processFailed(object, exception.getMessage(), exception);
        }
    }

    /**
     * Returns the progress bar.
     *
     * @return the progress bar
     */
    protected ProgressBar getProgressBar() {
        return bar;
    }

    /**
     * Returns the task queue, creating it if it doesn't exist.
     *
     * @return the task queue
     */
    private TaskQueueHandle getTaskQueue() {
        if (taskQueue == null) {
            ApplicationInstance app = ApplicationInstance.getActive();
            taskQueue = app.createTaskQueue();
        }
        return taskQueue;
    }

    /**
     * Cleans up the task queue.
     */
    private void removeTaskQueue() {
        if (taskQueue != null) {
            final ApplicationInstance app = ApplicationInstance.getActive();
            app.removeTaskQueue(taskQueue);
            taskQueue = null;
        }
    }

}

