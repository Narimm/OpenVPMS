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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.web.component.im.doc.DocumentException.ErrorCode.ReadError;
import static org.openvpms.web.component.im.doc.DocumentException.ErrorCode.WriteError;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


/**
 * Jasper Reports document handler.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class JRXMLDocumentHandler implements DocumentHandler {

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
        JasperDesign design;
        try {
            design = JRXmlLoader.load(stream);
        } catch (JRException exception) {
            throw new DocumentException(ReadError, fileName, exception);
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            JRXmlWriter.writeReport(design, bytes, "UTF-8");

            document = (Document) service.create("document.jrxml");
            document.setName(fileName);
            byte[] data = bytes.toByteArray();
            document.setDocSize(data.length);
            document.setContents(data);
        } catch (JRException exception) {
            throw new DocumentException(WriteError, fileName, exception);
        }
        return document;
    }
}
