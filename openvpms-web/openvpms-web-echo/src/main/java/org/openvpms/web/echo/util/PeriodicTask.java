package org.openvpms.web.echo.util;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.webcontainer.ContainerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Periodically executes a {@link Runnable} using the Echo task queue.
 *
 * @author Tim Anderson
 */
public class PeriodicTask {

    /**
     * The task to run.
     */
    private final Runnable task;

    /**
     * The task interval.
     */
    private final int interval;

    /**
     * The Echo application.
     */
    private final ApplicationInstance app;

    /**
     * The task queue handle.
     */
    private TaskQueueHandle queue;

    /**
     * Determines if the task is stopped.
     */
    private boolean stop = true;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PeriodicTask.class);

    /**
     * Constructs a {@link PeriodicTask}.
     *
     * @param app      the application instance
     * @param interval the interval
     * @param task     the task to run
     */
    public PeriodicTask(ApplicationInstance app, int interval, Runnable task) {
        this.task = task;
        this.interval = interval;
        this.app = app;
    }

    /**
     * Starts the task.
     * <p>
     * If the task is already running, this is ignored.
     */
    public void start() {
        if (stop) {
            queue = app.createTaskQueue();
            stop = false;
            ContainerContext context = (ContainerContext) app.getContextProperty(
                    ContainerContext.CONTEXT_PROPERTY_NAME);
            if (context != null) {
                context.setTaskQueueCallbackInterval(queue, interval * 1000);
            }
            queue();
        }
    }

    /**
     * Restarts the task.
     */
    public void restart() {
        stop();
        start();
    }

    /**
     * Stops the task.
     */
    public void stop() {
        stop = true;
        if (queue != null) {
            app.removeTaskQueue(queue);
            queue = null;
        }
    }

    /**
     * Queues the task.
     */
    private void queue() {
        app.enqueueTask(queue, this::execute);
    }

    /**
     * Executes the task.
     */
    private void execute() {
        if (!stop) {
            try {
                task.run();
            } catch (Throwable exception) {
                log.warn("Task execution failed", exception);
            }
            if (!stop) {
                queue();
            }
        }
    }
}
