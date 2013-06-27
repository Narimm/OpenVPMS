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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link SecurityRole} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("archetype-service-appcontext.xml")
public class ArchetypeServiceSecurityRoleTestCase extends AbstractArchetypeServiceTest {


    /**
     * Verifies that roles can be saved and retrieved.
     */
    @Test
    public void testSave() {
        ArchetypeAwareGrantedAuthority auth1 = createAuthority();
        ArchetypeAwareGrantedAuthority auth2 = createAuthority();
        ArchetypeAwareGrantedAuthority auth3 = createAuthority();

        SecurityRole role = createRole(auth1, auth2, auth3);
        checkRole(role, auth1, auth2, auth3);
    }

    /**
     * Verifies that an authority can be removed from a role.
     */
    @Test
    public void testRemoveAuthority() {
        ArchetypeAwareGrantedAuthority auth1 = createAuthority();
        ArchetypeAwareGrantedAuthority auth2 = createAuthority();

        SecurityRole role = createRole(auth1, auth2);
        checkRole(role, auth1, auth2);
        role.removeAuthority(auth2);
        save(role);
        checkRole(role, auth1);

        // make sure that the authority hasn't been deleted
        assertNotNull(get(auth2));
    }

    /**
     * Verifies that a role can be deleted.
     */
    @Test
    public void testRemoveRole() {
        ArchetypeAwareGrantedAuthority auth1 = createAuthority();
        ArchetypeAwareGrantedAuthority auth2 = createAuthority();

        SecurityRole role = createRole(auth1, auth2);

        remove(role);
        assertNull(get(role));

        // make sure that the authorities haven't been deleted
        assertNotNull(get(auth1));
        assertNotNull(get(auth2));
    }

    /**
     * Verifies that a role can be removed when its associated with a user.
     */
    @Test
    public void testUserRoles() {
        ArchetypeAwareGrantedAuthority auth1 = createAuthority();
        ArchetypeAwareGrantedAuthority auth2 = createAuthority();
        SecurityRole role = createRole(auth1, auth2);

        // create 2 users that share the same role
        User user1 = createUser(role);
        User user2 = createUser(role);

        // remove the role from user1
        user1.removeRole(role);
        save(role, user1);

        // make sure the role has been removed from user1, but not user2
        assertNotNull(get(role));

        user1 = get(user1);
        assertNotNull(user1);
        assertFalse(user1.getRoles().contains(role));

        user2 = get(user2);
        assertTrue(user2.getRoles().contains(role));
    }

    /**
     * Helper to create a user.
     *
     * @param roles the roles to assign
     * @return a new user
     */
    private User createUser(SecurityRole... roles) {
        List<IMObject> toSave = new ArrayList<IMObject>();
        User user;
        user = (User) create("security.user");
        user.setName("XUser" + System.currentTimeMillis());
        user.setUsername("" + System.nanoTime());
        user.setPassword("foo");
        for (SecurityRole role : roles) {
            user.addRole(role);
            toSave.add(role);
        }
        toSave.add(user);
        save(toSave);
        return user;
    }


    /**
     * Verifies that a role can be retrieved and that its attributes are saved correctly.
     *
     * @param role        the role
     * @param authorities the expected authorities
     */
    private void checkRole(SecurityRole role, ArchetypeAwareGrantedAuthority... authorities) {
        SecurityRole retrieved = get(role);
        assertNotNull(retrieved);
        assertEquals(role.getName(), retrieved.getName());
        assertEquals(authorities.length, retrieved.getAuthorities().size());
        for (ArchetypeAwareGrantedAuthority authority : authorities) {
            assertTrue(retrieved.getAuthorities().contains(authority));
        }
    }

    /**
     * Creates and saves a new role.
     *
     * @param authorities the authorities to assign to the role
     * @return a new role
     */
    private SecurityRole createRole(ArchetypeAwareGrantedAuthority... authorities) {
        SecurityRole role = (SecurityRole) create("security.role");
        role.setName("XRole" + System.currentTimeMillis());
        role.setAuthorities(new HashSet<ArchetypeAwareGrantedAuthority>(Arrays.asList(authorities)));
        save(role);
        return role;
    }

    /**
     * Creates and saves an authority.
     *
     * @return a new authority
     */
    private ArchetypeAwareGrantedAuthority createAuthority() {
        ArchetypeAwareGrantedAuthority result;
        result = (ArchetypeAwareGrantedAuthority) create("security.archetypeAuthority");
        result.setName("XAuthority" + System.currentTimeMillis());
        result.setServiceName("foo");
        result.setMethod("bar");
        result.setArchetypeShortName("*");
        return result;
    }
}
