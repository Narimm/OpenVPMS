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
 *
 *  $Id: MemorySecurityServiceTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.security;

import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Exercises the security test cases using a hibernate user details service.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class HibernateSecurityServiceTestCase extends SecurityServiceTests {

    /**
     * A reference to the dao, which is used to during setup
     */
    private IMObjectDAO dao;


    /**
     * Default constructor
     */
    public HibernateSecurityServiceTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{"org/openvpms/component/business/service/security/hibernate-security-service-appcontext.xml"};
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        this.archetype = (IArchetypeService) applicationContext
                .getBean("archetypeService");
        this.dao = (IMObjectDAO) applicationContext.getBean("imObjectDao");
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
        user.setUsername(name + System.currentTimeMillis()); // ensure unique
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

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.security.MemorySecurityServiceTestCase#createSecurityContext(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    protected void createSecurityContext(String name, String password,
                                         String ... authorities) {
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

}
