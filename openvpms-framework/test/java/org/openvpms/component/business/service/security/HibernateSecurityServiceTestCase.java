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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.security;

import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;


/**
 * Exercises the security test cases using a hibernate user details service.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 */
@ContextConfiguration("hibernate-security-service-appcontext.xml")
public class HibernateSecurityServiceTestCase extends SecurityServiceTests {

    /**
     * A reference to the dao, which is used to during setup
     */
    @Autowired
    private IMObjectDAO dao;

    /**
     * Create a secure context for authorization testing.
     *
     * @param name        the user name
     * @param password    the password
     * @param authorities the authorities of the person
     */
    @Override
    protected void createSecurityContext(String name, String password, String... authorities) {
        User user = createUser(name, password);

        SecurityRole role = createSecurityRole("role1");
        for (String authority : authorities) {
            // bit of a hack. The authority should be created via the archetype
            // service, but there is no facility to populate it from an
            // authority string.
            ArchetypeAwareGrantedAuthority auth
                    = new ArchetypeAwareGrantedAuthority(authority);
            auth.setArchetypeIdAsString("security.archetypeAuthority.1.0");
            dao.save(auth);
            role.addAuthority(auth);
        }

        user.addRole(role);
        dao.save(role);
        dao.save(user);

        UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(
                user.getName(), user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    /**
     * Create a user given the specified name and password
     *
     * @param name     the name of the user
     * @param password the password associated with the user
     * @return User
     */
    private User createUser(String name, String password) {
        User user = new User();
        user.setArchetypeIdAsString("security.user.1.0");
        user.setUsername(name + System.nanoTime()); // ensure unique
        user.setName(name);
        user.setPassword(password);

        return user;
    }

    /**
     * Create a role with the given name
     *
     * @param name the name of the role
     * @return a new role
     */
    private SecurityRole createSecurityRole(String name) {
        SecurityRole role = new SecurityRole();
        role.setArchetypeIdAsString("security.role.1.0");
        role.setName(name);

        return role;
    }

}
