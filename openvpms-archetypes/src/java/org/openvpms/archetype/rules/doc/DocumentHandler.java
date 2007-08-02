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

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.io.InputStream;


/**
 * Document handler.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see DocumentHandlers
 */
public interface DocumentHandler {

    /**
     * Determines if this handler supports a document.
     *
     * @param name     the document name
     * @param mimeType the mime type of the document. May be <code>null</code>
     * @return <code>true</code> if this handler supports the document
     */
    boolean canHandle(String name, String mimeType);

    /**
     * Determines if this handler supports a document.
     *
     * @param name      the document name
     * @param shortName the document archetype short name
     * @param mimeType  the mime type of the document. May be <code>null</code>
     * @return <code>true</code> if this handler supports the document
     */
    boolean canHandle(String name, String shortName, String mimeType);

    /**
     * Determines if this handler supports a document.
     *
     * @param document the document
     * @return <code>true</code> if this handler supports the document
     */
    boolean canHandle(Document document);

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param name     the document name. Any path information is removed.
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the document. May be <code>null</code>
     * @param size     the size of stream, or <tt>-1</tt> if the size is not
     *                 known
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    Document create(String name, InputStream stream, String mimeType, int size);

    /**
     * Returns the document content as a stream.
     *
     * @param document the document
     * @return the document content
     * @throws DocumentException for any error
     */
    InputStream getContent(Document document);

}
