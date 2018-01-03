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
 * .
 *
 * @author Tim Anderson
 */
public interface DocumentAct extends Act {

    /**
     * Returns the document reference.
     *
     * @return the document reference, or <tt>null</tt> if none is set
     */
    Reference getDocument();

    /**
     * Sets the document reference.
     *
     * @param reference the document reference. May be <tt>null</tt>
     */
    void setDocument(Reference reference);

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
