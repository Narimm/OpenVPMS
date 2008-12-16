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

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.File;


/**
 * Implementation of the {@link DocumentFactory} interface that creates
 * documents from files.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class FileDocumentFactory implements DocumentFactory {

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Creates a new <tt>FileDocumentFactory</tt>.
     */
    public FileDocumentFactory() {
        handlers = new DocumentHandlers();
    }

    /**
     * Creates a document given a document act.
     * This uses the file name of the document act to load the corresponding
     * file and create a document from it.
     *
     * @param file
     * @param mimeType
     * @return a new document
     * @throws DocumentException for any error
     */
    public Document create(File file, String mimeType) {
        return DocumentHelper.create(file, mimeType, handlers);
    }

}
