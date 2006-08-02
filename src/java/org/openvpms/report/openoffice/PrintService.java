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
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.PrintableState;
import com.sun.star.view.PrintableStateEvent;
import com.sun.star.view.XPrintable;
import com.sun.star.view.XPrintableListener;
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
    private final OpenOfficeService _service;


    /**
     * Creates a new <code>PrintService</code>.
     *
     * @param service the OpenOffice service
     */
    public PrintService(OpenOfficeService service) {
        _service = service;
    }

    /**
     * Returns a list of printers.
     *
     * @return a list of printers
     * @throws OpenOfficeException for any error
     */
    public String[] getPrinters() {
        XPrinterServer printerServer = _service.getPrinterServer();
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
        final XComponent doc = DocumentHelper.newDocComponentFromTemplate(
                document, _service);
        XPrintable printable = (XPrintable) UnoRuntime.queryInterface(
                XPrintable.class, doc);
        PropertyValue[] printerDesc = {
                DocumentHelper.newProperty("Name", printer)
        };

        PropertyValue[] printOpts = {
                DocumentHelper.newProperty("Pages", "1")
        };

        doc.addEventListener(new XPrintableListener() {
            public void stateChanged(PrintableStateEvent event) {
                if (!event.State.equals(PrintableState.JOB_STARTED)) {
                    DocumentHelper.close(doc);
                }
            }

            public void disposing(EventObject eventObject) {
            }
        });

        try {
            printable.setPrinter(printerDesc);
            printable.print(printOpts);
        } catch (IllegalArgumentException exception) {
            DocumentHelper.close(doc);
            throw new OpenOfficeException(FailedToPrint, exception.getMessage(),
                                          exception);
        }
    }

}
