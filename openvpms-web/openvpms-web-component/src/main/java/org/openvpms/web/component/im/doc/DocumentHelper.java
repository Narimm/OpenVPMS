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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.openvpms.web.system.ServiceHelper;

/**
 * Document helper methods.
 *
 * @author Tim Anderson
 */
public class DocumentHelper {

    /**
     * Converts a document to the specified mime type.
     *
     * @param document the document
     * @param mimeType the mime type to convert to
     */
    public static Document convert(Document document, String mimeType) {
        OOConnection connection = null;
        try {
            DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
            connection = OpenOfficeHelper.getConnectionPool().getConnection();
            Converter converter = new Converter(connection, handlers);
            document = converter.convert(document, mimeType);
        } finally {
            OpenOfficeHelper.close(connection);
        }
        return document;
    }

}
