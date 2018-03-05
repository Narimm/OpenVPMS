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

import org.openvpms.insurance.exception.InsuranceException;

import java.io.InputStream;
import java.util.Date;

/**
 * Claim attachment.
 *
 * @author Tim Anderson
 */
public interface Attachment {

    enum Status {
        PENDING,
        ERROR,
        POSTED,
        SUBMITTED,
        ACCEPTED;

        public boolean isA(String status) {
            return name().equals(status);
        }
    }

    enum Type {
        INVOICE,
        CLINICAL_HISTORY,
        DOCUMENT
    }

    /**
     * Returns the OpenVPMS identifier for this attachment.
     *
     * @return the identifier
     */
    long getId();

    /**
     * Returns the attachment identifier, issued by the insurer.
     *
     * @return the attachment identifier, or {@code null} if none has been issued
     */
    String getInsurerId();

    /**
     * Sets the attachment identifier, issued by the insurer.
     * <p>
     * An attachment can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceAttachment</em> prefix.
     * @param id        the claim identifier
     */
    void setInsurerId(String archetype, String id);

    /**
     * Returns the date when the attachment was created.
     *
     * @return the date
     */
    Date getDate();

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
     * Determines if the attachment has content.
     *
     * @return {@code true} if the attachment has content
     */
    boolean hasContent();

    /**
     * Returns the attachment contents.
     *
     * @return the attachment contents
     * @throws InsuranceException if the content cannot be retrieved
     */
    InputStream getContent();

    /**
     * Returns the attachment status.
     *
     * @return the attachment status
     */
    Status getStatus();

    /**
     * Sets the attachment status.
     *
     * @param status the attachment status
     */
    void setStatus(Status status);

    /**
     * Returns the type of the attachment.
     *
     * @return the type of the attachment
     */
    Type getType();

}
