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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentDO;


/**
 * Document specific-act
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-04-26 17:47:12 +1000 (Thu, 26 Apr 2007) $
 */
public class DocumentActDOImpl extends ActDOImpl implements DocumentActDO {

    /**
     * The version of the document
     */
    private String docVersion;

    /**
     * The filename of the document
     */
    private String fileName;

    /**
     * The mimetype of the document
     */
    private String mimeType;

    /**
     * Indicates whether it has been printed
     */
    private boolean printed;

    /**
     * The document.
     */
    private DocumentDO document;

    /**
     * Default constructor
     */
    public DocumentActDOImpl() {
        super();
    }

    /**
     * @return Returns the document.
     */
    public DocumentDO getDocument() {
        return document;
    }

    /**
     * @param document The document reference to set.
     */
    public void setDocument(DocumentDO document) {
        this.document = document;
    }

    /**
     * @return Returns the version.
     */
    public String getDocVersion() {
        return docVersion;
    }

    /**
     * @param version The version to set.
     */
    public void setDocVersion(String version) {
        docVersion = version;
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    /**
     * @return Returns the printed.
     */
    public boolean isPrinted() {
        return printed;
    }

    /**
     * @param printed The printed to set.
     */
    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.act.Act#toString()
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("docVersion", docVersion)
                .append("docReference", document)
                .toString();
    }

}
