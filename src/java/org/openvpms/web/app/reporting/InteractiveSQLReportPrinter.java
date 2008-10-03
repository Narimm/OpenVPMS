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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting;

import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.resource.util.Messages;


/**
 * Interactive printer for {@link SQLReportPrinter}. Pops up a dialog with
 * options to print, preview, or cancel, and supply the report with parameters.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InteractiveSQLReportPrinter extends InteractivePrinter {

    /**
     * Constructs a new <tt>InteractiveSQLReportPrinter</tt>.
     *
     * @param printer the printer to delegate to
     */
    public InteractiveSQLReportPrinter(SQLReportPrinter printer) {
        super(printer);
    }

    /**
     * Creates a new print dialog.
     *
     * @return a new print dialog
     */
    @Override
    protected PrintDialog createDialog() {
        final SQLReportPrinter printer = getPrinter();
        return new SQLReportDialog(getTitle(), printer.getParameterTypes()) {

            @Override
            protected void doPrint() {
                printer.setParameters(getValues());
            }

            @Override
            protected void doPreview() {
                printer.setParameters(getValues());
                doPrintPreview();
            }
        };
    }

    /**
     * Returns the underlying printer.
     *
     * @return the printer
     */
    protected SQLReportPrinter getPrinter() {
        return (SQLReportPrinter) super.getPrinter();
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    @Override
    protected String getTitle() {
        return Messages.get("reporting.run.title", getPrinter().getName());
    }

}
