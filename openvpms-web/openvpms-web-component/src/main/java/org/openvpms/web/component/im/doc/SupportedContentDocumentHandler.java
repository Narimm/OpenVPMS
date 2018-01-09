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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DefaultDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.jasper.JRXMLDocumentHandler;

import java.io.InputStream;

/**
 * A {@link DocumentHandler} that determines what documents are supported based on a list of file name extensions
 * and mime types passed at construction.
 *
 * @author Tim Anderson
 */
public abstract class SupportedContentDocumentHandler implements DocumentHandler {

    /**
     * The supported file extensions.
     */
    private final String[] supportedExtensions;

    /**
     * The supported mime types.
     */
    private final String[] supportedMimeTypes;

    /**
     * The handler for jrxml documents.
     */
    private final JRXMLDocumentHandler jrxmlHandler;

    /**
     * The handler for non-jrxml documents.
     */
    private final DefaultDocumentHandler handler;

    /**
     * Constructs a {@link SupportedContentDocumentHandler}.
     *
     * @param supportedExtensions the file name extensions supported by the handler
     * @param supportedMimeTypes  the mime types supported by the handler
     * @param service             the archetype service
     */
    public SupportedContentDocumentHandler(String[] supportedExtensions, String[] supportedMimeTypes,
                                           IArchetypeService service) {
        this.supportedExtensions = supportedExtensions;
        this.supportedMimeTypes = supportedMimeTypes;
        handler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT, service);
        jrxmlHandler = new JRXMLDocumentHandler(service);
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
        name = name.toLowerCase();
        return isSupportedExtension(name) || (mimeType != null && isSupportedMimeType(mimeType));
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
        return DocumentArchetypes.DEFAULT_DOCUMENT.equals(shortName) && canHandle(name, mimeType);
    }

    /**
     * Determines if this handler supports a document.
     *
     * @param document the document
     * @return {@code true} if this handler supports the document
     */
    @Override
    public boolean canHandle(Document document) {
        return DocumentArchetypes.DEFAULT_DOCUMENT.equals(document.getArchetypeId().getShortName());
    }

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param name     the document name. Any path information is removed.
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the document. May be {@code null}
     * @param size     the size of stream, or {@code -1} if the size is not known
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     * @throws OpenVPMSException         for any other error
     */
    @Override
    public Document create(String name, InputStream stream, String mimeType, int size) {
        return getHandler(name).create(name, stream, mimeType, size);
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
        getHandler(document.getName()).update(document, stream, mimeType, size);
    }

    /**
     * Returns the document content as a stream.
     *
     * @param document the document
     * @return the document content
     * @throws DocumentException for any error
     */
    @Override
    public InputStream getContent(Document document) {
        return getHandler(document.getName()).getContent(document);
    }

    /**
     * Determines if a file is supported based on its extension.
     *
     * @param name the file name
     * @return {@code true} if the name has a supported extension
     */
    protected boolean isSupportedExtension(String name) {
        for (String ext : supportedExtensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a file is supported based on its mime type.
     *
     * @param mimeType the file mime type
     * @return {@code true} if the mime type is supported
     */
    protected boolean isSupportedMimeType(String mimeType) {
        for (String type : supportedMimeTypes) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the appropriate handler for a file based on the file name.
     *
     * @param name the file name
     * @return the handler for the file
     */
    private DocumentHandler getHandler(String name) {
        return (isJRXML(name)) ? jrxmlHandler : handler;
    }

    /**
     * Determines if a file is a JRXML.
     *
     * @param name the file name
     * @return {@code true} if the file is a JRXML
     */
    private boolean isJRXML(String name) {
        return name.toLowerCase().endsWith(DocFormats.JRXML_EXT);
    }

}
