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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.print;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.mail.MailDialogFactory;
import org.openvpms.web.component.mail.MailEditor;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.VetoListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * {@link Printer} implementation that provides interactive support if
 * the underlying implementation requires it. Pops up a dialog with options to
 * print, preview, or cancel.
 *
 * @author Tim Anderson
 */
public class InteractivePrinter implements Printer {

    /**
     * The printer to delegate to.
     */
    private final Printer printer;

    /**
     * The print listener. May be {@code null}.
     */
    private PrinterListener listener;

    /**
     * The cancel listener. May be {@code null}.
     */
    private VetoListener cancelListener;

    /**
     * The dialog title.
     */
    private final String title;

    /**
     * If {@code true} display a 'skip' button that simply closes the dialog.
     */
    private final boolean skip;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Determines if printing should occur interactively or be performed
     * without user intervention. If the printer doesn't support non-interactive
     * printing, requests to do non-interactive printing are ignored.
     */
    private boolean interactive;

    /**
     * The mail context. If non-null, documents may be mailed.
     */
    private MailContext mailContext;

    /**
     * The print dialog, or {@code null} if none is being displayed.
     */
    private PrintDialog dialog;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InteractivePrinter.class);


    /**
     * Constructs an {@link InteractivePrinter}.
     *
     * @param printer the printer to delegate to
     * @param context the context
     * @param help    the help context
     */
    public InteractivePrinter(Printer printer, Context context, HelpContext help) {
        this(printer, false, context, help);
    }

    /**
     * Constructs an {@link InteractivePrinter}.
     *
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    public InteractivePrinter(Printer printer, boolean skip, Context context, HelpContext help) {
        this(null, printer, skip, context, help);
    }

    /**
     * Constructs an {@link InteractivePrinter}.
     *
     * @param title   the dialog title. May be {@code null}
     * @param printer the printer to delegate to
     * @param context the context
     * @param help    the help context
     */
    public InteractivePrinter(String title, Printer printer, Context context, HelpContext help) {
        this(title, printer, false, context, help);
    }

    /**
     * Constructs an {@link InteractivePrinter}.
     *
     * @param title   the dialog title. May be {@code null}
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    public InteractivePrinter(String title, Printer printer, boolean skip, Context context, HelpContext help) {
        this.title = title;
        this.printer = printer;
        this.skip = skip;
        this.context = context;
        this.help = help;
        interactive = printer.getInteractive();
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
     * Initiates printing of an object.
     * If the underlying printer requires interactive support or no printer
     * is specified, pops up a {@link PrintDialog} prompting if printing of an
     * object should proceed, invoking {@link #doPrint} if 'OK' is selected, or
     * {@link #download} if 'preview' is selected.
     *
     * @param printer the printer name. May be {@code null}
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        if (interactive || printer == null) {
            printInteractive(printer);
        } else if (!PrintHelper.exists(printer)) {
            log.warn("Printer not found: " + printer);
            printInteractive(printer);
        } else {
            printDirect(printer);
        }
    }

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or {@code null} if none
     * is defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        return printer.getDefaultPrinter();
    }

    /**
     * Returns a document for the object, corresponding to that which would be printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument() {
        return getDocument(DocFormats.PDF_TYPE, false);
    }

    /**
     * Returns a document for the object, corresponding to that which would be printed.
     *
     * @param mimeType the mime type. If {@code null} the default mime type associated with the report will be used.
     * @param email    if {@code true} indicates that the document will be emailed. Documents generated from templates
     *                 can perform custom formatting
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument(String mimeType, boolean email) {
        return printer.getDocument(mimeType, email);
    }

    /**
     * Determines if printing should occur interactively or be performed
     * without user intervention.
     *
     * @param interactive if {@code true} print interactively
     * @throws OpenVPMSException for any error
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @return {@code true} if printing should occur interactively,
     * {@code false} if it can be performed non-interactively
     * @throws OpenVPMSException for any error
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
        printer.setCopies(copies);
    }

    /**
     * Returns the number of copies to print.
     *
     * @return the no. of copies to print
     */
    public int getCopies() {
        return printer.getCopies();
    }

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    public String getDisplayName() {
        return printer.getDisplayName();
    }

    /**
     * Sets the listener for print events.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(PrinterListener listener) {
        this.listener = listener;
    }

    /**
     * Sets a listener to veto cancel events.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setCancelListener(VetoListener listener) {
        cancelListener = listener;
    }

    /**
     * Sets a context to support mailing documents.
     *
     * @param context the mail context. If {@code null}, mailing won't be enabled
     */
    public void setMailContext(MailContext context) {
        this.mailContext = context;
    }

    /**
     * Returns the mail context.
     *
     * @return the mail context
     */
    public MailContext getMailContext() {
        return mailContext;
    }

    /**
     * Returns the print dialog.
     *
     * @return the print dialog, if one is being displayed, otherwise {@code null}
     */
    public PrintDialog getPrintDialog() {
        return dialog;
    }

    /**
     * Returns the underlying printer.
     *
     * @return the printer
     */
    protected Printer getPrinter() {
        return printer;
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    protected String getTitle() {
        return title;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    protected HelpContext getHelpContext() {
        return help;
    }

    /**
     * Creates a new print dialog.
     *
     * @return a new print dialog
     */
    protected PrintDialog createDialog() {
        String title = getTitle();
        if (title == null) {
            title = Messages.get("printdialog.title");
        }
        boolean mail = mailContext != null;
        return new PrintDialog(title, true, mail, skip, context.getLocation(), help) {
            @Override
            protected void onPreview() {
                download();
            }

            @Override
            protected void onMail() {
                mail(this);
            }
        };
    }

    /**
     * Prints interactively.
     *
     * @param printerName the default printer to print to
     * @throws OpenVPMSException for any error
     */
    protected void printInteractive(String printerName) {
        dialog = createDialog();
        if (printerName == null) {
            printerName = getDefaultPrinter();
        }
        dialog.setDefaultPrinter(printerName);
        dialog.setCopies(printer.getCopies());
        dialog.setCancelListener(cancelListener);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                try {
                    handleAction(dialog);
                } finally {
                    dialog = null;
                }
            }
        });
        dialog.show();
    }

    /**
     * Print directly to the printer, without popping up any dialogs.
     *
     * @param printer the printer
     * @throws OpenVPMSException for any error
     */
    protected void printDirect(String printer) {
        doPrint(printer);
    }

    /**
     * Handles a print dialog action.
     *
     * @param dialog the dialog
     */
    protected void handleAction(PrintDialog dialog) {
        String action = dialog.getAction();
        if (PrintDialog.OK_ID.equals(action)) {
            String printerName = dialog.getPrinter();
            if (printerName == null) {
                // no printer so can't print. Download but notify listeners that it has been printed
                // so that the printed flag is updated
                download();
                printed(null);
            } else {
                printer.setCopies(dialog.getCopies());
                doPrint(printerName);
            }
        } else if (PrintDialog.SKIP_ID.equals(action)) {
            skipped();
        } else if (MailDialog.SEND_ID.equals(action)) {
            mailed();
        } else {
            cancelled();
        }
    }

    /**
     * Prints the object.
     *
     * @param printerName the printer name
     */
    protected void doPrint(String printerName) {
        try {
            printer.print(printerName);
            printed(printerName);
        } catch (Throwable exception) {
            failed(exception);
        }
    }

    /**
     * Generates a document and downloads it to the client.
     */
    protected void download() {
        try {
            Document document = getDocument();
            DownloadServlet.startDownload(document);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Generates a document and pops up a mail document with it as an attachment.
     * <p>
     * If emailed, then the print dialog is closed.
     *
     * @param parent the parent print dialog
     */
    protected void mail(final PrintDialog parent) {
        try {
            Document document = getDocument(DocFormats.PDF_TYPE, true);
            mail(document, parent);
        } catch (Throwable exception) {
            failed(exception);
        }
    }

    /**
     * Invoked when the object has been successfully printed.
     * Notifies any registered listener.
     *
     * @param printer the printer that was used to print the object.
     *                May be {@code null}
     */
    protected void printed(String printer) {
        if (listener != null) {
            listener.printed(printer);
        }
    }

    /**
     * Invoked when the print is cancelled.
     * Notifies any registered listener.
     */
    protected void cancelled() {
        if (listener != null) {
            listener.cancelled();
        }
    }

    /**
     * Invoked when the print is skipped.
     * Notifies any registered listener.
     */
    protected void skipped() {
        if (listener != null) {
            listener.skipped();
        }
    }

    /**
     * Invoked when the print job is mailed instead.
     * Notifies any registered listener that the printing has been skipped.
     */
    protected void mailed() {
        if (listener != null) {
            listener.skipped();
        }
    }

    /**
     * Invoked when the object has been failed to print.
     * Notifies any registered listener.
     *
     * @param exception the cause of the failure
     */
    protected void failed(Throwable exception) {
        if (listener != null) {
            listener.failed(exception);
        } else {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Mails a document as an attachment.
     * <p>
     * If emailed, then the print dialog is closed.
     *
     * @param document the document to mail
     * @param parent   the parent print dialog
     */
    protected void mail(Document document, final PrintDialog parent) {
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, help.subtopic("email"));
        final MailDialog dialog = createMailDialog(document, mailContext, layoutContext);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                if (MailDialog.SEND_ID.equals(dialog.getAction())) {
                    // close the parent dialog. This will notify registered listeners of the action taken,
                    // so need to propagate the action to the parent.
                    parent.close(MailDialog.SEND_ID);
                }
            }
        });
        show(dialog);
    }

    /**
     * Creates a dialog to email a document as an attachment.
     * <p>
     * If emailed, then the print dialog is closed.
     *
     * @param document      the document to mail
     * @param mailContext   the mail context
     * @param layoutContext the layout context
     */
    protected MailDialog createMailDialog(Document document, MailContext mailContext, LayoutContext layoutContext) {
        MailDialog dialog = ServiceHelper.getBean(MailDialogFactory.class).create(mailContext, layoutContext);
        MailEditor editor = dialog.getMailEditor();
        editor.setSubject(getDisplayName());
        editor.addAttachment(document);
        return dialog;
    }

    /**
     * Shows the dialog.
     *
     * @param dialog the dialog
     */
    protected void show(MailDialog dialog) {
        dialog.show();
    }

}
