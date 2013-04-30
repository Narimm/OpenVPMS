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
 * An action that may be retried on failure.
 *
 * @author Tim Anderson
 * @see Retryer
 */
public interface Retryable {

    /**
     * Runs the action.
     *
     * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
     *         retried
     * @throws RuntimeException if the action fails and may be retried
     */
    boolean run();
}