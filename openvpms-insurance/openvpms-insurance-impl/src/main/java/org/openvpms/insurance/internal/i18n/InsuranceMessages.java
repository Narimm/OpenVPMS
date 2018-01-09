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

package org.openvpms.insurance.internal.i18n;

import org.openvpms.component.i18n.Message;
import org.openvpms.component.i18n.Messages;

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

    /**
     * Message indicating that a claim has the wrong status for finalisation.
     *
     * @param status the claim status
     * @return a new message
     */
    public static Message cannotFinaliseClaimWithStatus(String status) {
        return messages.create(1, status);
    }

    /**
     * Message indicating that a claim cannot be finalised as an attachment is in error.
     *
     * @param fileName the attachment file name
     * @return a new message
     */
    public static Message cannotFinaliseClaimAttachmentError(String fileName) {
        return messages.create(2, fileName);
    }

    /**
     * Message indicating that a claim cannot be finalised as an attachment has no content.
     *
     * @param fileName the attachment file name
     * @return a new message
     */
    public static Message cannotFinaliseClaimNoAttachment(String fileName) {
        return messages.create(3, fileName);
    }

    /**
     * Message indicating that a claim could not be finalised.
     *
     * @param message the cause
     * @return a new message
     */
    public static Message failedToFinaliseClaim(String message) {
        return messages.create(4, message);
    }

    /**
     * Message indicating that the claim identifier archetype cannot be changed.
     *
     * @param current the current archetype
     * @param other   the other archetype
     * @return a new message
     */
    public static Message differentClaimIdentifierArchetype(String current, String other) {
        return messages.create(5, current, other);
    }

    /**
     * Message indicating that an attachment has no content.
     *
     * @param fileName the attachment file name
     * @return a new message
     */
    public static Message attachmentHasNoContent(String fileName) {
        return messages.create(20, fileName);
    }

    /**
     * Message indicating that a policy has no policy number.
     *
     * @return a new message
     */
    public static Message policyHasNoId() {
        return messages.create(30);
    }

    /**
     * Message indicating that a policy has no insurer.
     *
     * @return a new message
     */
    public static Message policyHasNoInsurer() {
        return messages.create(31);
    }

    /**
     * Message indicating that a policy has no patient.
     *
     * @return a new message
     */
    public static Message policyHasNoPatient() {
        return messages.create(32);
    }

    /**
     * Message indicating that a policy has no patient.
     *
     * @return a new message
     */
    public static Message policyHasNoCustomer() {
        return messages.create(33);
    }

    /**
     * Message indicating that a policy has no expiry date.
     *
     * @return a new message
     */
    public static Message policyHasNoExpiryDate() {
        return messages.create(34);
    }

    /**
     * Message indicating that an insurance service is unavailable.
     *
     * @param name the service configuration name
     * @return a new message
     */
    public static Message serviceUnavailable(String name) {
        return messages.create(40, name);
    }
}
