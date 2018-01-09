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

package org.openvpms.component.model.document;

import org.openvpms.component.model.object.IMObject;

import java.io.InputStream;

/**
 * Models any type of resource that has a mime type, a size, and holds the contents.
 *
 * @author Tim Anderson
 */
public interface Document extends IMObject {

    /**
     * Returns the document content.
     *
     * @return the content
     */
    InputStream getContent();

    /**
     * Sets the document content.
     *
     * @param stream a stream of the content
     */
    void setContent(InputStream stream);

    /**
     * Returns the document size.
     * <p>
     * If the document has been compressed, this represents its size prior to compression.
     *
     * @return the document size
     */
    int getSize();

    /**
     * Sets the document size.
     *
     * @param size the size of the document.
     */
    void setSize(int size);

    /**
     * Returns the mime type.
     *
     * @return the mime type.
     */
    String getMimeType();

    /**
     * Sets the mime type.
     *
     * @param mimeType the mime type.
     */
    void setMimeType(String mimeType);

    /**
     * Returns the document checksum.
     *
     * @return the checksum
     */
    long getChecksum();

    /**
     * Sets the document checksum.
     *
     * @param checksum the checksum
     */
    void setChecksum(long checksum);

}
