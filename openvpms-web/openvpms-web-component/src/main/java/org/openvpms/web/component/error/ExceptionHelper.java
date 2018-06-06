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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.error;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * Exception helper methods.
 *
 * @author Tim Anderson
 */
public class ExceptionHelper {

    /**
     * Returns the root cause of an exception.
     *
     * @param exception the exception
     * @return the root cause of the exception, or {@code exception} if it is the root
     */
    public static Throwable getRootCause(Throwable exception) {
        if (exception.getCause() != null) {
            return getRootCause(exception.getCause());
        }
        return exception;
    }

    /**
     * Determines if an exception indicates that an object being operated on (or a related object) was modified
     * externally.
     *
     * @param exception the exception
     * @return {@code true} if the object was modified externally
     */
    public static boolean isModifiedExternally(Throwable exception) {
        return exception instanceof StaleObjectStateException || exception instanceof ObjectNotFoundException
                || exception instanceof UnexpectedRollbackException;
    }



}
