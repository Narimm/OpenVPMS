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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.dispatcher;

import org.oasis.ubl.common.aggregate.DocumentReferenceType;

/**
 * Encapsulates a document obtained from the <tt>InboxService</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class Document {

    /**
     * The document reference.
     */
    private final DocumentReferenceType reference;

    /**
     * The document content.
     */
    private final Object content;

    /**
     * Constructs a document.
     *
     * @param reference a reference to the document
     * @param content   the document content
     */
    public Document(DocumentReferenceType reference, Object content) {
        this.reference = reference;
        this.content = content;
    }

    /**
     * Returns the document reference.
     *
     * @return the document reference
     */
    public DocumentReferenceType getDocumentReference() {
        return reference;
    }

    /**
     * Returns the document content.
     *
     * @return the document content
     */
    public Object getContent() {
        return content;
    }
}