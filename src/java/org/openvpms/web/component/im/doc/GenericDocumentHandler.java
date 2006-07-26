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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import static org.openvpms.web.component.im.doc.DocumentException.ErrorCode.WriteError;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class GenericDocumentHandler implements DocumentHandler {

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param fileName the file name
     * @param stream   a stream representing the document content
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocument(String fileName, InputStream stream, String contentType, Integer size) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Document document;
        try {
            document = (Document) service.create("document.other");
            document.setName(fileName);
            document.setMimeType(contentType);
            byte[] data = new byte[size];
            stream.read(data);
            document.setDocSize(data.length);
            document.setContents(data);
        } catch (Exception exception) {
            throw new DocumentException(WriteError, fileName, exception);
        }
        return document;
    }
}
