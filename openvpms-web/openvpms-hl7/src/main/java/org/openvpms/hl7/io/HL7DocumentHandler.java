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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.io;

import org.openvpms.archetype.rules.doc.AbstractDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.ReadError;

/**
 * Handler for HL7 message documents.
 *
 * @author Tim Anderson
 */
public class HL7DocumentHandler extends AbstractDocumentHandler {

    /**
     * The charset to use.
     */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Constructs an {@link HL7DocumentHandler}.
     *
     * @param service the archetype service
     */
    public HL7DocumentHandler(IArchetypeService service) {
        super("document.HL7", service);
    }

    /**
     * Creates a document from an encoded HL7 message.
     *
     * @param name     the document
     * @param message  the encoded HL7 message
     * @param mimeType the HL7 mime type
     * @return a new document
     */
    public Document create(String name, String message, String mimeType) {
        byte[] bytes = message.getBytes(CHARSET);
        return create(name, bytes, mimeType, bytes.length);
    }

    /**
     * Converts a document to the encoded HL7 message string.
     *
     * @param document the document
     * @return the encoded message
     */
    public String getStringContent(Document document) {
        byte[] bytes = document.getContents();
        return new String(bytes, CHARSET);
    }

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param name     the document name. Any path information is removed.
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the content. May be {@code null}
     * @param size     the size of stream, or {@code -1} if the size is not known
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document create(String name, InputStream stream, String mimeType, int size) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int read = 0;
        int length;
        CRC32 checksum = new CRC32();
        try {
            while ((length = stream.read(buffer)) != -1) {
                checksum.update(buffer, 0, length);
                output.write(buffer, 0, length);
                read += length;
            }
            if (size != -1 && read != size) {
                throw new DocumentException(ReadError, name);
            }
            output.close();
        } catch (IOException exception) {
            throw new DocumentException(ReadError, exception, name);
        }
        byte[] data = output.toByteArray();
        return create(name, data, mimeType, size, checksum.getValue());
    }

    /**
     * Returns the document content as a stream.
     *
     * @param document the document
     * @return the document content
     */
    @Override
    public InputStream getContent(Document document) {
        return new ByteArrayInputStream(document.getContents());
    }
}
