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


package org.openvpms.component.business.domain.im.act;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.model.object.Reference;

/**
 * Document specific-act
 *
 * @author Jim Alateras
 */
public class DocumentAct extends Act implements org.openvpms.component.model.act.DocumentAct {
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
     * A reference to the actual {@link Document}
     */
    private IMObjectReference docReference;

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public DocumentAct() {
        super();
    }

    /**
     * Returns the document reference.
     *
     * @return the document reference, or <tt>null</tt> if none is set
     */
    public IMObjectReference getDocument() {
        return docReference;
    }

    /**
     * Sets the document reference.
     *
     * @param reference the document reference. May be <tt>null</tt>
     */
    public void setDocument(IMObjectReference reference) {
        this.docReference = reference;
    }

    /**
     * Sets the document reference.
     *
     * @param reference the document reference. May be <tt>null</tt>
     */
    public void setDocument(Reference reference) {
        setDocument((IMObjectReference) reference);
    }

    /**
     * @return Returns the document.
     * @deprecated use {@link #getDocument()}
     */
    @Deprecated
    public Reference getDocReference() {
        return getDocument();
    }

    /**
     * @param reference The document reference to set.
     * @deprecated use {@link #setDocument(IMObjectReference)}
     */
    @Deprecated
    public void setDocReference(IMObjectReference reference) {
        setDocument(reference);
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
        this.docVersion = version;
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
     * @see org.openvpms.component.business.domain.im.act.Act#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        DocumentAct copy = (DocumentAct) super.clone();

        copy.docVersion = this.docVersion;
        copy.docReference = (IMObjectReference) this.docReference.clone();

        return copy;
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
                .append("docReference", docReference)
                .toString();
    }


}
