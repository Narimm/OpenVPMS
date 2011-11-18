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

package org.openvpms.web.component.print;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;


/**
 * {@link Printer} implementation that provides interactive support if
 * the underlying implementation requires it. Pops up a dialog with options to
 * print, preview, or cancel.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InteractivePrinter implements Printer {

    /**
     * The printer to delegate to.
     */
    private final Printer printer;

    /**
     * The print listener. May be <tt>null</tt>.
     */
    private PrinterListener listener;

    /**
     * The cancel listener. May be <tt>null</tt>.
     */
    private VetoListener cancelListener;

    /**
     * The dialog title.
     */
    private final String title;

    /**
     * If <tt>true</tt> display a 'skip' button that simply closes the dialog.
     */
    private final boolean skip;

    /**
     * Determines if printing should occur interactively or be performed
     * without user intervention. If the printer doesn't support non-interactive
     * printing, requests to do non-interactive printing are ignored.
     */
    private boolean interactive;

    /**
     * The mail context. If non-null, documents may be mailed.
     */
    private MailContext context;


    /**
     * Constructs a new <tt>InteractivePrinter</tt>.
     *
     * @param printer the printer to delegate to
     */
    public InteractivePrinter(Printer printer) {
        this(printer, false);
    }

    /**
     * Constructs a new <tt>InteractivePrinter</tt>.
     *
     * @param printer the printer to delegate to
     * @param skip    if <tt>true</tt> display a 'skip' button that simply
     *                closes the dialog
     */
    public InteractivePrinter(Printer printer, boolean skip) {
        this(null, printer, skip);
    }

    /**
     * Constructs a new <tt>InteractivePrinter</tt>.
     *
     * @param title   the dialog title. May be <tt>null</tt>
     * @param printer the printer to delegate to
     */
    public InteractivePrinter(String title, Printer printer) {
        this(title, printer, false);
    }

    /**
     * Constructs a new <tt>InteractivePrinter</tt>.
     *
     * @param title   the dialog title. May be <tt>null</tt>
     * @param printer the printer to delegate to
     * @param skip    if <tt>true</tt> display a 'skip' button that simply
     *                closes the dialog
     */
    public InteractivePrinter(String title, Printer printer, boolean skip) {
        this.title = title;
        this.printer = printer;
        this.skip = skip;
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
     * {@link #doPrintPreview} if 'preview' is selected.
     *
     * @param printer the printer name. May be <tt>null</tt>
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        if (interactive || printer == null) {
            printInteractive(printer);
        } else {
            printDirect(printer);
        }
    }

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or <tt>null</tt> if none
     *         is defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        return printer.getDefaultPrinter();
    }

    /**
     * Returns a document for the object, corresponding to that which would be
     * printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument() {
        return getDocument(DocFormats.PDF_TYPE);
    }

    /**
     * Returns a document for the object, corresponding to that which would be
     * printed.
     *
     * @param format the document format to return
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument(String format) {
        return printer.getDocument(format);
    }

    /**
     * Determines if printing should occur interactively or be performed
     * without user intervention.
     *
     * @param interactive if <tt>true</tt> print interactively
     * @throws OpenVPMSException for any error
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @return <tt>true</tt> if printing should occur interactively,
     *         <tt>false</tt> if it can be performed non-interactively
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
     * Sets the listener for print events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setListener(PrinterListener listener) {
        this.listener = listener;
    }

    /**
     * Sets a listener to veto cancel events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setCancelListener(VetoListener listener) {
        cancelListener = listener;
    }

    /**
     * Sets a context to support mailing documents.
     *
     * @param context the mail context. If <tt>null</tt>, mailing won't be enabled
     */
    public void setMailContext(MailContext context) {
        this.context = context;
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
     * Creates a new print dialog.
     *
     * @return a new print dialog
     */
    protected PrintDialog createDialog() {
        String title = getTitle();
        if (title == null) {
            title = Messages.get("printdialog.title");
        }
        boolean mail = context != null;
        return new PrintDialog(title, true, mail, skip) {
            @Override
            protected void onPreview() {
                doPrintPreview();
            }

            @Override
            protected void onMail() {
                doMail();
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
        final PrintDialog dialog = createDialog();
        if (printerName == null) {
            printerName = getDefaultPrinter();
        }
        dialog.setDefaultPrinter(printerName);
        dialog.setCopies(printer.getCopies());
        dialog.setCancelListener(cancelListener);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                String action = dialog.getAction();
                if (PrintDialog.OK_ID.equals(action)) {
                    String printerName = dialog.getPrinter();
                    if (printerName == null) {
                        doPrintPreview();
                    } else {
                        printer.setCopies(dialog.getCopies());
                        doPrint(printerName);
                    }
                } else if (PrintDialog.SKIP_ID.equals(action)) {
                    skipped();
                } else {
                    cancelled();
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
     * Prints the object.
     *
     * @param printerName the printer name
     */
    protected void doPrint(String printerName) {
        try {
            printer.print(printerName);
            printed(printerName);
        } catch (OpenVPMSException exception) {
            failed(exception);
        }
    }

    /**
     * Generates a document and downloads it to the client.
     */
    protected void doPrintPreview() {
        try {
            Document document = getDocument();
            DownloadServlet.startDownload(document);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Generates a document and pops up a mail document with it as an attachment.
     */
    protected void doMail() {
        try {
            Document document = getDocument();
            MailDialog dialog = new MailDialog(context);
            dialog.getMailEditor().addAttachment(document);
            dialog.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the object has been successfully printed.
     * Notifies any registered listener.
     *
     * @param printer the printer that was used to print the object.
     *                May be <tt>null</tt>
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
}
