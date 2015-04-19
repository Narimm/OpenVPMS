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
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.io.InputStream;

/**
 * An {@link UploadListener} that creates an {@link Document} from the content.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class DocumentUploadListener extends AbstractUploadListener {

    /**
     * Uploads a file.
     *
     * @param event the upload event
     */
    public void fileUpload(UploadEvent event) {
        try {
            String fileName = event.getFileName();
            InputStream stream = event.getInputStream();
            String contentType = event.getContentType();
            Integer size = event.getSize();
            DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
            DocumentHandler handler = handlers.get(fileName, contentType);
            Document doc = handler.create(fileName, stream, contentType, size);
            upload(doc);
        } catch (Exception exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when a document has been uploaded.
     *
     * @param document the document
     */
    protected abstract void upload(Document document);
}
