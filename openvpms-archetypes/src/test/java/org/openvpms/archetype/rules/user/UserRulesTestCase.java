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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.user;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.security.User;


/**
 * Tests the {@link UserRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserRulesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link UserRules#getUser(String)} method.
     */
    public void testGetUser() {
        UserRules rules = new UserRules();
        String username = "zuser" + System.currentTimeMillis();
        assertNull(rules.getUser(username));
        User user = TestHelper.createUser(username, true);
        assertEquals(user, rules.getUser(username));
    }

    /**
     * Tests the {@link UserRules#isClinician(User)} method.
     */
    public void testIsClinician() {
        UserRules rules = new UserRules();
        User user = TestHelper.createUser();
        assertFalse(rules.isClinician(user));
        Lookup clinicianClassification
                = TestHelper.getClassification("lookup.userType", "CLINICIAN");
        user.addClassification(clinicianClassification);
        assertTrue(rules.isClinician(user));
    }
}
