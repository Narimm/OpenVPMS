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
 */

package org.openvpms.web.component.print;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.DocumentTemplatePrinter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.servlet.DownloadServlet;

import javax.print.attribute.standard.MediaTray;


/**
 * Abstract implementation of the {@link Printer} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPrinter implements Printer {

    /**
     * Determines if printing should be interactive.
     */
    private boolean interactive = true;

    /**
     * The no. of copies to print.
     */
    private int copies;


    /**
     * Constructs an {@code AbstractPrinter}.
     */
    public AbstractPrinter() {
    }

    /**
     * Prints the object to the default printer.
     *
     * @throws OpenVPMSException for any error
     */
    public void print() {
        print(getDefaultPrinter());
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @return {@code true} if printing should occur interactively,
     *         {@code false} if it can be performed non-interactively
     */
    public boolean getInteractive() {
        return interactive;
    }

    /**
     * Sets the number of copies to print.
     *
     * @param copies the no. of copies to print
     */
    public void setCopies(int copies) {
        this.copies = copies;
    }

    /**
     * Returns the number of copies to print.
     *
     * @return the no. of copies to print
     */
    public int getCopies() {
        return copies;
    }

    /**
     * Returns the print properties for an object.
     *
     * @param printer the printer
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    protected PrintProperties getProperties(String printer) {
        PrintProperties result = new PrintProperties(printer);
        result.setCopies(getCopies());
        return result;
    }

    /**
     * Returns the print properties for an object.
     *
     * @param printer  the printer
     * @param template the document template. May be {@code null}
     * @param context  the context
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    protected PrintProperties getProperties(String printer, DocumentTemplate template, Context context) {
        PrintProperties properties = new PrintProperties(printer);
        properties.setCopies(getCopies());
        if (template != null) {
            properties.setMediaSize(template.getMediaSize());
            properties.setOrientation(template.getOrientationRequested());
            properties.setMediaTray(getMediaTray(template, printer, context));
        }
        return properties;
    }

    /**
     * Prints a document, or downloads it to the client if printing is not
     * supported.
     *
     * @param document the document to print
     * @param printer  the printer
     */
    protected void print(Document document, String printer) {
        String mimeType = document.getMimeType();
        if (DocFormats.ODT_TYPE.equals(mimeType) || DocFormats.DOC_TYPE.equals(mimeType)) {
            OpenOfficeHelper.getPrintService().print(document, printer, getCopies());
        } else {
            DownloadServlet.startDownload(document);
        }
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @param interactive if {@code true} print interactively
     */
    protected void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Helper to return the default printer for a template for the current practice or location.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @param context  the context
     * @return the default printer
     */
    protected String getDefaultPrinter(DocumentTemplate template, Context context) {
        return PrintHelper.getDefaultPrinter(template, context);
    }

    /**
     * Helper to return the document template printer relationship for a template and printer and current
     * location/practice.
     *
     * @param template an template
     * @param printer  the printer name
     * @param context  the context
     * @return the relationship, or {@code null} if none is found
     */
    protected DocumentTemplatePrinter getDocumentTemplatePrinter(DocumentTemplate template, String printer,
                                                                 Context context) {
        DocumentTemplatePrinter relationship = PrintHelper.getDocumentTemplatePrinter(template, context);
        if (relationship != null) {
            // make sure the relationship is for the same printer
            if (ObjectUtils.equals(printer, relationship.getPrinterName())) {
                return relationship;
            }
        }
        return null;
    }

    /**
     * Helper to return the media tray for a document template for a particular printer for the current practice.
     *
     * @param template the template
     * @param printer  the printer name
     * @param context  the context
     * @return the media tray for the template, or {@code null} if none is defined
     */
    protected MediaTray getMediaTray(DocumentTemplate template, String printer, Context context) {
        DocumentTemplatePrinter relationship = getDocumentTemplatePrinter(template, printer, context);
        return relationship != null ? relationship.getMediaTray() : null;
    }

    /**
     * Helper to determine if printing should occur interactively for a
     * particular document template, printer and the current practice.
     * If no relationship is defined, defaults to {@code true}.
     *
     * @param template the template
     * @param printer  the printer name
     * @param context  the context
     * @return {@code true} if printing should occur interactively
     */
    protected boolean getInteractive(DocumentTemplate template, String printer, Context context) {
        DocumentTemplatePrinter relationship = getDocumentTemplatePrinter(template, printer, context);
        return relationship == null || relationship.getInteractive();
    }


}
