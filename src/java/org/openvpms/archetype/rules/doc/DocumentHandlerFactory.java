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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Factory of {@link DocumentHandler} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentHandlerFactory {

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
     * Determines if this factory supports a document.
     *
     * @param name     the document name
     * @param mimeType the mime type of the document
     * @return a handler for the document, or <code>null</code> if none is found
     * @throws DocumentException for any error
     */
    public DocumentHandler get(String name, String mimeType) {
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
     * Determines if this factory supports a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document
     * @return a handler for the document, or <code>null</code> if none is found
     * @throws DocumentException for any error
     */
    public DocumentHandler get(String name, String shortName, String mimeType) {
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
     * Returns the default factory.
     *
     * @return the default factory
     */
    private synchronized DocumentHandler getDefaultHandler() {
        if (defaultHandler == null) {
            defaultHandler = new DefaultDocumentHandler("document.other");
        }
        return defaultHandler;
    }

}
