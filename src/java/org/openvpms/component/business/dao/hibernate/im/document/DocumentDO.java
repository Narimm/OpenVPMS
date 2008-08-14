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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.im.document.Document;


/**
 * Data object interface corresponding to the {@link Document} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface DocumentDO extends IMObjectDO {

    /**
     * Returns the document contents.
     *
     * @return the document contents
     */
    byte[] getContents();

    /**
     * Sets the document contents.
     *
     * @param contents the contents
     */
    void setContents(byte[] contents);

    /**
     * Returns the document size.
     *
     * @return the document size
     */
    int getDocSize();

    /**
     * Sets the document size.
     *
     * @param size the document size
     */
    void setDocSize(int size);

    /**
     * Returns the document mime type.
     *
     * @return the mime type
     */
    String getMimeType();

    /**
     * Sets the document mime type.
     *
     * @param mimeType the mime type
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
