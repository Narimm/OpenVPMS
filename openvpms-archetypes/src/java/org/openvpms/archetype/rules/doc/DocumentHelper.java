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

package org.openvpms.archetype.rules.doc;

import org.apache.commons.io.IOUtils;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Document helper.
 *
 * @author Tim Anderson
 */
public class DocumentHelper {

    /**
     * Creates a new document from a file.
     *
     * @param file     the file
     * @param mimeType the mime type. May be {@code null}
     * @param factory  the document handler factory
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     * @throws OpenVPMSException         for any other error
     */
    public static Document create(File file, String mimeType, DocumentHandlers factory) {
        DocumentHandler handler = factory.get(file.getName(), mimeType);
        return create(file, handler, mimeType);
    }

    /**
     * Creates a new document from a file.
     *
     * @param file      the file
     * @param shortName the document archetype short name
     * @param mimeType  the mime type. May be {@code null}
     * @param factory   the document handler factory
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     * @throws OpenVPMSException         for any other error
     */
    public static Document create(File file, String shortName, String mimeType, DocumentHandlers factory) {
        DocumentHandler handler = factory.get(file.getName(), shortName, mimeType);
        return create(file, handler, mimeType);

    }

    /**
     * Creates a new document from a file.
     *
     * @param file     the file
     * @param handler  the document handler
     * @param mimeType the mime type. May be {@code null}
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     * @throws OpenVPMSException         for any other error
     */
    private static Document create(File file, DocumentHandler handler, String mimeType) {
        if (handler == null) {
            throw new DocumentException(DocumentException.ErrorCode.UnsupportedDoc, mimeType);
        }
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            int length = (int) file.length();
            return handler.create(file.getName(), stream, mimeType, length);
        } catch (IOException exception) {
            throw new DocumentException(DocumentException.ErrorCode.ReadError, exception);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
