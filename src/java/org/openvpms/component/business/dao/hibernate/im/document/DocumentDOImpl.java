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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.document;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;


/**
 * Implementation of the {@link DocumentDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-04-26 17:47:12 +1000 (Thu, 26 Apr 2007) $
 */
public class DocumentDOImpl extends IMObjectDOImpl implements DocumentDO {

    /**
     * The contents
     */
    private byte[] contents;

    /**
     * The size of the document.
     */
    private int docSize;

    /**
     * The mime type
     */
    private String mimeType;

    /**
     * The checksum of the contents.
     */
    private long checksum;


    /**
     * Default constructor.
     */
    public DocumentDOImpl() {
    }

    /**
     * Returns the document contents.
     *
     * @return the document contents
     */
    public byte[] getContents() {
        return contents;
    }

    /**
     * Sets the document contents.
     *
     * @param contents the contents
     */
    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    /**
     * Returns the document size.
     *
     * @return the document size
     */
    public int getDocSize() {
        return docSize;
    }

    /**
     * Sets the document size.
     *
     * @param size the document size
     */
    public void setDocSize(int size) {
        this.docSize = size;
    }

    /**
     * Returns the document mime type.
     *
     * @return the mime type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the document mime type.
     *
     * @param mimeType the mime type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the document checksum.
     *
     * @return the checksum
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * Sets the document checksum.
     *
     * @param checksum the checksum
     */
    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("mimeType", mimeType)
                .append("docSize", docSize)
                .append("checksum", checksum)
                .toString();
    }
}
