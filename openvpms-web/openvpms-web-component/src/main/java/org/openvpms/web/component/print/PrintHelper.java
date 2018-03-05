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

package org.openvpms.web.component.print;

import org.apache.commons.lang.StringUtils;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;


/**
 * Print helper.
 *
 * @author Tim Anderson
 */
public class PrintHelper {

    /**
     * Returns the default printer.
     *
     * @return the default printer, or {@code null} if none is defined
     */
    public static String getDefaultPrinter() {
        PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
        return (printer != null) ? printer.getName() : null;
    }

    /**
     * Returns a list of the available printers.
     *
     * @return a list of the available printers
     */
    public static String[] getPrinters() {
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
        String[] names = new String[printers.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = printers[i].getName();
        }
        return names;
    }

    /**
     * Determines if a printer exists.
     *
     * @param name the printer name
     * @return {@code true} if the printer exists, otherwise {@code false}
     */
    public static boolean exists(String name) {
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer : printers) {
            if (StringUtils.equals(printer.getName(), name)) {
                return true;
            }
        }
        return false;
    }

}
