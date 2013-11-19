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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openvpms.archetype.rules.doc.AbstractDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;


/**
 * Jasper Reports document handler.
 *
 * @author Tim Anderson
 */
public class JRXMLDocumentHandler extends AbstractDocumentHandler {

    /**
     * Constructs a {@code JRXMLDocumentHandler}.
     *
     * @param service the archetype service
     */
    public JRXMLDocumentHandler(IArchetypeService service) {
        super(DocumentArchetypes.DEFAULT_DOCUMENT, service);
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
        return name.endsWith(".jrxml");
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document. May be {@code null}
     * @return {@code true} if this handler supports the document
     */
    @Override
    public boolean canHandle(String name, String shortName, String mimeType) {
        boolean result = super.canHandle(name, shortName, mimeType);
        return result && name.endsWith(".jrxml");
    }

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param name     the document name
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the document. May be {@code null}
     * @param size     the size of stream
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     * @throws JRXMLDocumentException    if the document version cannot be parsed
     */
    public Document create(String name, InputStream stream, String mimeType, int size) {
        Document document;
        JasperDesign design;
        try {
            design = JRXmlLoader.load(stream);
        } catch (JRException exception) {
            if (ExceptionUtils.getRootCause(exception) instanceof SAXParseException) {
                if (name == null) {
                    name = "file";
                }
                throw new JRXMLDocumentException(exception, JRXMLDocumentException.ErrorCode.ReadError, name);
            } else {
                throw new DocumentException(DocumentException.ErrorCode.ReadError, exception, name);
            }
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DeflaterOutputStream output = new DeflaterOutputStream(bytes);
            JRXmlWriter.writeReport(design, output, "UTF-8");
            output.close();
            document = create(name, bytes.toByteArray(), "text/xml", size);
        } catch (Exception exception) {
            throw new DocumentException(DocumentException.ErrorCode.WriteError, exception, name);
        }
        return document;
    }

}
