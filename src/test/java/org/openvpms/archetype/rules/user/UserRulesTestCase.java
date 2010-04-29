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

import static org.junit.Assert.*;
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
    @Test
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
    @Test
    public void testIsClinician() {
        UserRules rules = new UserRules();
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
        UserRules rules = new UserRules();
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

        UserRules rules = new UserRules();
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
}
