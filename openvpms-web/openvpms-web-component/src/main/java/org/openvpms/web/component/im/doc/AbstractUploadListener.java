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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.i18n.Messages;


/**
 * Abstract implementation of the <tt>UploadListener</tt> interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractUploadListener implements UploadListener {

    /**
     * Invoked when an upload fails.
     *
     * @param event the upload event
     */
    public void invalidFileUpload(UploadEvent event) {
        String message = Messages.get("file.upload.failed", event.getFileName());
        ErrorDialog.show(message);
    }
}
