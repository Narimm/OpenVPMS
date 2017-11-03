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

package org.openvpms.insurance.i18n;

import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.component.system.common.i18n.Messages;

/**
 * Insurance messages.
 *
 * @author Tim Anderson
 */
public class InsuranceMessages {

    /**
     * The messages.
     */
    private static Messages messages = new Messages("INS", InsuranceMessages.class.getName());

    public static Message cannotFinaliseClaimWithStatus(String status) {
        return messages.getMessage(1, status);
    }

    public static Message cannotFinaliseClaimAttachmentError(String fileName) {
        return messages.getMessage(2, fileName);
    }

    public static Message cannotFinaliseClaimNoAttachment(String fileName) {
        return messages.getMessage(3, fileName);
    }

    public static Message failedToFinaliseClaim(String message) {
        return messages.getMessage(4, message);
    }

    public static Message differentClaimIdentifierArchetype(String current, String other) {
        return messages.getMessage(5, current, other);
    }

    /**
     * Message indicating that an attachment has no content.
     *
     * @param fileName the attachment file name
     * @return a new message
     */
    public static Message attachmentHasNoContent(String fileName) {
        return messages.getMessage(20, fileName);
    }

    /**
     * Message indicating that a policy has no policy number.
     *
     * @return a new message
     */
    public static Message policyHasNoId() {
        return messages.getMessage(30);
    }

    /**
     * Message indicating that a policy has no insurer.
     *
     * @return a new message
     */
    public static Message policyHasNoInsurer() {
        return messages.getMessage(31);
    }

    /**
     * Message indicating that a policy has no patient.
     *
     * @return a new message
     */
    public static Message policyHasNoPatient() {
        return messages.getMessage(32);
    }

    /**
     * Message indicating that a policy has no patient.
     *
     * @return a new message
     */
    public static Message policyHasNoCustomer() {
        return messages.getMessage(33);
    }

    /**
     * Message indicating that a policy has no expiry date.
     *
     * @return a new message
     */
    public static Message policyHasNoExpiryDate() {
        return messages.getMessage(34);
    }

}
