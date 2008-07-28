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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface DocumentDO extends IMObjectDO {
    /**
     * @return Returns the checksum.
     */
    long getChecksum();

    /**
     * @param checksum The checksum to set.
     */
    void setChecksum(long checksum);

    /**
     * @return Returns the contents.
     */
    byte[] getContents();

    /**
     * @param contents The contents to set.
     */
    void setContents(byte[] contents);

    /**
     * @return Returns the docSize.
     */
    int getDocSize();

    /**
     * @param docSize The docSize to set.
     */
    void setDocSize(int docSize);

    /**
     * @return Returns the mimeType.
     */
    String getMimeType();

    /**
     * @param mimeType The mimeType to set.
     */
    void setMimeType(String mimeType);
}
