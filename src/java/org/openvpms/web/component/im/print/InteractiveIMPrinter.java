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

package org.openvpms.web.component.im.print;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;


/**
 * {@link IMPrinter} implementation that provides interactive support if
 * the underlying implementation requires it. Pops up a dialog with options to
 * print, preview, or cancel.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InteractiveIMPrinter<T> implements IMPrinter<T> {

    /**
     * The printer to delegate to.
     */
    private final IMPrinter<T> printer;

    /**
     * The print listener. May be <code>null</code>.
     */
    private IMPrinterListener listener;

    /**
     * The dialog title.
     */
    private final String title;

    /**
     * If <code>triue</code> display a 'skip' button that simply closes the
     * dialog.
     */
    private final boolean skip;

    /**
     * Determines if printing should occur interactively or be performed
     * without user intervention. If the printer doesn't support non-interactive
     * printing, requests to do non-interactive printing are ignored.
     */
    private boolean interactive;


    /**
     * Constructs a new <code>InteractiveIMPrinter</code>.
     *
     * @param printer the printer to delegate to
     */
    public InteractiveIMPrinter(IMPrinter<T> printer) {
        this(printer, false);
    }

    /**
     * Constructs a new <tt>InteractiveIMPrinter</tt>.
     *
     * @param printer the printer to delegate to
     * @param skip    if <code>triue</code> display a 'skip' button that simply
     *                closes the dialog
     */
    public InteractiveIMPrinter(IMPrinter<T> printer, boolean skip) {
        this(null, printer, skip);
    }

    /**
     * Constructs a new <tt>InteractiveIMPrinter</tt>.
     *
     * @param title   the dialog title. May be <tt>null</tt>
     * @param printer the printer to delegate to
     */
    public InteractiveIMPrinter(String title, IMPrinter<T> printer) {
        this(title, printer, false);
    }

    /**
     * Constructs a new <code>InteractiveIMPrinter</code>.
     *
     * @param title   the dialog title. May be <tt>null</tt>
     * @param printer the printer to delegate to
     * @param skip    if <code>triue</code> display a 'skip' button that simply
     *                closes the dialog
     */
    public InteractiveIMPrinter(String title, IMPrinter<T> printer,
                                boolean skip) {
        this.title = title;
        this.printer = printer;
        this.skip = skip;
        interactive = printer.getInteractive();
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
     * Returns the objects being printed.
     *
     * @return the objects being printed
     */
    public Iterable<T> getObjects() {
        return printer.getObjects();
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
     * If the underlying printer requires interactive support, pops up a
     * {@link PrintDialog} prompting if printing of an object should
     * proceed, invoking {@link #doPrint} if 'OK' is selected, or
     * {@link #doPrintPreview} if 'preview' is selected.
     *
     * @param printer the printer name. May be <code>null</code>
     */
    public void print(String printer) {
        if (interactive) {
            printInteractive(printer);
        } else {
            printDirect(printer);
        }
    }

    protected void printDirect(String printer) {
        if (printer == null) {
            printer = getDefaultPrinter();
        }
        doPrint(printer);
    }

    protected void printInteractive(String printer) {
        final PrintDialog dialog = new PrintDialog(getTitle(), true, skip) {
            @Override
            protected void onPreview() {
                doPrintPreview();
            }
        };

        try {
            if (printer == null) {
                printer = getDefaultPrinter();
            }
            dialog.setDefaultPrinter(printer);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    String action = dialog.getAction();
                    if (PrintDialog.OK_ID.equals(action)) {
                        String printer = dialog.getPrinter();
                        if (printer == null) {
                            doPrintPreview();
                        } else {
                            doPrint(printer);
                        }
                    } else if (PrintDialog.SKIP_ID.equals(action)) {
                        skipped();
                    } else {
                        cancelled();
                    }
                }
            });
            dialog.show();
        } catch (OpenVPMSException exception) {
            failed(exception);
        }
    }

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or <code>null</code> if none
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
        return printer.getDocument();
    }

    /**
     * Determines if printing should occur interactively or be performed
     * without user intervention. If the printer doesn't support non-interactive
     * printing, requests to do non-interactive printing are ignored.
     *
     * @param interactive if <tt>true</tt> print interactively
     * @throws OpenVPMSException for any error
     */
    public void setInteractive(boolean interactive) {
        if (printer.getInteractive()) {
            // must print interactively.
            this.interactive = true;
        } else {
            this.interactive = interactive;
        }
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @return <tt>true</tt> if printing should occur interactively,
     *         <tt>false</tt> if it can be performed non-interactively
     * @throws OpenVPMSException for any error
     */
    public boolean getInteractive() {
        return printer.getInteractive();
    }

    /**
     * Sets the listener for print events.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public void setListener(IMPrinterListener listener) {
        this.listener = listener;
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    protected String getTitle() {
        if (title != null) {
            return title;
        }
        return Messages.get("imobject.print.title", getDisplayName());
    }

    /**
     * Prints the object.
     *
     * @param printerName the printer name
     */
    protected void doPrint(String printerName) {
        try {
            printer.print(printerName);
            printed();
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
            failed(exception);
        }
    }

    /**
     * Invoked when the object has been successfully printed.
     * Notifies any registered listener.
     */
    protected void printed() {
        if (listener != null) {
            listener.printed();
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
