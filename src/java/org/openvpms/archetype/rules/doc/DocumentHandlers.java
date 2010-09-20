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

package org.openvpms.archetype.rules.doc;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.UnsupportedDoc;
import org.openvpms.component.business.domain.im.document.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Maintains a set of {@link DocumentHandler} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentHandlers {

    /**
     * The list of registered handler.
     */
    private List<DocumentHandler> handlers
            = Collections.synchronizedList(new ArrayList<DocumentHandler>());

    /**
     * The fallback handler.
     */
    private DocumentHandler defaultHandler;


    /**
     * Adds a new document handler.
     *
     * @param handler the document handler
     */
    public void addDocumentHandler(DocumentHandler handler) {
        handlers.add(handler);
    }

    /**
     * Sets the document handlers.
     *
     * @param handlers the document handlers
     */
    public void setDocumentHandlers(List<DocumentHandler> handlers) {
        this.handlers.clear();
        this.handlers.addAll(handlers);
    }

    /**
     * Finds a handler for a document.
     *
     * @param document the document
     * @return a handler for the document, or <code>null</code> if none is found
     */
    public DocumentHandler find(Document document) {
        return find(document.getName(),
                    document.getArchetypeId().getShortName(),
                    document.getMimeType());
    }

    /**
     * Returns a handler for a document.
     *
     * @param document the document
     * @return a handler for the document
     * @throws DocumentException if no handler exists
     */
    public DocumentHandler get(Document document) {
        return get(document.getName(), document.getArchetypeId().getShortName(),
                   document.getMimeType());
    }

    /**
     * Finds a handler for a document.
     *
     * @param name     the document name
     * @param mimeType the mime type of the document
     * @return a handler for the document, or <code>null</code> if none is found
     */
    public DocumentHandler find(String name, String mimeType) {
        for (DocumentHandler handler : handlers) {
            if (handler.canHandle(name, mimeType)) {
                return handler;
            }
        }
        if (getDefaultHandler().canHandle(name, mimeType)) {
            return getDefaultHandler();
        }
        return null;
    }


    /**
     * Returns a handler for a document.
     *
     * @param name     the document name
     * @param mimeType the mime type of the document
     * @return a handler for the document
     * @throws DocumentException if no handler exists
     */
    public DocumentHandler get(String name, String mimeType) {
        DocumentHandler handler = find(name, mimeType);
        if (handler == null) {
            throw new DocumentException(UnsupportedDoc, mimeType);
        }
        return handler;
    }


    /**
     * Returns a handler for a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document
     * @return a handler for the document, or <code>null</code> if none is found
     */
    public DocumentHandler find(String name, String shortName,
                                String mimeType) {
        for (DocumentHandler handler : handlers) {
            if (handler.canHandle(name, shortName, mimeType)) {
                return handler;
            }
        }
        if (getDefaultHandler().canHandle(name, shortName, mimeType)) {
            return getDefaultHandler();
        }
        return null;
    }

    /**
     * Returns a handler for a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document
     * @return a handler for the document
     * @throws DocumentException if no handler exists
     */
    public DocumentHandler get(String name, String shortName, String mimeType) {
        DocumentHandler handler = find(name, shortName, mimeType);
        if (handler == null) {
            throw new DocumentException(UnsupportedDoc, mimeType);
        }
        return handler;
    }

    /**
     * Returns the default handler.
     *
     * @return the default handler
     */
    private synchronized DocumentHandler getDefaultHandler() {
        if (defaultHandler == null) {
            defaultHandler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT);
        }
        return defaultHandler;
    }

}
