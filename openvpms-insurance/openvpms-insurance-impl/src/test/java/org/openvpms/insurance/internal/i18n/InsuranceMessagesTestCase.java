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

import org.junit.Test;
import org.openvpms.component.i18n.Message;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link InsuranceMessages} class.
 *
 * @author Tim Anderson
 */
public class InsuranceMessagesTestCase {

    /**
     * Tests the {@link InsuranceMessages#cannotFinaliseClaimWithStatus(String)} method.
     */
    @Test
    public void testCannotFinaliseClaimWithStatus() {
        check("INS-0001: Cannot finalise claims with 'foo' status",
              InsuranceMessages.cannotFinaliseClaimWithStatus("foo"));
    }

    /**
     * Tests the {@link InsuranceMessages#cannotFinaliseClaimAttachmentError(String)} method.
     */
    @Test
    public void testCannotFinaliseClaimAttachmentError() {
        check("INS-0002: This claim cannot be finalised.\n\nThe 'foo' attachment is not valid",
              InsuranceMessages.cannotFinaliseClaimAttachmentError("foo"));
    }

    /**
     * Tests the {@link InsuranceMessages#cannotFinaliseClaimNoAttachment(String)} method.
     */
    @Test
    public void testCannotFinaliseClaimNoAttachment() {
        check("INS-0003: This claim cannot be finalised.\n\nThe 'foo' attachment has no content",
              InsuranceMessages.cannotFinaliseClaimNoAttachment("foo"));
    }

    /**
     * Tests the {@link InsuranceMessages#failedToFinaliseClaim(String)} method.
     */
    @Test
    public void testFailedToFinaliseClaim() {
        check("INS-0004: The claim could not be finalised: foo", InsuranceMessages.failedToFinaliseClaim("foo"));
    }

    /**
     * Tests the {@link InsuranceMessages#differentClaimIdentifierArchetype(String, String)} method.
     */
    @Test
    public void testDifferentClaimIdentifierArchetype() {
        check("INS-0005: Cannot change claim identifier archetypes from foo to bar",
              InsuranceMessages.differentClaimIdentifierArchetype("foo", "bar"));
    }

    /**
     * Tests the {@link InsuranceMessages#attachmentHasNoContent(String)} method.
     */
    @Test
    public void testAttachmentHasNoContent() {
        check("INS-0020: Attachment 'foo' has no content", InsuranceMessages.attachmentHasNoContent("foo"));
    }

    /**
     * Tests the {@link InsuranceMessages#policyHasNoId()} method.
     */
    @Test
    public void testPolicyHasNoId() {
        check("INS-0030: Policy has no Policy Number", InsuranceMessages.policyHasNoId());
    }

    /**
     * Tests the {@link InsuranceMessages#policyHasNoInsurer()} method.
     */
    @Test
    public void testPolicyHasNoInsurer() {
        check("INS-0031: Policy has no insurer", InsuranceMessages.policyHasNoInsurer());
    }

    /**
     * Tests the {@link InsuranceMessages#policyHasNoPatient()} method.
     */
    @Test
    public void testPolicyHasNoPatient() {
        check("INS-0032: Policy has no patient", InsuranceMessages.policyHasNoPatient());
    }

    /**
     * Tests the {@link InsuranceMessages#policyHasNoCustomer()} method.
     */
    @Test
    public void testpolicyHasNoCustomer() {
        check("INS-0033: Policy has no customer", InsuranceMessages.policyHasNoCustomer());
    }

    /**
     * Tests the {@link InsuranceMessages#policyHasNoExpiryDate()} method.
     */
    @Test
    public void testPolicyHasNoExpiryDate() {
        check("INS-0034: Policy has no expiry date", InsuranceMessages.policyHasNoExpiryDate());
    }

    /**
     * Tests the {@link InsuranceMessages#serviceUnavailable(String)} method.
     */
    @Test
    public void testServiceUnavailable() {
        check("INS-0040: The foo is currently not available", InsuranceMessages.serviceUnavailable("foo"));
    }

    /**
     * Verifies a message matches that expected.
     *
     * @param expected the expected message
     * @param actual   the actual message
     */
    private void check(String expected, Message actual) {
        assertEquals(expected, actual.toString());
    }

}
