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
 *  Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.report.openoffice;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.DuplexMode;
import com.sun.star.view.XPrintable;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.PrintProperties;

import javax.print.attribute.standard.Sides;

import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToPrint;

/**
 * OpenOffice print service.
 *
 * @author Tim Anderson
 * @author Benjamin Charlton
 */
public class PrintService {

    /**
     * The connection pool.
     */
    private OOConnectionPool pool;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Constructs a {@link PrintService}.
     *
     * @param pool     the connection pool
     * @param handlers the document handlers
     */
    public PrintService(OOConnectionPool pool, DocumentHandlers handlers) {
        this.pool = pool;
        this.handlers = handlers;
    }

    /**
     * Prints a document.
     *
     * @param document   the document to print
     * @param properties the print properties
     * @throws OpenOfficeException for any error
     */
    public void print(Document document, PrintProperties properties) {
        OOConnection connection = null;
        try {
            connection = pool.getConnection();
            OpenOfficeDocument doc = new OpenOfficeDocument(document, connection, handlers);
            print(doc, properties, true);
        } finally {
            OpenOfficeHelper.close(connection);

        }
    }

    /**
     * Prints a document.
     *
     * @param document   the document to print
     * @param properties the print properties
     * @param close      if {@code true}, close the document when printing completes
     * @throws OpenOfficeException for any error
     */
    public void print(OpenOfficeDocument document, PrintProperties properties, boolean close) {
        XPrintable printable = UnoRuntime.queryInterface(XPrintable.class, document.getComponent());
        int copies = properties.getCopies();
        String printer = properties.getPrinterName();
        Sides sides = properties.getSides();
        Short printSides = getDuplexMode(sides);
        PropertyValue[] printerDesc = {newProperty("Name", printer)};
        PropertyValue[] printOpts = {newProperty("Wait", true), newProperty("CopyCount", copies),
                                     newProperty("DuplexMode", printSides)};
        try {
            printable.setPrinter(printerDesc);
            printable.print(printOpts);
        } catch (IllegalArgumentException exception) {
            throw new OpenOfficeException(FailedToPrint, exception.getMessage(), exception);
        }
        if (close) {
            document.close();
        }
    }

    /**
     * Helper to create a new {@code PropertyValue}.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new {@code PropertyValue}
     */
    private PropertyValue newProperty(String name, Object value) {
        PropertyValue property = new PropertyValue();
        property.Name = name;
        property.Value = value;
        return property;
    }

    /**
     * Returns the duplex mode.
     *
     * @param sides the sides to print on. May be {@code null}
     * @return the sides to print on
     */
    private Short getDuplexMode(Sides sides) {
        if (sides != null) {
            if (sides == Sides.ONE_SIDED) {
                return DuplexMode.OFF;
            } else if (sides == Sides.DUPLEX || sides == Sides.TWO_SIDED_LONG_EDGE) {
                return DuplexMode.LONGEDGE;
            } else if (sides == Sides.TUMBLE || sides == Sides.TWO_SIDED_SHORT_EDGE) {
                return DuplexMode.SHORTEDGE;
            }
        }
        return DuplexMode.UNKNOWN;
    }
}
