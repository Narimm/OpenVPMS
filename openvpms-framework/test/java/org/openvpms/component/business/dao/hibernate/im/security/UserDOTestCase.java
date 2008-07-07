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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.security;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Tests the {@link UserDO} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-06 12:05:11 +1100 (Tue, 06 Dec 2005) $
 */
public class UserDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of roles in the database.
     */
    private int roles;

    /**
     * The initial no. of users in the database.
     */
    private int users;


    /**
     * Tests the creation of an user.
     */
    public void testCreate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UserDO user = createUser("admin", "password");
        String name = user.getUsername();
        session.save(user);
        tx.commit();

        user = reload(user);
        assertNotNull(user);
        assertEquals(name, user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals(users + 1, count(UserDO.class));
    }

    /**
     * Test the deletion of an user.
     */
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UserDO user = createUser("admin", "password");
        session.save(user);
        tx.commit();

        // check the row count
        assertEquals(users + 1, count(UserDO.class));

        user = (UserDO) session.load(UserDO.class, user.getId());
        assertNotNull(user);

        // delete the user
        tx = session.beginTransaction();
        session.delete(user);
        tx.commit();

        // check the row count
        assertEquals(users, count(UserDO.class));
    }

    /**
     * Test the modification of an user.
     */
    public void testUpdate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UserDO user = createUser("admin", "password");
        session.save(user);
        tx.commit();

        // check the row count
        assertEquals(users + 1, count(UserDO.class));

        user = (UserDO) session.load(UserDO.class, user.getId());
        assertNotNull(user);

        // modify the name and password
        String newName = "root-" + System.currentTimeMillis();
        user.setUsername(newName);
        user.setPassword("secret");
        tx = session.beginTransaction();
        session.save(user);
        tx.commit();

        // check the row count
        assertEquals(users + 1, count(UserDO.class));

        // retrieve and check the attributes
        user = reload(user);
        assertNotNull(user);
        assertEquals(newName, user.getUsername());
        assertEquals("secret", user.getPassword());
    }

    /**
     * Verifies that a lookup with the same archetype and code as an existing
     * lookup cannot be saved.
     */
    public void testDuplicate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UserDO user1 = createUser("admin", "password");
        session.save(user1);
        tx.commit();

        // create a new user with duplicate user name
        UserDO user2 = new UserDO(user1.getArchetypeId());
        user2.setUsername(user1.getUsername());
        user2.setPassword("secret");
        try {
            tx = session.beginTransaction();
            session.save(user2);
            tx.commit();
            fail("Expected duplicate user insertion to fail");
        } catch (Exception expected) {
            // do nothing
        }
    }

    /**
     * Test the addition of a role to an user.
     */
    public void testAddRole() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UserDO user = createUser("bernief", "bernief");
        SecurityRoleDO role = createSecurityRole("worker");
        user.addRole(role);
        session.save(user);
        tx.commit();

        // check the row counts
        assertEquals(users + 1, count(UserDO.class));
        assertEquals(roles + 1, count(SecurityRoleDO.class));

        user = reload(user);
        assertEquals(1, user.getRoles().size());
        assertEquals(role, user.getRoles().iterator().next());
    }

    /**
     * Test the removal of a role from an user.
     */
    public void testDeleteRole() throws Exception {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UserDO user = createUser("bernief", "bernief");
        SecurityRoleDO role = createSecurityRole("manager");
        user.addRole(role);
        session.save(user);
        tx.commit();

        // check the row counts
        assertEquals(users + 1, count(UserDO.class));
        assertEquals(roles + 1, count(SecurityRoleDO.class));

        user = (UserDO) session.load(UserDO.class, user.getId());
        assertNotNull(user);
        assertEquals(1, user.getRoles().size());

        tx = session.beginTransaction();
        user.removeRole(user.getRoles().iterator().next());
        session.save(user);
        tx.commit();

        // check that the role still exists but the link between
        // user and security role does not exist
        assertEquals(users + 1, count(UserDO.class));
        assertEquals(roles + 1, count(SecurityRoleDO.class));

        user = (UserDO) session.load(UserDO.class, user.getId());
        assertNotNull(user);
        assertEquals(0, user.getRoles().size());

        // now delete the actual role
        tx = session.beginTransaction();
        session.delete(role);
        tx.commit();
        assertEquals(users + 1, count(UserDO.class));
        assertEquals(roles, count(SecurityRoleDO.class));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        users = count(UserDO.class);
        roles = count(SecurityRoleDO.class);
    }

    /**
     * Creates a role with the given name.
     *
     * @param name the name of the role
     * @return a new role
     */
    private SecurityRoleDO createSecurityRole(String name) {
        SecurityRoleDO role = new SecurityRoleDO(
                new ArchetypeId("openvpms-security-security.role.1.0"));
        role.setName(name);
        return role;
    }

    /**
     * Create a user given the specified name and password.
     *
     * @param name     the user's login name
     * @param password the user's password
     * @return User
     */
    private UserDO createUser(String name, String password) {
        UserDO user = new UserDO(
                new ArchetypeId("openvpms-security-security.user.1.0"));
        user.setUsername(name + System.currentTimeMillis()); // ensure unique
        user.setName(name);
        user.setPassword(password);
        return user;
    }
}
