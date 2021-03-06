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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.print;

import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.print.Printer;


/**
 * Prints an object.
 *
 * @author Tim Anderson
 */
public interface IMPrinter<T> extends Printer {

    /**
     * Returns the objects being printed.
     *
     * @return the objects being printed
     */
    Iterable<T> getObjects();

    /**
     * Returns the reporter.
     *
     * @return the reporter
     */
    Reporter<T> getReporter();
}
