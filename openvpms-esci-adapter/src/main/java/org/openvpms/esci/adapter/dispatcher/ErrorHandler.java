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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.esci.adapter.dispatcher;

/**
 * Handler for {@link ESCIDispatcher} errors.
 *
 * @author Tim Anderson
 */
public interface ErrorHandler {

    /**
     * Determines if the dispatcher should terminate on error.
     *
     * @return {@code true} if the dispatcher should terminate on error, {@code false} if it should continue
     */
    boolean terminateOnError();

    /**
     * Invoked when an error occurs.
     *
     * @param exception the error
     */
    void error(Throwable exception);
}
