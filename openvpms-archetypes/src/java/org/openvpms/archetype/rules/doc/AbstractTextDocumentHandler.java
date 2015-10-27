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

import java.nio.charset.Charset;

/**
 * A document handler for text documents.
 * <p/>
 * Text documents are not compressed, to allow searching.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTextDocumentHandler extends AbstractDocumentHandler {

    /**
     * The charset to use.
     */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Constructs an {@link AbstractTextDocumentHandler}.
     *
     * @param shortName the document archetype short name
     * @param service   the archetype service
     */
    public AbstractTextDocumentHandler(String shortName, IArchetypeService service) {
        super(shortName, service, false);
    }

    /**
     * Creates a document from a string.
     *
     * @param name     the document
     * @param content  the content
     * @param mimeType the mime type
     * @return a new document
     */
    public Document create(String name, String content, String mimeType) {
        byte[] bytes = content.getBytes(CHARSET);
        return create(name, bytes, mimeType, bytes.length);
    }

    /**
     * Updates a document.
     *
     * @param document the document
     * @param content  the new content. May be null
     */
    public void update(Document document, String content) {
        byte[] bytes = (content != null) ? content.getBytes(CHARSET) : new byte[0];
        int length = (content != null) ? content.length() : 0;
        update(document, document.getName(), bytes, document.getMimeType(), length, calculateChecksum(bytes));
    }

    /**
     * Converts a document to text.
     *
     * @param document the document
     * @return the text
     */
    public String toString(Document document) {
        byte[] bytes = document.getContents();
        return new String(bytes, CHARSET);
    }

}
