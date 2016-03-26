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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.doc;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Collections;
import java.util.List;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.UnsupportedDoc;


/**
 * Maintains a set of {@link DocumentHandler} instances.
 *
 * @author Tim Anderson
 */
public class DocumentHandlers {

    /**
     * The list of registered handler.
     */
    private final List<DocumentHandler> handlers;

    /**
     * The fallback handler.
     */
    private final DocumentHandler defaultHandler;



    /**
     * Constructs a {@link DocumentHandlers}.
     *
     * @param service  the archetype service
     */
    public DocumentHandlers(IArchetypeService service) {
        this(service, Collections.<DocumentHandler>emptyList());
    }

    /**
     * Constructs a {@link DocumentHandlers}.
     *
     * @param service  the archetype service
     * @param handlers the handlers
     */
    public DocumentHandlers(IArchetypeService service, List<DocumentHandler> handlers) {
        defaultHandler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT, service);
        this.handlers = handlers;
    }

    /**
     * Finds a handler for a document.
     *
     * @param document the document
     * @return a handler for the document, or {@code null} if none is found
     */
    public DocumentHandler find(Document document) {
        return find(document.getName(), document.getArchetypeId().getShortName(), document.getMimeType());
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
     * @return a handler for the document, or {@code null} if none is found
     */
    public DocumentHandler find(String name, String mimeType) {
        for (DocumentHandler handler : handlers) {
            if (handler.canHandle(name, mimeType)) {
                return handler;
            }
        }
        if (defaultHandler.canHandle(name, mimeType)) {
            return defaultHandler;
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
            throw new DocumentException(UnsupportedDoc, name, mimeType);
        }
        return handler;
    }

    /**
     * Returns a handler for a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document
     * @return a handler for the document, or {@code null} if none is found
     */
    public DocumentHandler find(String name, String shortName, String mimeType) {
        for (DocumentHandler handler : handlers) {
            if (handler.canHandle(name, shortName, mimeType)) {
                return handler;
            }
        }
        if (defaultHandler.canHandle(name, shortName, mimeType)) {
            return defaultHandler;
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
            throw new DocumentException(UnsupportedDoc, name, mimeType);
        }
        return handler;
    }

}
