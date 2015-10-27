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

package org.openvpms.archetype.rules.doc;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * Text document handler.
 *
 * @author Tim Anderson
 */
public class TextDocumentHandler extends AbstractTextDocumentHandler {

    /**
     * The plain text mime type.
     */
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * Constructs a {@link TextDocumentHandler}.
     *
     * @param service the archetype service
     */
    public TextDocumentHandler(IArchetypeService service) {
        super(DocumentArchetypes.TEXT_DOCUMENT, service);
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param name     the document name
     * @param mimeType the mime type of the document. May be {@code null}
     * @return {@code true} if this handler supports the document
     */
    @Override
    public boolean canHandle(String name, String mimeType) {
        return name.toLowerCase().endsWith(".txt") || TEXT_PLAIN.equalsIgnoreCase(mimeType);
    }

    /**
     * Creates a document from a string.
     *
     * @param name    the document
     * @param content the content
     * @return a new document
     */
    public Document create(String name, String content) {
        return create(name, content, TEXT_PLAIN);
    }
}
