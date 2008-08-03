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
 * Implementation of the {@link DocumentActDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-04-26 17:47:12 +1000 (Thu, 26 Apr 2007) $
 */
public class DocumentActDOImpl extends ActDOImpl implements DocumentActDO {

    /**
     * The document.
     */
    private DocumentDO document;

    /**
     * The document version.
     */
    private String docVersion;

    /**
     * The document file name.
     */
    private String fileName;

    /**
     * The document mime type.
     */
    private String mimeType;

    /**
     * Indicates if the document has been printed.
     */
    private boolean printed;


    /**
     * Default constructor.
     */
    public DocumentActDOImpl() {
    }

    /**
     * Returns the document.
     *
     * @return the document. May be <tt>null</tt>
     */
    public DocumentDO getDocument() {
        return document;
    }

    /**
     * Sets the document.
     *
     * @param document the document. May be <tt>null</tt>
     */
    public void setDocument(DocumentDO document) {
        this.document = document;
    }

    /**
     * Returns the document version.
     *
     * @return the document version. May be <tt>null</tt>
     */
    public String getDocVersion() {
        return docVersion;
    }

    /**
     * Sets the document version.
     *
     * @param version the version to set. May be <tt>null</tt>
     */
    public void setDocVersion(String version) {
        docVersion = version;
    }

    /**
     * Returns the document file name.
     *
     * @return the file name. May be <tt>null</tt>
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the document file name.
     *
     * @param fileName the file name. May be <tt>null</tt>
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the document mime type.
     *
     * @return the mime type. May be <tt>null</tt>
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the document mime type.
     *
     * @param mimeType the mime type. May be <tt>null</tt>
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Determines if the document has been printed.
     *
     * @return <tt>true</tt> if the document has been printed, otherwise
     *         <tt>false</tt>
     */
    public boolean isPrinted() {
        return printed;
    }

    /**
     * Determines if the document has been printed.
     *
     * @param printed if <tt>true</tt>, the document has been printed
     */
    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("document", document)
                .toString();
    }

}
