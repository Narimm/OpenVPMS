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
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.dialog.PrintDialog;
import org.openvpms.web.component.im.doc.DownloadHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link IMObjectPrinter} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectPrinter implements IMObjectPrinter {

    /**
     * Localised type display name (e.g, Customer, Product).
     */
    private final String _type;

    /**
     * The print listener. May be <code>null</code>.
     */
    private IMObjectPrinterListener _listener;


    /**
     * Constructs a new <code>AbstractIMObjectPrinter</code>.
     *
     * @param type display name for the types of objects that this may
     *             print
     */
    public AbstractIMObjectPrinter(String type) {
        _type = type;
    }

    /**
     * Pops up a {@link PrintDialog} prompting if printing of an object
     * should proceed, invoking {@link #doPrint} if 'OK' is selected, or
     * {@link #doPrintPreview} if 'preview' is selected.
     *
     * @param object the object to print
     */
    public void print(final IMObject object) {
        String title = Messages.get("imobject.print.title", _type);
        final PrintDialog dialog = new PrintDialog(title);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                String action = dialog.getAction();
                if (PrintDialog.OK_ID.equals(action)) {
                    doPrint(object, null);
                } else if (PrintDialog.PREVIEW_ID.equals(action)) {
                    doPrintPreview(object);
                }
            }
        });
        dialog.show();
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
     * Prints the object.
     *
     * @param object  the object to print
     * @param printer the printer
     */
    protected void doPrint(IMObject object, String printer) {
        try {
            Document document = getDocument(object);
            DownloadHelper.download(document);
            printed(object);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
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
            DownloadHelper.download(document);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }
}
