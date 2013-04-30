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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.retry;

/**
 * An {@link Retryable} that supports different behaviour on the first and subsequent attempts to perform the action.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRetryable implements Retryable {

    /**
     * Determines if its the first attempt at running the action.
     */
    private boolean first = true;

    /**
     * Runs the action.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     *         retried
     * @throws RuntimeException if the action fails and may be retried
     */
    public boolean run() {
        boolean result;
        if (first) {
            first = false;
            result = runFirst();
        } else {
            result = runSubsequent();
        }
        return result;
    }

    /**
     * Runs the action for the first time.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     *         retried
     * @throws RuntimeException if the action fails and may be retried
     */
    public abstract boolean runFirst();

    /**
     * Runs the action. This is invoked after the first attempt to run the action has failed.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     *         retried
     * @throws RuntimeException if the action fails and may be retried
     */
    public abstract boolean runSubsequent();

}
