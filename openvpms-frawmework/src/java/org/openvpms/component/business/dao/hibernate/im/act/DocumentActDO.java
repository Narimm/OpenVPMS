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
import org.openvpms.component.business.domain.im.act.DocumentAct;


/**
 * Data object interface corresponding to the {@link DocumentAct} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface DocumentActDO extends ActDO {

    /**
     * Returns the document.
     *
     * @return the document. May be <tt>null</tt>
     */
    DocumentDO getDocument();

    /**
     * Sets the document.
     *
     * @param document the document. May be <tt>null</tt>
     */
    void setDocument(DocumentDO document);

    /**
     * Returns the document version.
     *
     * @return the document version. May be <tt>null</tt>
     */
    String getDocVersion();

    /**
     * Sets the document version.
     *
     * @param version the version to set. May be <tt>null</tt>
     */
    void setDocVersion(String version);

    /**
     * Returns the document file name.
     *
     * @return the file name. May be <tt>null</tt>
     */
    String getFileName();

    /**
     * Sets the document file name.
     *
     * @param fileName the file name. May be <tt>null</tt>
     */
    void setFileName(String fileName);

    /**
     * Returns the document mime type.
     *
     * @return the mime type. May be <tt>null</tt>
     */
    String getMimeType();

    /**
     * Sets the document mime type.
     *
     * @param mimeType the mime type. May be <tt>null</tt>
     */
    void setMimeType(String mimeType);

    /**
     * Determines if the document has been printed.
     *
     * @return <tt>true</tt> if the document has been printed, otherwise
     *         <tt>false</tt>
     */
    boolean isPrinted();

    /**
     * Determines if the document has been printed.
     *
     * @param printed if <tt>true</tt>, the document has been printed
     */
    void setPrinted(boolean printed);
}
