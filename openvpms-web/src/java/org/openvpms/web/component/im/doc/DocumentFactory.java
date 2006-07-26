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

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import static org.openvpms.web.component.im.doc.DocumentException.ErrorCode.UnsupportedDoc;

import java.io.InputStream;


/**
 * Factory for {@link Document}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentFactory {

    /**
     * Creates a new {@link Document} for the specified file name and content
     * stream.
     *
     * @param fileName the document file name
     * @param stream   a stream representing the document content
     * @return a new document
     * @throws DocumentException         for any document exception
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Document create(String fileName, InputStream stream, String contentType, Integer size) {
        DocumentHandler handler = null;
        if (fileName.endsWith(".jrxml")) {
            handler = new JRXMLDocumentHandler();
        }
        else
            handler = new GenericDocumentHandler();
        if (handler == null) {
            throw new DocumentException(UnsupportedDoc, fileName);
        }
        return handler.getDocument(fileName, stream, contentType, size);
    }

}
