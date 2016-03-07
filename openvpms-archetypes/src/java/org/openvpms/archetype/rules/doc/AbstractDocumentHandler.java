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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.doc;

import org.apache.commons.lang.mutable.MutableInt;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.ReadError;


/**
 * Generic document handler.
 *
 * @author Tim Anderson
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
     * Determines if documents should be compressed.
     */
    private final boolean compress;

    /**
     * Constructs an {@link AbstractDocumentHandler}.
     *
     * @param shortName the document archetype short name
     * @param service   the archetype service
     */
    public AbstractDocumentHandler(String shortName, IArchetypeService service) {
        this(shortName, service, true);
    }

    /**
     * Constructs an {@link AbstractDocumentHandler}.
     *
     * @param shortName the document archetype short name
     * @param service   the archetype service
     * @param compress  if {@code true} compress documents
     */
    public AbstractDocumentHandler(String shortName, IArchetypeService service, boolean compress) {
        this.shortName = shortName;
        this.service = service;
        this.compress = compress;
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param name     the document name
     * @param mimeType the mime type of the document. May be {@code null}
     * @return {@code true} if this handler supports the document
     */
    public boolean canHandle(String name, String mimeType) {
        return true;
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document. May be {@code null}
     * @return {@code true} if this handler supports the document
     */
    public boolean canHandle(String name, String shortName, String mimeType) {
        return this.shortName.equals(shortName) && canHandle(name, mimeType);
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
    public Document create(String name, InputStream stream, String mimeType, int size) {
        CRC32 checksum = new CRC32();
        MutableInt actualSize = new MutableInt(size);
        byte[] data = getContent(name, stream, actualSize, checksum);
        return create(name, data, mimeType, actualSize.intValue(), checksum.getValue());
    }

    /**
     * Updates a {@link Document} from a stream.
     *
     * @param document the document to update
     * @param stream   a stream representing the new document content
     * @param mimeType the mime type of the document. May be {@code null}
     * @param size     the size of stream, or {@code -1} if the size is not known
     */
    @Override
    public void update(Document document, InputStream stream, String mimeType, int size) {
        CRC32 checksum = new CRC32();
        MutableInt actualSize = new MutableInt(size);
        byte[] content = getContent(document.getName(), stream, actualSize, checksum);
        if (mimeType == null) {
            mimeType = document.getMimeType();
        }
        update(document, document.getName(), content, mimeType, actualSize.intValue(), checksum.getValue());
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param document the document
     * @return {@code true} if this handler supports the document
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
        ByteArrayInputStream bytes = new ByteArrayInputStream(document.getContents());
        return getInputStream(bytes);
    }

    /**
     * Creates a new {@link Document}.
     *
     * @param name     the document name. Any path information is removed.
     * @param content  the serialized content
     * @param mimeType the mime type of the content. May be {@code null}
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
     * @param mimeType the mime type of the content. May be {@code null}
     * @param size     the uncompressed document size
     * @param checksum the uncompressed document CRC32 checksum
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document create(String name, byte[] content, String mimeType, int size, long checksum) {
        Document document = (Document) service.create(shortName);
        return update(document, name, content, mimeType, size, checksum);
    }

    /**
     * Updates a document.
     *
     * @param name     the document name. Any path information is removed.
     * @param content  the serialized content
     * @param mimeType the mime type of the content. May be {@code null}
     * @param size     the uncompressed document size
     * @param checksum the uncompressed document CRC32 checksum
     * @return the document
     */
    protected Document update(Document document, String name, byte[] content, String mimeType, int size,
                              long checksum) {
        if (name != null) {
            // strip path information
            File file = new File(name);
            name = file.getName();
        }
        document.setName(name);
        document.setMimeType(mimeType);
        document.setContents(content);
        document.setDocSize(size);
        document.setChecksum(checksum);
        return document;
    }

    /**
     * Returns a stream to write the document to.
     * <p/>
     * If compression is enabled, this returns an {@link DeflaterOutputStream}, otherwise it returns the stream
     * unchanged.
     *
     * @param stream the raw stream
     * @return a stream to write the document to
     */
    protected OutputStream getOutputStream(OutputStream stream) {
        return compress ? new DeflaterOutputStream(stream) : stream;
    }

    /**
     * Returns a stream to read the document from.
     * <p/>
     * If compression is enabled, this returns an {@link InflaterInputStream}, otherwise it returns the stream
     * unchanged.
     *
     * @param stream the raw stream
     * @return a stream to read the document from
     */
    protected InputStream getInputStream(ByteArrayInputStream stream) {
        return compress ? new InflaterInputStream(stream) : stream;
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

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Outputs the content of a stream into a byte array, using the stream returned by
     * {@link #getOutputStream(OutputStream)}.
     *
     * @param name     the document name, for error reporting
     * @param stream   a stream representing the document content
     * @param size     the size of stream, or {@code -1} if the size is not known. This will be updated with the actual
     *                 size on return
     * @param checksum updated with the uncompressed document CRC32 checksum
     * @return the content
     */
    private byte[] getContent(String name, InputStream stream, MutableInt size, CRC32 checksum) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        OutputStream output = getOutputStream(bytes);
        int read = 0;
        int initialSize = size.intValue();
        int length;
        try {
            while ((length = stream.read(buffer)) != -1) {
                checksum.update(buffer, 0, length);
                output.write(buffer, 0, length);
                read += length;
            }
            if (initialSize != -1 && read != initialSize) {
                throw new DocumentException(ReadError, name);
            }
            output.close();
        } catch (IOException exception) {
            throw new DocumentException(ReadError, exception, name);
        }
        size.setValue(read);
        return bytes.toByteArray();
    }

}
