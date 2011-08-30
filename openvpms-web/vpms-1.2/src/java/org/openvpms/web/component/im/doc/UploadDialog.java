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

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import nextapp.echo2.app.filetransfer.UploadSelect;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.resource.util.Messages;

import java.util.TooManyListenersException;


/**
 * File upload dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UploadDialog extends PopupDialog {

    /**
     * Construct a new <code>UploadDialog</code>.
     */
    public UploadDialog(final UploadListener listener) {
        super(Messages.get("file.upload.title"), CANCEL);
        setModal(true);
        UploadSelect select = new UploadSelect();

        UploadListener delegate = new UploadListener() {
            public void fileUpload(UploadEvent event) {
                close();
                listener.fileUpload(event);
            }

            public void invalidFileUpload(UploadEvent event) {
                close();
                listener.invalidFileUpload(event);
            }
        };

        try {
            select.addUploadListener(delegate);
        } catch (TooManyListenersException exception) {
            throw new RuntimeException(exception);
        }
        getLayout().add(select);
    }

}
