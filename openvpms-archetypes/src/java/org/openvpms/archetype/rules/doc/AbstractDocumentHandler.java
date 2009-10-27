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

package org.openvpms.archetype.rules.doc;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.ReadError;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;


/**
 * Generic document handler.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractDocumentHandler implements DocumentHandler {

    /**
     * The document archetype short name.
     */
    private final String shortName;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <code>AbstractDocumentHandler</code>
     *
     * @param shortName the document archetype short name
     */
    public AbstractDocumentHandler(String shortName) {
        this(shortName, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <code>AbstractDocumentHandler</code>.
     *
     * @param shortName the document archetype short name
     * @param service   the archetype service
     */
    public AbstractDocumentHandler(String shortName,
                                   IArchetypeService service) {
        this.shortName = shortName;
        this.service = service;
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param name     the document name
     * @param mimeType the mime type of the document. May be <code>null</code>
     * @return <code>true</code> if this handler supports the document
     */
    public boolean canHandle(String name, String mimeType) {
        return true;
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document. May be <code>null</code>
     * @return <code>true</code> if this handler supports the document
     */
    public boolean canHandle(String name, String shortName, String mimeType) {
        return this.shortName.equals(shortName);
    }

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param name     the document name. Any path information is removed.
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the content. May be <code>null</code>
     * @param size     the size of stream, or <tt>-1</tt> if the size is not
     *                 known
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document create(String name, InputStream stream, String mimeType,
                           int size) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DeflaterOutputStream output = new DeflaterOutputStream(bytes);
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
        byte[] data = bytes.toByteArray();
        return create(name, data, mimeType, size, checksum.getValue());
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param document the document
     * @return <code>true</code> if this handler supports the document
     * @throws DocumentException for any error
     */
    public boolean canHandle(Document document) {
        return shortName.equals(document.getArchetypeId().getShortName());
    }

    /**
     * Returns the document content as a stream.
     *
     * @param document the document
     * @return the document content
     * @throws DocumentException for any error
     */
    public InputStream getContent(Document document) {
        ByteArrayInputStream bytes
                = new ByteArrayInputStream(document.getContents());
        return new InflaterInputStream(bytes);
    }

    /**
     * Creates a new {@link Document}.
     *
     * @param name     the document name. Any path information is removed.
     * @param content  the serialized content
     * @param mimeType the mime type of the content. May be <code>null</code>
     * @param size     the uncompressed document size
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document create(String name, byte[] content, String mimeType, int size) {
        return create(name, content, mimeType, size, calculateChecksum(content));
    }

    /**
     * Creates a new {@link Document}.
     *
     * @param name     the document name. Any path information is removed.
     * @param content  the serialized content
     * @param mimeType the mime type of the content. May be <code>null</code>
     * @param size     the uncompressed document size
     * @param checksum the uncompressed document CRC32 checksum
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document create(String name, byte[] content, String mimeType,
                           int size, long checksum) {
        if (name != null) {
            // strip path information
            File file = new File(name);
            name = file.getName();
        }
        Document document = (Document) service.create(shortName);
        document.setName(name);
        document.setMimeType(mimeType);
        document.setContents(content);
        document.setDocSize(size);
        document.setChecksum(checksum);
        return document;
    }

    /**
     * Calculates a CRC32 checksum for a document.
     *
     * @param content the document content
     * @return the CRC32 checksum of the document
     */
    protected long calculateChecksum(byte[] content) {
        CRC32 checksum = new CRC32();
        checksum.update(content);
        return checksum.getValue();
    }
}
