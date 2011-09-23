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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.sms.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.i18n.Message;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * Tests the {@link SMSMessages} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSMessagesTestCase {

    /**
     * Verifies there is a test case for each message.
     */
    @Test
    public void testCoverage() {
        Set<String> messages = new HashSet<String>();
        Set<String> tests = new HashSet<String>();
        for (Method method : SMSMessages.class.getMethods()) {
            if (method.getReturnType() == Message.class) {
                messages.add(method.getName());
            }
        }
        for (Method method : SMSMessagesTestCase.class.getMethods()) {
            String name = method.getName();
            if (name.startsWith("test") && !name.equals("testCoverage")) {
                tests.add(name);
            }
        }
        assertEquals(messages.size(), tests.size());
        for (String message : messages) {
            String test = "test" + message.substring(0, 1).toUpperCase() + message.substring(1);
            assertTrue(tests.contains(test));
        }
    }

    /**
     * Tests the {@link SMSMessages#SMSNotConfigured} method.
     */
    @Test
    public void testSMSNotConfigured() {
        Party practice = new Party();
        practice.setName("foo");
        assertEquals("SMS-0100: The SMS provider is not configured for practice foo",
                     SMSMessages.SMSNotConfigured(practice).toString());
    }

    /**
     * Tests the {@link SMSMessages#practiceNotFound} method.
     */
    @Test
    public void testPracticeNotFound() {
        assertEquals("SMS-0101: Practice not found", SMSMessages.practiceNotFound().toString());
    }

    /**
     * Tests the {@link SMSMessages#failedToCreateEmail} method.
     */
    @Test
    public void testFailedToCreateEmail() {
        assertEquals("SMS-0200: Failed to create email: foo", SMSMessages.failedToCreateEmail("foo").toString());
    }

    /**
     * Tests the {@link SMSMessages#failedToEvaluateExpression} method.
     */
    @Test
    public void testFailedToEvaluateExpression() {
        assertEquals("SMS-0300: Failed to evaluate expression: foo",
                     SMSMessages.failedToEvaluateExpression("foo").toString());
    }

    /**
     * Tests the {@link SMSMessages#invalidFromAddress} method.
     */
    @Test
    public void testInvalidFromAddress() {
        assertEquals("SMS-0301: Invalid 'From' email address: foo", SMSMessages.invalidFromAddress("foo").toString());
    }

    /**
     * Tests the {@link SMSMessages#invalidToAddress} method.
     */
    @Test
    public void testInvalidToAddress() {
        assertEquals("SMS-0302: Invalid 'To' email address: foo", SMSMessages.invalidToAddress("foo").toString());
    }

    /**
     * Tests the {@link SMSMessages#invalidReplyToAddress} method.
     */
    @Test
    public void testInvalidReplyToAddress() {
        assertEquals("SMS-0303: Invalid 'Reply To' email address: foo",
                     SMSMessages.invalidReplyToAddress("foo").toString());
    }

    /**
     * Tests the {@link SMSMessages#noMessageText} method.
     */
    @Test
    public void testNoMessageText() {
        assertEquals("SMS-0304: Message has no text", SMSMessages.noMessageText().toString());
    }

}
