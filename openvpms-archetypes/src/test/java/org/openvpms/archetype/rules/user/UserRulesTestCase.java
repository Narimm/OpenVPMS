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

package org.openvpms.archetype.rules.user;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

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
     * Tests the {@link UserRules#getLocations(User, Party)} method.
     */
    @Test
    public void testGetLocationsByUserAndPractice() {
        User user = TestHelper.createUser();
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        IMObjectBean practiceBean = new IMObjectBean(practice);
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party location3 = TestHelper.createLocation();
        practiceBean.addNodeTarget("locations", location1);
        practiceBean.addNodeTarget("locations", location2);

        EntityBean bean = new EntityBean(user);
        bean.addNodeTarget("locations", location1);
        bean.addNodeTarget("locations", location3);  // not linked to the practice
        List<Party> locations = rules.getLocations(user, practice);
        assertEquals(1, locations.size());
        assertTrue(locations.contains(location1));
        assertFalse(locations.contains(location2));
        assertFalse(locations.contains(location3));
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

    /**
     * Tests the {@link UserRules#getClinicians(Party)} method.
     */
    @Test
    public void testGetClinicians() {
        Party locationA = TestHelper.createLocation();
        Party locationB = TestHelper.createLocation();
        User user1 = TestHelper.createUser();      // user with no locations

        User user2 = TestHelper.createUser();      // user linked to locationA
        addLocation(user2, locationA);

        User user3 = TestHelper.createClinician(); // clinician linked to location A
        addLocation(user3, locationA);

        User user4 = TestHelper.createClinician(); // clinician linked to location B
        addLocation(user4, locationB);

        User user5 = TestHelper.createClinician();  // clinician linked to both locations
        addLocation(user5, locationA);
        addLocation(user5, locationB);

        User user6 = TestHelper.createClinician(); // clinician linked to no locations

        List<User> clinicians1 = rules.getClinicians(locationA);
        assertFalse(clinicians1.contains(user1));
        assertFalse(clinicians1.contains(user2));
        assertTrue(clinicians1.contains(user3));
        assertFalse(clinicians1.contains(user4));
        assertTrue(clinicians1.contains(user5));
        assertTrue(clinicians1.contains(user6));

        List<User> clinicians2 = rules.getClinicians(locationB);
        assertFalse(clinicians2.contains(user1));
        assertFalse(clinicians2.contains(user2));
        assertFalse(clinicians2.contains(user3));
        assertTrue(clinicians2.contains(user4));
        assertTrue(clinicians2.contains(user5));
        assertTrue(clinicians2.contains(user5));
    }

    /**
     * Adds a relationship between a party and practice location.
     *
     * @param party    the party
     * @param location the practice location
     */
    private void addLocation(Party party, Party location) {
        IMObjectBean bean = new IMObjectBean(party);
        bean.addNodeTarget("locations", location);
        bean.save();
    }
}
