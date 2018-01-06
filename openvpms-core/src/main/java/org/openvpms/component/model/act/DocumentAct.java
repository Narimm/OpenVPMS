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

package org.openvpms.component.model.act;

import org.openvpms.component.model.object.Reference;


/**
 * A document-specific activity.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface DocumentAct extends Act {

    /**
     * Returns the document reference.
     *
     * @return the document reference. May be {@code null}
     */
    Reference getDocument();

    /**
     * Sets the document reference.
     *
     * @param reference the document reference. May be {@code null}
     */
    void setDocument(Reference reference);

    /**
     * Returns the document file name.
     *
     * @return the file name. May be {@code null}
     */
    String getFileName();

    /**
     * Sets the document file name.
     *
     * @param fileName the file name. May be {@code null}
     */
    void setFileName(String fileName);

    /**
     * Returns the mime type of the document.
     *
     * @return the mime type. May be {@code null}
     */
    String getMimeType();

    /**
     * Sets the mime type of the document.
     *
     * @param mimeType the mime type. May be {@code null}
     */
    void setMimeType(String mimeType);

    /**
     * Determines if the document has been printed.
     *
     * @return {@code true} if the document has been printed
     */
    boolean isPrinted();

    /**
     * Determines if the document has been printed.
     *
     * @param printed ifi {@code true}, indicates the document has been printed
     */
    void setPrinted(boolean printed);

}
