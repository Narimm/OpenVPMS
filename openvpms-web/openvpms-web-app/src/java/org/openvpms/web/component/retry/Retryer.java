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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.retry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Helper to retry an action if the action fails.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Retryer {

    /**
     * The default no. of attempts, before failing.
     */
    public static final int DEFAULT_ATTEMPTS = 10;

    /**
     * The default delay between retries, in milliseconds.
     */
    public static final long DEFAULT_DELAY = 1000;

    /**
     * The delay between retries, in milliseconds.
     */
    private long delay = DEFAULT_DELAY;

    /**
     * The action to run.
     */
    private Retryable action;

    /**
     * The action to invoke when the action successfully completes.
     */
    private Runnable thenAction;

    /**
     * The action to invoke when the action fails to complete.
     */
    private Runnable elseAction;

    /**
     * The no. of runs.
     */
    private int runs;

    /**
     * The no. of attempts to make, before giving up.
     */
    private int attempts = DEFAULT_ATTEMPTS;

    /**
     * The logger.
     */
    private Log log = LogFactory.getLog(Retryer.class);

    /**
     * Constructs a <tt>Retryer</tt>.
     *
     * @param action the action to run
     */
    public Retryer(Retryable action) {
        this(action, null);
    }

    /**
     * Constructs a <tt>Retryer</tt>.
     *
     * @param action     the action
     * @param thenAction the action to invoke when the action successfully completes. May be {@code null}
     */
    public Retryer(Retryable action, Runnable thenAction) {
        this(action, thenAction, null);
    }

    /**
     * Constructs a <tt>Retryer</tt>.
     *
     * @param action     the action
     * @param thenAction the action to invoke when the action successfully completes. May be {@code null}
     * @param elseAction the action to invoke when the action fails to complete. May be {@code null}
     */
    public Retryer(Retryable action, Runnable thenAction, Runnable elseAction) {
        this.action = action;
        this.thenAction = thenAction;
        this.elseAction = elseAction;
    }

    /**
     * Runs an action, retrying it if it fails.
     *
     * @param action the action to run
     * @return {@code true} if the action ran successfully
     */
    public static boolean run(Retryable action) {
        Retryer retryer = new Retryer(action);
        return retryer.start();
    }

    /**
     * Starts the action.
     *
     * @return {@code true} if the action (and any then-action) completes successfully
     */
    public boolean start() {
        runs = 0;
        return run();
    }

    /**
     * Sets the delay between retries.
     *
     * @param delay the delay between retries, in milliseconds
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * Runs the action.
     * <p/>
     * If the action completes successfully, the {@link #runThenAction} will be invoked.
     * If the action fails after the maximum no. of attempts, then {@link #runElseAction()} will be invoked.
     *
     * @return {@code true} if all actions complete successfully
     */
    protected boolean run() {
        boolean result = false;
        boolean success = false;
        for (int i = 0; i < attempts; ++i) {
            ++runs;
            try {
                if (action.run()) {
                    success = true;
                } else {
                    logAbort();
                }
                break;
            } catch (Throwable exception) {
                if (runs >= attempts) {
                    logAbort(exception);
                    break;
                } else {
                    logRetry(exception);
                    delay();
                }
            }
        }
        if (success) {
            result = runThenAction();
        } else {
            runElseAction();
        }
        return result;
    }

    /**
     * Runs the "thenAction", if one is present.
     *
     * @return {@code true} if the action completes successfully
     */
    protected boolean runThenAction() {
        return run(thenAction);
    }

    /**
     * Runs the "elseAction", if one is present.
     *
     * @return {@code true} if the action completes successfully
     */
    protected boolean runElseAction() {
        return run(elseAction);
    }

    /**
     * Logs than an action is being retried.
     *
     * @param exception the cause of the retry
     */
    protected void logRetry(Throwable exception) {
        log.warn("Retrying " + action + " after " + runs + " attempts", exception);
    }

    /**
     * Logs that an action is being aborted.
     */
    protected void logAbort() {
        log.warn("Aborting " + action + " after " + runs + " attempts");
    }

    /**
     * Logs that an action is being aborted.
     *
     * @param exception the cause of the failure
     */
    protected void logAbort(Throwable exception) {
        log.warn("Aborting " + action + " after " + runs + " attempts", exception);
        ErrorHelper.show(exception);
    }

    /**
     * Pauses the thread.
     */
    protected void delay() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignore) {
            // no-op
        }
    }

    /**
     * Runs an action, displaying any errors.
     *
     * @param action the action. May be {@code null}
     * @return {@code} true if action is {@code null} or completes successfully
     */
    private boolean run(Runnable action) {
        boolean result = true;
        if (action != null) {
            try {
                action.run();
            } catch (Throwable exception) {
                result = false;
                ErrorHelper.show(exception);
            }
        }
        return result;
    }
}
