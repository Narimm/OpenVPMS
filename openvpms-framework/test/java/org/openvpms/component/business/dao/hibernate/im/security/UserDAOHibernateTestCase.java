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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.security;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipDOImpl;
import org.openvpms.component.business.dao.hibernate.im.party.ContactDO;
import org.openvpms.component.business.dao.hibernate.im.party.ContactDOImpl;
import org.openvpms.component.business.dao.hibernate.im.party.PartyDO;
import org.openvpms.component.business.dao.hibernate.im.party.PartyDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.model.entity.EntityRelationship;
import org.openvpms.component.model.party.Contact;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link UserDAOHibernate} class.
 *
 * @author Jim Alateras
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
     * Location archetype id.
     */
    private static final ArchetypeId LOCATION_ID = new ArchetypeId("contact.location.1.0");

    /**
     * Phone archetype id.
     */
    private static final ArchetypeId PHONE_ID = new ArchetypeId("contact.phoneNumber.1.0");

    /**
     * Practice-location id.
     */
    private static final ArchetypeId PRACTICE_LOCATION_ID = new ArchetypeId("party.organisationLocation.1.0");

    /**
     * User-location id.
     */
    private static final ArchetypeId USER_LOCATION_ID = new ArchetypeId("entityRelationship.userLocation.1.0");

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

        PartyDO location1 = new PartyDOImpl();
        location1.setArchetypeId(PRACTICE_LOCATION_ID);
        EntityRelationshipDO link1 = new EntityRelationshipDOImpl(USER_LOCATION_ID);
        link1.setSource(user1);
        link1.setTarget(location1);
        user1.addSourceEntityRelationship(link1);
        location1.addTargetEntityRelationship(link1);

        PartyDO location2 = new PartyDOImpl();
        location2.setArchetypeId(PRACTICE_LOCATION_ID);
        EntityRelationshipDO link2 = new EntityRelationshipDOImpl(USER_LOCATION_ID);
        link2.setSource(user2);
        link2.setTarget(location2);
        user2.addSourceEntityRelationship(link2);
        location2.addTargetEntityRelationship(link2);

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(user1);
        session.save(user2);
        session.save(location1);
        session.save(location2);
        tx.commit();

        User retrieved1 = checkCanRetrieve(name1, password1);
        User retrieved2 = checkCanRetrieve(name2, password2);
        assertEquals(1, retrieved1.getSourceEntityRelationships().size());
        assertEquals(1, retrieved2.getSourceEntityRelationships().size());

        EntityRelationship relationship1 = retrieved1.getSourceEntityRelationships().iterator().next();
        EntityRelationship relationship2 = retrieved2.getSourceEntityRelationships().iterator().next();
        assertEquals(retrieved1.getObjectReference(), relationship1.getSource());
        assertEquals(retrieved2.getObjectReference(), relationship2.getSource());

        assertEquals(location1.getObjectReference(), relationship1.getTarget());
        assertEquals(location2.getObjectReference(), relationship2.getTarget());
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
     * Verifies contacts are saved and loaded with users.
     */
    @Test
    public void testContacts() {
        String name1 = "u1" + System.currentTimeMillis();
        UserDO user = createUser(name1, "foo", USER_ID);

        ContactDO contact1 = new ContactDOImpl();
        contact1.setArchetypeId(LOCATION_ID);

        ContactDO contact2 = new ContactDOImpl();
        contact2.setArchetypeId(PHONE_ID);

        user.addContact(contact1);
        user.addContact(contact2);

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(user);
        tx.commit();

        List<User> list = dao.getByUserName(name1);
        assertEquals(1, list.size());
        User retrieved = list.get(0);
        Set<Contact> contacts = retrieved.getContacts();
        assertEquals(2, contacts.size());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        dao = new UserDAOHibernate();
        dao.setSessionFactory(getSessionFactory());
    }

    /**
     * Verifies a user can be retrieved.
     *
     * @param name     the user name
     * @param password the password
     */
    private User checkCanRetrieve(String name, String password) {
        List<User> list = dao.getByUserName(name);
        assertEquals(1, list.size());
        User retrieved = list.get(0);
        assertEquals(name, retrieved.getUsername());
        assertEquals(password, retrieved.getPassword());
        return retrieved;
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
