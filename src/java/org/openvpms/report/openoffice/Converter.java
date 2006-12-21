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

package org.openvpms.report.openoffice;

import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.DocFormats;


/**
 * Converts a {@link Document} from one format to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Converter {

    /**
     * The the connection to the OpenOffice service.
     */
    private final OOConnection connection;

    /**
     * The document handlers
     */
    private final DocumentHandlers handlers;


    /**
     * Constructs a new <code>Converter</code>.
     *
     * @param connection the connection to the OpenOffice service
     * @param handlers   the document handlers
     */
    public Converter(OOConnection connection,
                     DocumentHandlers handlers) {
        this.connection = connection;
        this.handlers = handlers;
    }

    /**
     * Determines if a document can be converted.
     *
     * @param document the document to convert
     * @param mimeType the target mime type
     * @return <code>true</code> if the document can be converted, otherwise
     *         <code>false</code>
     */
    public static boolean canConvert(Document document, String mimeType) {
        return canConvert(document.getName(), document.getMimeType(), mimeType);
    }

    /**
     * Determines if a document can be converted.
     *
     * @param fileName       the document file name
     * @param sourceMimeType the document mime type
     * @param targetMimeType the target mime type
     * @return <code>true</code> if the document can be converted, otherwise
     *         <code>false</code>
     */
    public static boolean canConvert(String fileName, String sourceMimeType,
                                     String targetMimeType) {
        String ext = null;
        if (fileName != null) {
            ext = FilenameUtils.getExtension(fileName);
        }
        return (DocFormats.ODT_EXT.equals(ext)
                || DocFormats.ODT_TYPE.equals(sourceMimeType))
                && DocFormats.PDF_TYPE.equals(targetMimeType);
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
        OpenOfficeDocument doc = new OpenOfficeDocument(document, connection,
                                                        handlers);
        doc.refresh();   // workaround to avoid corruption of generated doc
        // when the source document contains user fields.
        // Alternative approach is to do a Thread.sleep(1000).
        try {
            return doc.export(mimeType, document.getName());
        } finally {
            doc.close();
        }
    }

}
