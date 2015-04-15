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

package org.openvpms.component.business.dao.hibernate.im.security;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link SecurityRoleDOImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-06 12:05:11 +1100 (Tue, 06 Dec 2005) $
 */
public class SecurityRoleDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of roles in the database.
     */
    private int roles;

    /**
     * The initial no. of authorities in the database
     */
    private int authorities;


    /**
     * Tests the creation of a role.
     */
    @Test
    public void testCreate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        SecurityRoleDO role = createSecurityRole("admin");
        session.save(role);
        tx.commit();

        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));
    }

    /**
     * Test the deletion of a role.
     */
    @Test
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        SecurityRoleDO role = createSecurityRole("admin");
        session.save(role);
        tx.commit();

        // check the row count
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));

        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);

        // delete the role
        tx = session.beginTransaction();
        session.delete(role);
        tx.commit();

        // check the row count
        assertEquals(roles, count(SecurityRoleDOImpl.class));
    }

    /**
     * Test the modification of a role.
     */
    @Test
    public void testUpdate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        SecurityRoleDO role = createSecurityRole("backup");
        session.save(role);
        tx.commit();

        // check the row count
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));

        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);

        // modify the role
        tx = session.beginTransaction();
        role.setName("backup-engineer");
        session.save(role);
        tx.commit();

        // check the row count
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));

        // retrieve and check the role name
        role = reload(role);
        assertNotNull(role);
        assertEquals("backup-engineer", role.getName());
    }

    /**
     * Test the creation and addition of an archetype authority.
     */
    @Test
    public void testAddAuthority() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        SecurityRoleDO role = createSecurityRole("admin");
        ArchetypeAuthorityDO authority = createAuthority(
                "modify-add-products", "archetype", "create", "product.*");
        session.save(authority);
        role.addAuthority(authority);

        authority = createAuthority(
                "modify-remove-products", "archetype", "remove",
                "product.*");
        session.save(authority);
        role.addAuthority(authority);

        session.save(role);
        tx.commit();

        // check the row counts
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));
        assertEquals(authorities + 2, count(ArchetypeAuthorityDOImpl.class));

        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);
        assertEquals(2, role.getAuthorities().size());
    }

    /**
     * Test the removal of archetype authorities.
     */
    @Test
    public void testDeleteAuthority() {
        final int count = 10;
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        SecurityRoleDO role = createSecurityRole("admin");
        for (int index = 0; index < count; index++) {
            ArchetypeAuthorityDO authority = createAuthority(
                    "authority" + index, "archetype", "create" + index,
                    "product.*");
            session.save(authority);
            role.addAuthority(authority);
        }

        session.save(role);
        tx.commit();

        // check the row counts
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));
        assertEquals(authorities + count, count(ArchetypeAuthorityDOImpl.class));

        // retrieve and check the role
        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);
        assertEquals(count, role.getAuthorities().size());

        // remove the first authority and resave
        tx = session.beginTransaction();
        ArchetypeAuthorityDO authority = role.getAuthorities().iterator().next();
        role.removeAuthority(authority);
        session.save(role);
        tx.commit();

        // retrieve and verify that the authority was removed from the role
        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);
        assertEquals(count - 1, role.getAuthorities().size());

        // verify the authority wasn't removed from the db
        assertNotNull(reload(authority));

        // check row count
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));
        assertEquals(authorities + count, count(ArchetypeAuthorityDOImpl.class));
    }

    /**
     * Test the modification of archetype authorities.
     */
    @Test
    public void testUpdateAuthority() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        SecurityRoleDO role = createSecurityRole("admin");
        ArchetypeAuthorityDO authority = createAuthority(
                "modify-all", "archetype", "create", "*");
        session.save(authority);

        role.addAuthority(authority);
        session.save(role);
        tx.commit();

        // check the row counts
        assertEquals(roles + 1, count(SecurityRoleDOImpl.class));
        assertEquals(authorities + 1, count(ArchetypeAuthorityDOImpl.class));

        // retrieve and check the role
        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);
        assertEquals(1, role.getAuthorities().size());

        // modify the authority
        authority = role.getAuthorities().iterator().next();
        authority.setMethod("modify");
        tx = session.beginTransaction();
        session.save(role);
        tx.commit();

        // retrieve and check it
        role = (SecurityRoleDO) session.load(SecurityRoleDOImpl.class,
                                             role.getId());
        assertNotNull(role);
        assertEquals(1, role.getAuthorities().size());
        authority = role.getAuthorities().iterator().next();
        assertEquals("modify", authority.getMethod());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        roles = count(SecurityRoleDOImpl.class);
        authorities = count(ArchetypeAuthorityDOImpl.class);
    }

    /**
     * Creates a role with the given name.
     *
     * @param name the name of the role
     * @return a new role
     */
    private SecurityRoleDO createSecurityRole(String name) {
        SecurityRoleDO role = new SecurityRoleDOImpl(
                new ArchetypeId("openvpms-security-security.role.1.0"));
        role.setName(name);
        return role;
    }

    /**
     * Create an authority with the specified parameters.
     *
     * @param name      an alias for the authority
     * @param service   the service that is secured
     * @param method    the method that is secured (can be a regex)
     * @param archetype the archetype short name that is secured (can be regex).
     * @return ArchetypeAwareGrantedAuthority
     */
    private ArchetypeAuthorityDO createAuthority(String name,
                                                 String service,
                                                 String method,
                                                 String archetype) {
        ArchetypeAuthorityDO authority = new ArchetypeAuthorityDOImpl(
                new ArchetypeId("openvpms-security-security.authority.1.0"));
        authority.setName(name);
        authority.setServiceName(service);
        authority.setMethod(method);
        authority.setShortName(archetype);
        return authority;
    }

}
