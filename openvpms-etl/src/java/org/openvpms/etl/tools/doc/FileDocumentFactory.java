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
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.ReadError;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Implementation of the {@link DocumentFactory} interface that creates
 * documents from files.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FileDocumentFactory implements DocumentFactory {

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The parent directory.
     */
    private File parent;


    /**
     * Creates a new <tt>FileDocumentFactory</tt> for the current directory.
     */
    public FileDocumentFactory() {
        this(null);
    }

    /**
     * Creates a new <tt>FileDocumentFactory</tt> for the the specified
     * directory.
     *
     * @param dir the directory
     */
    public FileDocumentFactory(String dir) {
        parent = (dir == null) ? new File("./") : new File(dir);
        handlers = new DocumentHandlers();
    }

    /**
     * Creates a document given a document act.
     * This uses the file name of the document act to load the corresponding
     * file and create a document from it.
     *
     * @param act the document act
     * @return a new document
     * @throws DocumentException for any error
     */
    public Document create(DocumentAct act) {
        Document doc;
        String fileName = act.getFileName();
        String mimeType = act.getMimeType();
        File file = new File(parent, fileName);
        try {
            DocumentHandler handler = handlers.get(fileName, "document.other");
            FileInputStream stream = new FileInputStream(file);
            int size = (int) file.length();
            doc = handler.create(fileName, stream, mimeType, size);
            stream.close();
        } catch (IOException exception) {
            throw new DocumentException(ReadError, file.getPath(), exception);
        }
        return doc;
    }

}
