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

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.io.InputStream;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.UnsupportedDoc;

/**
 * An {@link UploadListener} that creates an {@link Document} from the content.
 *
 * @author Tim Anderson
 */
public abstract class DocumentUploadListener extends AbstractUploadListener {

    /**
     * The handler to use, or {@code null} to select one using the {@link ServiceHelper#getDocumentHandlers()}.
     */
    private final DocumentHandler handler;

    /**
     * Default constructor.
     */
    public DocumentUploadListener() {
        this(null);
    }

    /**
     * Constructs a {@link DocumentUploadListener}.
     *
     * @param handler the handler to use. May be {@code null}
     */
    public DocumentUploadListener(DocumentHandler handler) {
        this.handler = handler;
    }

    /**
     * Uploads a file.
     *
     * @param event the upload event
     */
    public void fileUpload(UploadEvent event) {
        String fileName = event.getFileName();
        String contentType = event.getContentType();
        try {
            InputStream stream = event.getInputStream();
            int size = event.getSize();
            Document doc = getDocument(stream, fileName, contentType, size);
            upload(doc);
        } catch (DocumentException exception) {
            if (exception.getErrorCode() == UnsupportedDoc) {
                ErrorHelper.show(Messages.format("document.upload.unsupported", fileName, contentType));
            } else {
                ErrorHelper.show(exception);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates a document for the specified stream.
     *
     * @param stream      the document stream
     * @param fileName    the file name
     * @param contentType the content type
     * @param size        the size
     * @return a new document
     */
    protected Document getDocument(InputStream stream, String fileName, String contentType, int size) {
        DocumentHandler h;
        if (handler != null) {
            if (!handler.canHandle(fileName, contentType)) {
                throw new DocumentException(UnsupportedDoc, fileName, contentType);
            }
            h = handler;
        } else {
            h = getDocumentHandler(fileName, contentType);
        }
        return h.create(fileName, stream, contentType, size);
    }

    /**
     * Returns a document handler for the specified file name and content type.
     *
     * @param fileName    the file name
     * @param contentType the content type
     * @return a new document handler
     */
    protected DocumentHandler getDocumentHandler(String fileName, String contentType) {
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        return handlers.get(fileName, contentType);
    }

    /**
     * Invoked when a document has been uploaded.
     *
     * @param document the document
     */
    protected abstract void upload(Document document);
}
