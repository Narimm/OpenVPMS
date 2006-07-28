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

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.web.component.im.doc.DocumentException.ErrorCode.ReadError;

import java.io.InputStream;

/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public class GenericDocumentHandler implements DocumentHandler {

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param fileName the file name
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the content
     * @param size     the size of stream
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocument(String fileName, InputStream stream,
                                String mimeType, int size) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Document document;
        try {
            document = (Document) service.create("document.other");
            document.setName(fileName);
            document.setMimeType(mimeType);
            byte[] data = new byte[size];
            if (stream.read(data) != size) {
                throw new DocumentException(ReadError, fileName);
            }
            document.setDocSize(data.length);
            document.setContents(data);
        } catch (Exception exception) {
            throw new DocumentException(ReadError, fileName, exception);
        }
        return document;
    }
}
