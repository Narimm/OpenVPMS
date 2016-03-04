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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.MacroMailContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * {@link IMPrinter} implementation that provides interactive support if
 * the underlying implementation requires it. Pops up a dialog with options to
 * print, preview, or cancel.
 *
 * @author Tim Anderson
 */
public class InteractiveIMPrinter<T> extends InteractivePrinter implements IMPrinter<T> {

    /**
     * Constructs an {@link InteractiveIMPrinter}.
     *
     * @param printer the printer to delegate to
     * @param context the context
     * @param help    the help context
     */
    public InteractiveIMPrinter(IMPrinter<T> printer, Context context, HelpContext help) {
        this(printer, false, context, help);
    }

    /**
     * Constructs an {@link InteractiveIMPrinter}.
     *
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    public InteractiveIMPrinter(IMPrinter<T> printer, boolean skip, Context context, HelpContext help) {
        this(null, printer, skip, context, help);
    }

    /**
     * Constructs an {@link InteractiveIMPrinter}.
     *
     * @param title   the dialog title. May be {@code null}
     * @param printer the printer to delegate to
     * @param context the context
     * @param help    the help context
     */
    public InteractiveIMPrinter(String title, IMPrinter<T> printer, Context context, HelpContext help) {
        this(title, printer, false, context, help);
    }

    /**
     * Constructs an {@link InteractiveIMPrinter}.
     *
     * @param title   the dialog title. May be {@code null}
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    public InteractiveIMPrinter(String title, IMPrinter<T> printer, boolean skip, Context context, HelpContext help) {
        super(title, printer, skip, context, help);
    }

    /**
     * Returns the objects being printed.
     *
     * @return the objects being printed
     */
    public Iterable<T> getObjects() {
        return getPrinter().getObjects();
    }

    /**
     * Returns the reporter.
     *
     * @return the reporter
     */
    @Override
    public Reporter<T> getReporter() {
        return getPrinter().getReporter();
    }

    /**
     * Returns the underlying printer.
     *
     * @return the underlying printer
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMPrinter<T> getPrinter() {
        return (IMPrinter<T>) super.getPrinter();
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    protected String getTitle() {
        String title = super.getTitle();
        if (title != null) {
            return title;
        }
        return Messages.format("imobject.print.title", getDisplayName());
    }

    /**
     * Creates a dialog to a document as an attachment.
     * <p/>
     * If emailed, then the print dialog is closed.
     *
     * @param document      the document to mail
     * @param mailContext   the mail context
     * @param layoutContext the layout context
     */
    @Override
    protected MailDialog createMailDialog(Document document, MailContext mailContext, LayoutContext layoutContext) {
        Reporter<T> reporter = getReporter();
        Object macroContext = reporter.getObject();
        if (macroContext != null) {
            mailContext = new MacroMailContext(mailContext, macroContext);
        }
        MailDialog dialog = super.createMailDialog(document, mailContext, layoutContext);
        DocumentTemplate template = reporter.getTemplate();
        if (template != null) {
            Entity emailTemplate = template.getEmailTemplate();
            if (emailTemplate != null) {
                dialog.getMailEditor().setContent(emailTemplate);
            }
        }
        return dialog;
    }
}
