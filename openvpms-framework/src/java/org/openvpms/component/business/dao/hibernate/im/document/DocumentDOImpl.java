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
 * Models any type of resource, which has a mime type, a size and holds the
 * contents
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-04-26 17:47:12 +1000 (Thu, 26 Apr 2007) $
 */
public class DocumentDOImpl extends IMObjectDOImpl implements DocumentDO {

    /**
     * The mime type
     */
    private String mimeType;

    /**
     * The size of the document
     */
    private int docSize;

    /**
     * The checksum of the contents
     */
    private long checksum;

    /**
     * The contents
     */
    private byte[] contents;


    /**
     * default constructor
     */
    public DocumentDOImpl() {
    }

    /**
     * @return Returns the checksum.
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * @param checksum The checksum to set.
     */
    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    /**
     * @return Returns the contents.
     */
    public byte[] getContents() {
        return contents;
    }

    /**
     * @param contents The contents to set.
     */
    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    /**
     * @return Returns the docSize.
     */
    public int getDocSize() {
        return docSize;
    }

    /**
     * @param docSize The docSize to set.
     */
    public void setDocSize(int docSize) {
        this.docSize = docSize;
    }

    /**
     * @return Returns the mimeType.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType The mimeType to set.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("mimeType", mimeType)
                .append("docSize", docSize)
                .append("checksum", checksum)
                .toString();
    }
}
