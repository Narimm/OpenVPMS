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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link FromAddressFormatter} class.
 *
 * @author Tim Anderson
 */
public class FromAddressFormatterTestCase extends AbstractAddressFormatterTest {

    /**
     * Tests the {@link FromAddressFormatter#format(Contact)} method.
     */
    @Override
    @Test
    public void testFormat() {
        AddressFormatter formatter = new FromAddressFormatter();
        Party location = (Party) create(PracticeArchetypes.LOCATION);
        location.setName("Main Clinic");

        Contact email = TestHelper.createEmailContact("main@clinic.com");
        location.addContact(email);

        assertEquals("Main Clinic <main@clinic.com>", formatter.format(email));

        email.setName("Main Clinic Accounts");

        assertEquals("Main Clinic Accounts (Main Clinic) <main@clinic.com>", formatter.format(email));
    }

    /**
     * Creates a new address formatter.
     *
     * @return a new address formatter
     */
    @Override
    protected AddressFormatter createAddressFormatter() {
        return new FromAddressFormatter();
    }
}
