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

package org.openvpms.web.component.subscription;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SubscriptionHelper}.
 *
 * @author Tim Anderson
 */
public class SubscriptionHelperTestCase {

    /**
     * Tests the {@link SubscriptionHelper#formatSubscription(String, String, Date, Date)} method.
     */
    @Test
    public void testFormatSubscription() {
        assertEquals("This is a trial version of OpenVPMS. Please "
                     + "<a href=\"http://www.openvpms.org/subscription-form\">subscribe</a> to comply with licensing "
                     + "requirements.", SubscriptionHelper.formatSubscription(null, null, null, new Date()));
        check("OpenVPMS subscription for Vets R Us expires on Friday, 1 January 2016", "2016-01-01", "2015-10-01");
        check("OpenVPMS subscription for Vets R Us expires on Friday, 1 January 2016", "2016-01-01", "2015-12-10");
        check("WARNING: your OpenVPMS subscription expires on Friday, 1 January 2016.\n" +
              "Please contact your administrator to renew the subscription.", "2016-01-01", "2015-12-11");
        check("WARNING: your OpenVPMS subscription expires on Friday, 1 January 2016.\n" +
              "Please contact your administrator to renew the subscription.", "2016-01-01", "2016-01-01");
        check("OpenVPMS subscription for Vets R Us expired on Friday, 1 January 2016.\n" +
              "Please contact your administrator to renew the subscription.", "2016-01-01", "2016-01-02");
    }

    /**
     * Verifies a subscription is formatted correctly.
     *
     * @param expected   the expected text
     * @param expiryDate the expiry date
     * @param now        the current date
     */
    private void check(String expected, String expiryDate, String now) {
        String organisation = "Vets R Us";
        String name = "A Vet";
        String text = SubscriptionHelper.formatSubscription(organisation, name, TestHelper.getDate(expiryDate),
                                                            TestHelper.getDate(now));
        assertEquals(expected, text);
    }
}
