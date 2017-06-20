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

package org.openvpms.report.openoffice;

import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.DocFormats;


/**
 * Converts a {@link Document} from one format to another.
 *
 * @author Tim Anderson
 */
public class Converter {

    /**
     * The OpenOffice connection pool.
     */
    private final OOConnectionPool pool;

    /**
     * The document handlers
     */
    private final DocumentHandlers handlers;

    /**
     * Supported conversions on source mime type -> target mime type.
     */
    private static final String[][] MIME_MAP = {{DocFormats.ODT_TYPE, DocFormats.PDF_TYPE},
                                                {DocFormats.DOC_TYPE, DocFormats.PDF_TYPE},
                                                {DocFormats.DOCX_TYPE, DocFormats.PDF_TYPE},
                                                {DocFormats.RTF_TYPE, DocFormats.HTML_TYPE}};

    /**
     * Supported conversions on extension -> target mime type.
     */
    private static final String[][] EXT_MAP = {{DocFormats.ODT_EXT, DocFormats.PDF_TYPE},
                                               {DocFormats.DOC_EXT, DocFormats.PDF_TYPE},
                                               {DocFormats.DOCX_EXT, DocFormats.PDF_TYPE},
                                               {DocFormats.ODT_EXT, DocFormats.HTML_TYPE},
                                               {DocFormats.DOC_EXT, DocFormats.HTML_TYPE},
                                               {DocFormats.DOCX_EXT, DocFormats.HTML_TYPE},
                                               {DocFormats.RTF_EXT, DocFormats.HTML_TYPE}};

    /**
     * Constructs a {@link Converter}.
     *
     * @param pool     the OpenOffice connection pool
     * @param handlers the document handlers
     */
    public Converter(OOConnectionPool pool, DocumentHandlers handlers) {
        this.pool = pool;
        this.handlers = handlers;
    }

    /**
     * Determines if a document can be converted.
     *
     * @param document the document to convert
     * @param mimeType the target mime type
     * @return {@code true} if the document can be converted, otherwise {@code false}
     */
    public boolean canConvert(Document document, String mimeType) {
        return canConvert(document.getName(), document.getMimeType(), mimeType);
    }

    /**
     * Determines if a document can be converted.
     *
     * @param fileName       the document file name
     * @param sourceMimeType the document mime type
     * @param targetMimeType the target mime type
     * @return {@code true} if the document can be converted, otherwise {@code false}
     */
    public boolean canConvert(String fileName, String sourceMimeType, String targetMimeType) {
        for (String[] map : MIME_MAP) {
            if (map[0].equals(sourceMimeType)
                && map[1].equals(targetMimeType)) {
                return true;
            }
        }
        if (fileName != null) {
            String ext = FilenameUtils.getExtension(fileName);
            for (String[] map : EXT_MAP) {
                if (map[0].equals(ext)
                    && map[1].equals(targetMimeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Converts a document.
     *
     * @param document the document to convert
     * @param mimeType the target mime type
     * @return the converted document
     * @throws OpenOfficeException if the document cannot be converted
     */
    public Document convert(Document document, String mimeType) {
        try (OOConnection connection = pool.getConnection()) {
            OpenOfficeDocument doc = createDocument(document, connection);
            try {
                String name = FilenameUtils.getBaseName(document.getName());
                return doc.export(mimeType, name);
            } finally {
                doc.close();
            }
        }
    }

    /**
     * Exports the document.
     *
     * @param mimeType the mime-type of the document format to export to
     * @return the exported document serialized to a byte array
     * @throws OpenOfficeException if the document cannot be exported
     */
    public byte[] export(Document document, String mimeType) {
        try (OOConnection connection = pool.getConnection()) {
            OpenOfficeDocument doc = createDocument(document, connection);
            try {
                return doc.export(mimeType);
            } finally {
                doc.close();
            }
        }
    }

    /**
     * Creates an OpenOffice document.
     *
     * @param document   the document
     * @param connection the connection
     * @return a new OpenOffice document
     */
    protected OpenOfficeDocument createDocument(Document document, OOConnection connection) {
        OpenOfficeDocument doc = new OpenOfficeDocument(document, connection, handlers);
        doc.refresh();
        // workaround to avoid corruption of generated doc
        // when the source document contains user fields.
        // Alternative approach is to do a Thread.sleep(1000).
        return doc;
    }

}
