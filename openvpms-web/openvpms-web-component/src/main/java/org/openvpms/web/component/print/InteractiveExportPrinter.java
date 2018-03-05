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

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;

/**
 * An interactive printer that supports exporting to CSV and mailing.
 *
 * @author Tim Anderson
 */
public class InteractiveExportPrinter extends InteractivePrinter {

    /**
     * Constructs an {@link InteractiveExportPrinter}.
     *
     * @param printer     the printer to delegate to
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     */
    public InteractiveExportPrinter(Printer printer, Context context, MailContext mailContext, HelpContext help) {
        this(null, printer, context, mailContext, help);
    }

    /**
     * Constructs an {@link InteractivePrinter}.
     *
     * @param title       the dialog title. May be {@code null}
     * @param printer     the printer to delegate to
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     */
    public InteractiveExportPrinter(String title, Printer printer, Context context, MailContext mailContext,
                                    HelpContext help) {
        this(title, printer, false, context, mailContext, help);
    }

    /**
     * Constructs an {@link InteractiveExportPrinter}.
     *
     * @param title       the dialog title. May be {@code null}
     * @param printer     the printer to delegate to
     * @param skip        if {@code true} display a 'skip' button that simply closes the dialog
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     */
    public InteractiveExportPrinter(String title, Printer printer, boolean skip, Context context,
                                    MailContext mailContext, HelpContext help) {
        super(title, printer, skip, context, help);
        setMailContext(mailContext);
    }

    /**
     * Creates a new print dialog.
     *
     * @return a new print dialog
     */
    @Override
    protected PrintDialog createDialog() {
        return new ExportPrintDialog(getTitle(), getContext().getLocation(), getHelpContext()) {

            @Override
            protected void onPreview() {
                doPrintPreview();
            }

            @Override
            protected void onMail() {
                mail(this);
            }

            @Override
            protected void onExport() {
                export();
            }

            @Override
            protected void onExportMail() {
                exportMail(this);
            }
        };
    }

    /**
     * Exports the document to CSV and downloads it.
     */
    protected void export() {
        try {
            Document document = getDocument(DocFormats.CSV_TYPE, false);
            DownloadServlet.startDownload(document);
        } catch (Throwable exception) {
            failed(exception);
        }
    }

    /**
     * Exports the document to CSV and mails it.
     */
    protected void exportMail(PrintDialog parent) {
        try {
            Document document = getDocument(DocFormats.CSV_TYPE, false);
            mail(document, parent);
        } catch (Throwable exception) {
            failed(exception);
        }
    }

}
