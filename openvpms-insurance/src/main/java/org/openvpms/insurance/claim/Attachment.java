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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.claim;

import java.io.InputStream;

/**
 * Claim attachment.
 *
 * @author Tim Anderson
 */
public interface Attachment {

    /**
     * Returns the attachment file name.
     *
     * @return the attachment file name
     */
    String getFileName();

    /**
     * Returns the attachment mime type
     *
     * @return the mime type
     */
    String getMimeType();

    /**
     * Returns the attachment size.
     *
     * @return the attachment size, in bytes
     */
    long getSize();

    /**
     * Returns the attachment contents.
     *
     * @return the attachment contents
     */
    InputStream getContent();
}
