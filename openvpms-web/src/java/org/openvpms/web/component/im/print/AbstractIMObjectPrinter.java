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

package org.openvpms.web.component.im.print;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.PrintProperties;
import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;

import java.util.Arrays;


/**
 * Abstract implementation of the {@link IMObjectPrinter} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectPrinter implements IMObjectPrinter {

    /**
     * The print listener. May be <code>null</code>.
     */
    private IMObjectPrinterListener _listener;

    /**
     * Determines if printing should occur interactively.
     */
    private boolean interactive = true;


    /**
     * Initiates printing of an object.
     * If printing interactively, pops up a {@link PrintDialog} prompting if
     * printing of an object should proceed, invoking {@link #doPrint} if 'OK'
     * is selected, or {@link #doPrintPreview} if 'preview' is selected.
     * If printing in the background, invokes {@link #doPrint}.
     *
     * @param object the object to print
     */
    public void print(final IMObject object) {
        if (isInteractive()) {
            String displayName = DescriptorHelper.getDisplayName(object);
            String title = Messages.get("imobject.print.title", displayName);
            final PrintDialog dialog = new PrintDialog(title);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    String action = dialog.getAction();
                    if (PrintDialog.OK_ID.equals(action)) {
                        String printer = dialog.getPrinter();
                        if (printer == null) {
                            doPrintPreview(object);
                        } else {
                            doPrint(object, printer);
                        }
                    } else if (PrintDialog.PREVIEW_ID.equals(action)) {
                        doPrintPreview(object);
                    }
                }
            });
            dialog.show();
        } else {
            doPrint(object, null);
        }
    }

    /**
     * Sets the listener for print events.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public void setListener(IMObjectPrinterListener listener) {
        _listener = listener;
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @param interactive if <code>true</code>, prompt the user
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Determines if printing should occur interactively.
     *
     * @return <code>true</code> if printing should be interactively;
     *         <code>false</code> if it should occur in the background
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Creates a new report.
     *
     * @param object the object to report on
     * @return a new report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected abstract IMObjectReport createReport(IMObject object);

    /**
     * Returns a document for an object.
     *
     * @param object the object
     * @return a document
     * @throws OpenVPMSException for any error
     */
    protected abstract Document getDocument(IMObject object);

    /**
     * Invoked when an object has been successfully printed.
     * Notifies any registered listener.
     *
     * @param object the object
     */
    protected void printed(IMObject object) {
        if (_listener != null) {
            _listener.printed(object);
        }
    }

    /**
     * Invoked when an object has been failed to print.
     * Notifies any registered listener.
     *
     * @param object    the object
     * @param exception the cause of the failure
     */
    protected void failed(IMObject object, Throwable exception) {
        if (_listener != null) {
            _listener.cancelled(object);
        }
    }

    /**
     * Prints the object.
     *
     * @param object  the object to print
     * @param printer the printer
     */
    protected void doPrint(IMObject object, String printer) {
        try {
            IMObjectReport report = createReport(object);
            report.print(Arrays.asList(object), new PrintProperties(printer));
            printed(object);
        } catch (OpenVPMSException exception) {
            if (isInteractive()) {
                ErrorHelper.show(exception);
            } else {
                failed(object, exception);
            }
        }
    }

    /**
     * Generates a document and downloads it to the client.
     *
     * @param object the object to preview
     */
    protected void doPrintPreview(IMObject object) {
        try {
            Document document = getDocument(object);
            DownloadServlet.startDownload(document);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }
}
