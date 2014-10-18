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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.user;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link UserRules} class.
 *
 * @author Tim Anderson
 */
public class UserRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private UserRules rules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new UserRules(getArchetypeService());
    }

    /**
     * Tests the {@link UserRules#getUser(String)} method.
     */
    @Test
    public void testGetUser() {
        String username = "zuser" + System.currentTimeMillis();
        assertNull(rules.getUser(username));
        User user = TestHelper.createUser(username, true);
        assertEquals(user, rules.getUser(username));
    }

    /**
     * Tests the  {@link UserRules#exists(String)} method.
     */
    @Test
    public void testExists() {
        String username = "zuser" + System.currentTimeMillis();
        assertFalse(rules.exists(username));
        User user = TestHelper.createUser(username, true);
        assertTrue(rules.exists(username));
        user.setActive(false);
        save(user);
        assertTrue(rules.exists(username));
        remove(user);
        assertFalse(rules.exists(username));
    }

    /**
     * Tests the {@link UserRules#exists(String, User)} method.
     */
    @Test
    public void testExistsExcludingUser() {
        String username = "zuser" + System.currentTimeMillis();

        User user1 = TestHelper.createUser(username, true);
        assertFalse(rules.exists(username, user1));

        User user2 = TestHelper.createUser(username, false);
        assertTrue(rules.exists(username, user2));

        user1.setActive(false);
        save(user1);
        assertTrue(rules.exists(username, user2));

        remove(user1);
        assertFalse(rules.exists(username, user2));
    }


    /**
     * Tests the {@link UserRules#isClinician(User)} method.
     */
    @Test
    public void testIsClinician() {
        User user = TestHelper.createUser();
        assertFalse(rules.isClinician(user));
        Lookup clinicianClassification
                = TestHelper.getLookup("lookup.userType", "CLINICIAN");
        user.addClassification(clinicianClassification);
        assertTrue(rules.isClinician(user));
    }

    /**
     * Tests the {@link UserRules#getLocations(User)} method.
     */
    @Test
    public void testGetLocations() {
        User user = TestHelper.createUser();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        EntityBean bean = new EntityBean(user);

        bean.addRelationship("entityRelationship.userLocation", location1);
        bean.addRelationship("entityRelationship.userLocation", location2);
        List<Party> locations = rules.getLocations(user);
        assertEquals(2, locations.size());
        assertTrue(locations.contains(location1));
        assertTrue(locations.contains(location2));
    }

    /**
     * Tests the {@link UserRules#getDefaultLocation(User)} method.
     */
    @Test
    public void testGetDefaultLocation() {
        User user = TestHelper.createUser();

        assertNull(rules.getDefaultLocation(user));

        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        EntityBean bean = new EntityBean(user);

        bean.addRelationship("entityRelationship.userLocation", location1);
        EntityRelationship rel2
                = bean.addRelationship("entityRelationship.userLocation",
                                       location2);

        Party defaultLocation = rules.getDefaultLocation(user);
        assertNotNull(defaultLocation);

        // location can be one of location1, or location2, as default not
        // specified
        assertTrue(defaultLocation.equals(location1)
                   || defaultLocation.equals(location2));

        // mark rel2 as the default
        EntityRelationshipHelper.setDefault(user, "locations", rel2,
                                            getArchetypeService());
        assertEquals(location2, rules.getDefaultLocation(user));
    }

    /**
     * Tests the {@link UserRules#isAdministrator} method.
     */
    @Test
    public void testIsAdministrator() {
        User user = TestHelper.createUser();
        assertFalse(rules.isAdministrator(user));

        Lookup adminClassification = TestHelper.getLookup("lookup.userType", "ADMINISTRATOR");
        user.addClassification(adminClassification);
        assertTrue(rules.isAdministrator(user));
    }
}
