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

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.document.DocumentDO;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface DocumentActDO extends ActDO {

    /**
     * @return Returns the document.
     */
    DocumentDO getDocument();

    /**
     * @param document The document reference to set.
     */
    void setDocument(DocumentDO document);

    /**
     * @return Returns the version.
     */
    String getDocVersion();

    /**
     * @param version The version to set.
     */
    void setDocVersion(String version);

    /**
     * @return Returns the fileName.
     */
    String getFileName();

    /**
     * @param fileName The fileName to set.
     */
    void setFileName(String fileName);

    /**
     * @return Returns the mimeType.
     */
    String getMimeType();

    /**
     * @param mimeType The mimeType to set.
     */
    void setMimeType(String mimeType);

    /**
     * @return Returns the printed.
     */
    boolean isPrinted();

    /**
     * @param printed The printed to set.
     */
    void setPrinted(boolean printed);
}
