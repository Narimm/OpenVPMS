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
package org.openvpms.web.component.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
    private Runnable action;

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
    public Retryer(Runnable action) {
        this(action, null);
    }

    /**
     * Constructs a <tt>Retryer</tt>.
     *
     * @param action     the action
     * @param thenAction the action to invoke when the action successfully completes. May be <tt>null</tt>
     */
    public Retryer(Runnable action, Runnable thenAction) {
        this(action, thenAction, null);
    }

    /**
     * Constructs a <tt>Retryer</tt>.
     *
     * @param action     the action
     * @param thenAction the action to invoke when the action successfully completes. May be <tt>null</tt>
     * @param elseAction the action to invoke when the action fails to complete. May be <tt>null</tt>
     */
    public Retryer(Runnable action, Runnable thenAction, Runnable elseAction) {
        this.action = action;
        this.thenAction = thenAction;
        this.elseAction = elseAction;
    }

    /**
     * Starts the action.
     */
    public void start() {
        runs = 0;
        run();
    }

    /**
     * Returns the no. of attempts to run the action.
     *
     * @return the no. of attempts
     */
    public int getRuns() {
        return runs;
    }

    /**
     * Runs the action.
     * <p/>
     * If the action completes successfully, the {@link #runThenAction} will be invoked.
     * If the action fails, then {@link #runFailed} will be invoked.
     * or {@link #runElseAction()}
     */
    protected void run() {
        boolean success = false;
        ++runs;
        try {
            action.run();
            success = true;
        } catch (Throwable exception) {
            runFailed(exception);
        }
        if (success) {
            runThenAction();
        }
    }

    /**
     * Runs the "thenAction", if one is present.
     */
    protected void runThenAction() {
        run(thenAction);
    }

    /**
     * Rurns the "elseAction", if one is present.
     */
    protected void runElseAction() {
        run(elseAction);
    }

    /**
     * Invoked when the action fails.
     *
     * @param exception the cause of the failure
     */
    protected void runFailed(Throwable exception) {
        if (runs >= attempts) {
            abort(exception);
        } else {
            retry(exception);
        }
    }

    /**
     * Invoked to run the action again, after failure.
     *
     * @param exception the cause of the failure
     */
    protected void retry(Throwable exception) {
        log.warn("Retrying " + action + " after " + runs + " attempts", exception);
        delay();
        run();
    }

    /**
     * Invoked to abort the action, after failure.
     *
     * @param exception the cause of the failure
     */
    protected void abort(Throwable exception) {
        log.warn("Aborting " + action + " after " + runs + " attempts", exception);
        ErrorHelper.show(exception);
        runElseAction();
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
     * @param runnable the runnable. May be <tt>null</tt>
     */
    private void run(Runnable runnable) {
        if (runnable != null) {
            try {
                runnable.run();
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        }
    }
}
