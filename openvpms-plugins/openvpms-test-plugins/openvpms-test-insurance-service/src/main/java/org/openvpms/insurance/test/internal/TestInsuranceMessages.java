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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.test.internal;

import org.openvpms.component.i18n.Message;
import org.openvpms.component.i18n.Messages;
import org.openvpms.component.model.party.Party;

/**
 * Test insurance service error messages.
 *
 * @author Tim Anderson
 */
public class TestInsuranceMessages {

    /**
     * The messages.
     */
    private static final Messages messages = new Messages("TINS", TestInsuranceMessages.class);

    /**
     * Creates a message for when a policy number is invalid for an insurer.
     *
     * @param policyNumber the policy number
     * @param insurer      the insurer
     * @param prefix       the expected policy number prefix
     * @return a new message
     */
    public static Message invalidPolicyNumber(String policyNumber, Party insurer, String prefix) {
        return messages.create(1, policyNumber, insurer.getName(), prefix);
    }

    /**
     * Creates a message indicating that policy number is too short.
     *
     * @return a new message
     */
    public static Message policyNumberMustBe10Chars() {
        return messages.create(2);
    }

}
