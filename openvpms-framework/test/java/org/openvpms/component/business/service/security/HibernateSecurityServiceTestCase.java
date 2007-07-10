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

// java core
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;

import java.util.List;

/**
 * Exercises the security test cases using a hibernate user details service
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class HibernateSecurityServiceTestCase extends SecurityServiceTests {

    /**
     * A reference to the dao, which is used to during setup
     */
    private IMObjectDAO dao;

    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateSecurityServiceTestCase.class);
    }

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
        return new String[] { "org/openvpms/component/business/service/security/hibernate-security-service-appcontext.xml" };
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

        // create the approapriate user records
        createUserAndRoles();
    }

    /**
     * Create a user given the specified name and password
     * 
     * @param name
     *            the name of the user
     * @param password
     *            the password associated with the user
     * @return User
     */
    private User createUser(String name, String password) throws Exception {
        User user = (User) archetype.create("security.user");
        user.setUsername(name);
        user.setName(name);
        user.setPassword(password);

        return user;
    }

    /**
     * Create a role with the given name
     * 
     * @param name
     *            the name of the role
     */
    private SecurityRole createSecurityRole(String name) throws Exception {
        SecurityRole role = (SecurityRole) archetype.create("security.role");
        role.setName(name);

        return role;
    }

    /**
     * Create an authority with the specified parameters
     * 
     * @param name
     *            an alias for the authority
     * @param service
     *            the service that is secured
     * @param method
     *            the method that is secured (can be a regex)
     * @param shortName
     *            the archetype short name that is secured (can be regex).
     * @return ArchetypeAwareGrantedAuthority
     */
    private ArchetypeAwareGrantedAuthority createAuthority(String name,
            String service, String method, String shortName) throws Exception {
        ArchetypeAwareGrantedAuthority authority = (ArchetypeAwareGrantedAuthority) archetype
                .create("security.archetypeAuthority");
        authority.setName(name);
        authority.setServiceName(service);
        authority.setMethod(method);
        authority.setArchetypeShortName(shortName);

        return authority;
    }

    /**
     * Create the appropriate user and role
     */
    private void createUserAndRoles() throws Exception {


        // create the user with roles and authorities
        deleteUser("jima");
        User user = createUser("jima", "jima");
        SecurityRole role1 = createSecurityRole("role1");
        ArchetypeAwareGrantedAuthority authority = createAuthority(
                "save.person.person", "archetypeService", "save", "party.person");
        dao.save(authority);
        
        role1.addAuthority(authority);
        user.addRole(role1);
        dao.save(role1);
        dao.save(user);

        // delete and recreate the user bernief
        deleteUser("bernief");
        user = createUser("bernief", "bernief");
        SecurityRole role2 = createSecurityRole("role2");
        authority = createAuthority("save.party.animal", "archetypeService", 
                "save", "party.animal");
        dao.save(authority);
        
        role2.addAuthority(authority);
        user.addRole(role2);
        dao.save(role2);
        dao.save(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.security.MemorySecurityServiceTestCase#createSecurityContext(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    protected void createSecurityContext(String name, String password,
            String authority) {
        List<IMObject> users = dao.get("security.user", name,
                User.class.getName(), true, 0, ArchetypeQuery.ALL_RESULTS).getResults();
        if (users.size() != 1) {
            fail("Failed to create security context. Could not locate user "
                    + name + " in database");
        }

        User user = (User) users.get(0);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user.getName(), user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }
    
    /**
     * Deletes the user with the specified name
     * 
     * @param name
     *            the name of the user to delete
     */
    private void deleteUser(String name) 
    throws Exception {
        List<IMObject> users = dao.get("security.user", name,
                User.class.getName(), true, 0, ArchetypeQuery.ALL_RESULTS).getResults();
        if (users.size() > 0) {
            for (IMObject im : users) {
                dao.delete(im);
            }
        }
    }
}
