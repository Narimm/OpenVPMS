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

import org.junit.Test;
import org.openvpms.web.component.retry.Retryable;
import org.openvpms.web.component.retry.Retryer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link org.openvpms.web.component.retry.Retryer}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RetryerTestCase {

    /**
     * Verifies an action is invoked the expected no. of times.
     */
    @Test
    public void testRetryer() {
        Action action = new Action(9);
        Retryer retryer = new Retryer(action);
        retryer.setDelay(0); // no delay
        assertTrue(retryer.start());
        assertEquals(10, action.getCount());
    }

    /**
     * Verifies that an action set to always fail triggers the 'elseAction'.
     */
    @Test
    public void testSucceedTriggersThenAction() {
        FlagAction thenAction = new FlagAction();
        FlagAction elseAction = new FlagAction();
        Retryer retryer = new Retryer(new Action(false), thenAction, elseAction);
        retryer.setDelay(0); // no delay
        assertTrue(retryer.start());
        assertTrue(thenAction.hasRun());
        assertFalse(elseAction.hasRun());
    }

    /**
     * Verifies that an action set to always fail triggers the 'elseAction'.
     */
    @Test
    public void testFailTriggersElseAction() {
        FlagAction thenAction = new FlagAction();
        FlagAction elseAction = new FlagAction();
        Retryer retryer = new Retryer(new Action(true), thenAction, elseAction);
        retryer.setDelay(0); // no delay
        assertFalse(retryer.start());
        assertFalse(thenAction.hasRun());
        assertTrue(elseAction.hasRun());
    }

    /**
     * Verifies that an action set to succeed after nine attempts triggers the 'thenAction'.
     */
    @Test
    public void testEventualSucceedTriggersThenAction() {
        FlagAction thenAction = new FlagAction();
        FlagAction elseAction = new FlagAction();
        Retryer retryer = new Retryer(new Action(9), thenAction, elseAction);
        retryer.setDelay(0); // no delay
        assertTrue(retryer.start());
        assertTrue(thenAction.hasRun());
        assertFalse(elseAction.hasRun());
    }


    private static class Action implements Retryable {

        /**
         * Determines if the action should always fail.
         */
        private boolean fail;

        /**
         * The no. of attempts, before succeeding.
         */
        private int attempts;

        /**
         * The current count of attempts.
         */
        int count = 0;


        /**
         * Constructs an action that either always fails, or always succeeds.
         *
         * @param fail if <tt>true</tt>, the action fails
         */
        public Action(boolean fail) {
            this.fail = fail;
        }

        /**
         * Constructs an action that succeeds after <tt>attempt</tt> attempts.
         *
         * @param attempts the attempts
         */
        public Action(int attempts) {
            fail = false;
            this.attempts = attempts;
        }

        /**
         * Runs the action.
         */
        public boolean run() {
            if (fail || ++count <= attempts) {
                throw new RuntimeException("RetryerTestCase - action set to fail");
            }
            return true;
        }

        /**
         * Returns the no. of times the action has run.
         *
         * @return the count of runs
         */
        public int getCount() {
            return count;
        }
    }

    /**
     * Action that sets a flag when run.
     */
    private static class FlagAction implements Runnable {

        /**
         * Determines if the aciton has been run.
         */
        private boolean run;

        /**
         * Determines if the action has been run.
         *
         * @return <tt>true</tt> if the action has been run, otherwise <tt>false</tt>
         */
        public boolean hasRun() {
            return run;
        }

        public void run() {
            run = true;
        }
    }
}
