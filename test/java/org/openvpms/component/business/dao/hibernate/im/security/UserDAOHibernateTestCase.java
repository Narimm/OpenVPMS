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
import org.openvpms.component.business.domain.im.security.User;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link UserDAOHibernate} class.
 *
 * @author Tim Anderson
 */
public class UserDAOHibernateTestCase extends HibernateInfoModelTestCase {

    /**
     * Default user id.
     */
    private static final ArchetypeId USER_ID = new ArchetypeId("security.user.1.0");

    /**
     * ESCI user archetype id.
     */
    private static final ArchetypeId ESCI_USER_ID = new ArchetypeId("user.esci.1.0");

    /**
     * The user DAO.
     */
    private UserDAOHibernate dao;

    /**
     * Tests the {@link UserDAOHibernate#getByUserName(String)} method.
     */
    @Test
    public void testGetByUserName() {
        String name1 = "user1" + System.currentTimeMillis();
        String password1 = "password1";

        String name2 = "user2" + System.currentTimeMillis();
        String password2 = "password2";

        UserDO user1 = createUser(name1, password1, USER_ID);
        UserDO user2 = createUser(name2, password2, ESCI_USER_ID);

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(user1);
        session.save(user2);
        tx.commit();

        checkCanRetrieve(name1, password1);
        checkCanRetrieve(name2, password2);
    }

    /**
     * Tests the {@link UserDAOHibernate#getByUserName(String)} method when results are constrained by archetype
     * using {@link UserDAOHibernate#setUserArchetypes(String[])}.
     */
    @Test
    public void testGetByUserNameWithFilter() {
        String name1 = "user1" + System.currentTimeMillis();
        String password1 = "password1";

        String name2 = "user2" + System.currentTimeMillis();
        String password2 = "password2";

        UserDO user1 = createUser(name1, password1, USER_ID);
        UserDO user2 = createUser(name2, password2, ESCI_USER_ID);

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(user1);
        session.save(user2);
        tx.commit();

        checkCanRetrieve(name1, password1);
        checkCanRetrieve(name2, password2);

        dao.setUserArchetypes(USER_ID.getShortName());
        checkCanRetrieve(name1, password1);
        checkCantRetrieve(name2);

        dao.setUserArchetypes(ESCI_USER_ID.getShortName());
        checkCantRetrieve(name1);
        checkCanRetrieve(name2, password2);

        dao.setUserArchetypes(ESCI_USER_ID.getShortName(), USER_ID.getShortName());
        checkCanRetrieve(name1, password1);
        checkCanRetrieve(name2, password2);
    }

    /**
     * Verfies that {@link UserDAOHibernate#getByUserName(String)} returns an empty list if there is no corresponding
     * user.
     */
    @Test
    public void testGetByUserNameForNoUser() {
        List<User> list = dao.getByUserName("anonexistentusername");
        assertEquals(0, list.size());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        dao = new UserDAOHibernate();
        dao.setSessionFactory(getSessionFactory());
    }

    /**
     * Verifies a user can be retrieved.
     *
     * @param name     the user name
     * @param password the password
     */
    private void checkCanRetrieve(String name, String password) {
        List<User> list = dao.getByUserName(name);
        assertEquals(1, list.size());
        User retrieved = list.get(0);
        assertEquals(name, retrieved.getUsername());
        assertEquals(password, retrieved.getPassword());
    }

    /**
     * Verifies that a user can't be retrieved.
     *
     * @param name the user name
     */
    private void checkCantRetrieve(String name) {
        List<User> list = dao.getByUserName(name);
        assertEquals(0, list.size());
    }

    /**
     * Creates a new user.
     *
     * @param username    the user name
     * @param password    the password
     * @param archetypeId the user archetype
     * @return a new user
     */
    private UserDO createUser(String username, String password, ArchetypeId archetypeId) {
        UserDO user = new UserDOImpl();
        user.setArchetypeId(archetypeId);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

}
