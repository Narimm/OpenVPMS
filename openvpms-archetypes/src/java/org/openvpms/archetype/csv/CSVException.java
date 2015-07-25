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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.csv;

import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * Base class for CSV exceptions.
 *
 * @author Tim Anderson
 */
public abstract class CSVException extends OpenVPMSException {
    /**
     * The line the error occurred on.
     */
    private final int line;

    public CSVException(Throwable exception, int line) {
        super(exception);
        this.line = line;
    }

    public CSVException(String msg, int line) {
        super(msg);
        this.line = line;
    }

    /**
     * Returns the line the error occurred on.
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }
}
