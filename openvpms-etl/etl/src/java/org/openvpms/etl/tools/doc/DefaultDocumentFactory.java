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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.tools.doc;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.File;
import java.util.Collection;


/**
 * Default implementation of the {@link DocumentFactory} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DefaultDocumentFactory implements DocumentFactory {

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    static {
        MimeUtil.registerMimeDetector(ExtensionMimeDetector.class.getName());
    }

    /**
     * Creates a new <tt>DefaultDocumentFactory</tt>.
     */
    public DefaultDocumentFactory() {
        handlers = new DocumentHandlers();
    }

    /**
     * Creates a document from the supplied file.
     *
     * @param file the file
     * @return a new document
     * @throws DocumentException for any error
     */
    public Document create(File file) {
        String mimeType = getMimeType(file);
        return DocumentHelper.create(file, mimeType, handlers);
    }

    /**
     * Returns the mime type for a file.
     *
     * @param file the file
     * @return the mime type of the file
     * @throws DocumentException if the mime type cannot be determined
     */
    protected String getMimeType(File file) {
        try {
            Collection types = MimeUtil.getMimeTypes(file);
            MimeType type = MimeUtil.getMostSpecificMimeType(types);
            return type.toString();
        } catch (Exception exception) {
            throw new DocumentException(DocumentException.ErrorCode.ReadError, exception, file.getName());
        }
    }

}
