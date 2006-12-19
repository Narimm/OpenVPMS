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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import com.sun.star.awt.XPrinterServer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XPrintable;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToPrint;


/**
 * OpenOffice print service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PrintService {

    /**
     * The OpenOffice service.
     */
    private final OpenOfficeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Creates a new <code>PrintService</code>.
     *
     * @param service  the OpenOffice service
     * @param handlers the document handlers
     */
    public PrintService(OpenOfficeService service, DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Returns a list of printers.
     *
     * @return a list of printers
     * @throws OpenOfficeException for any error
     */
    public String[] getPrinters() {
        XPrinterServer printerServer = service.getPrinterServer();
        return printerServer.getPrinterNames();
    }

    /**
     * Prints a document.
     *
     * @param document the document to print
     * @param printer  the printer name.
     * @throws OpenOfficeException for any error
     */
    public void print(Document document, String printer) {
        OpenOfficeDocument doc = new OpenOfficeDocument(document, service,
                                                        handlers);
        print(doc, printer, true);
    }

    /**
     * Prints a document.
     *
     * @param document the document to print
     * @param printer  the printer name
     * @param close    if <code>true</code>, close the document when printing
     *                 completes
     * @throws OpenOfficeException for any error
     */
    public void print(final OpenOfficeDocument document, String printer,
                      boolean close) {
        XPrintable printable = (XPrintable) UnoRuntime.queryInterface(
                XPrintable.class, document.getComponent());

        PropertyValue[] printerDesc = {newProperty("Name", printer)};
        PropertyValue[] printOpts = {newProperty("Pages", "1"),
                                     newProperty("Wait", true)};

/*
        todo - replaced asynchronous notification of print completion with
        synchonrous printing due to OpenOffice 2.1 crashes.
        if (close) {
            XPrintJobBroadcaster broadcaster = (XPrintJobBroadcaster)
                    UnoRuntime.queryInterface(XPrintJobBroadcaster.class,
                                              printable);
            broadcaster.addPrintJobListener(new XPrintJobListener() {
                public void printJobEvent(PrintJobEvent event) {
                    PrintableState state = event.State;
                    if (!state.equals(PrintableState.JOB_STARTED)
                            && !state.equals(PrintableState.JOB_SPOOLED)) {
                        document.close();
                    }
                }

                public void disposing(EventObject eventObject) {
                }
            });
        }
*/

        try {
            printable.setPrinter(printerDesc);
            printable.print(printOpts);
        } catch (IllegalArgumentException exception) {
            throw new OpenOfficeException(FailedToPrint, exception.getMessage(),
                                          exception);
        }
        if (close) {
            document.close();
        }
    }

    /**
     * Helper to create a new <code>PropertyValue</code>.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new <code>PropertyValue</code>
     */
    private static PropertyValue newProperty(String name, Object value) {
        PropertyValue property = new PropertyValue();
        property.Name = name;
        property.Value = value;
        return property;
    }

}
