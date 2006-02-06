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
 *  $Id: PersistentUserTestCase.java 326 2005-12-06 01:05:11Z jalateras $
 */

package org.openvpms.component.business.dao.hibernate.im.security;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;

/**
 * Test that we can manage persistent users.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-06 12:05:11 +1100 (Tue, 06 Dec 2005) $
 */
public class PersistentUserTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentUserTestCase.class);
    }

    /**
     * Constructor for PersistentUserTestCase.
     * 
     * @param arg0
     */
    public PersistentUserTestCase(String name) {
        super(name);
    }

    /*
     * @see HibernateInfoModelTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see HibernateInfoModelTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the simple creation of a user
     */
    public void testSimpleUserCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");

            // execute the test
            tx = session.beginTransaction();
            User user = createUser("jima", "jima");
            session.save(user);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            assertTrue(acount1 == acount + 1);
            
            user = (User)session.load(User.class, user.getUid());
            assertTrue(user != null);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /**
     * Test the creation of mutliple users
     */
    public void testMultipleUserCreaton() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");

            // execute the test
            User user = null;
            for (int index = 0; index < 10; index++) {
                tx = session.beginTransaction();
                user = createUser("jima" + index, "jima");
                session.save(user);
                tx.commit();
                user = (User)session.load(User.class, user.getUid());
                assertTrue(user != null);
            }

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            assertTrue(acount1 == acount + 10);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /**
     * Test the modification of a user
     */
    public void testModificationOfUser() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");

            // execute the test
            tx = session.beginTransaction();
            User user = createUser("jima69", "jima69");
            session.save(user);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            assertTrue(acount1 == acount + 1);
            
            user = (User)session.load(User.class, user.getUid());
            assertTrue(user != null);
            
            tx = session.beginTransaction();
            user.setPassword("testModificationOfUser");
            session.save(user);
            tx.commit();

            // retrieve the record and check that the password has changed
            user = (User)session.load(User.class, user.getUid());
            assertTrue(user.getPassword().equals("testModificationOfUser"));
            
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /**
     * Test the deletion of a user
     */
    public void testDeletionOfUser() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");

            // execute the test
            tx = session.beginTransaction();
            User user = createUser("jima33", "jima33");
            session.save(user);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            assertTrue(acount1 == acount + 1);
            
            user = (User)session.load(User.class, user.getUid());
            assertTrue(user != null);
            
            tx = session.beginTransaction();
            session.delete(user);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            assertTrue(acount1 == acount);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /**
     * Test the deletion of multipple users
     */
    public void testDeletionOfMultipleUsers() throws Exception {
        final int count = 10;
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");
            long ids[] = new long[count];

            // execute the test
            User user = null;
            for (int index = 0; index < count; index++) {
                tx = session.beginTransaction();
                user = createUser("jima" + index, "jima");
                session.save(user);
                tx.commit();
                user = (User)session.load(User.class, user.getUid());
                assertTrue(user != null);
                ids[index] = user.getUid();
            }

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            assertTrue(acount1 == acount + count);
            
            // now go through and delete each of the created users
            for (int index = 0; index < count; index++) {
                tx = session.beginTransaction();
                user = (User)session.load(User.class, ids[index]);
                session.delete(user);
                tx.commit();
            }
            
            // ensure that all the created usesrs have been deleted
            acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            assertTrue(acount1 == acount);
            
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test the creation of a user with a role
     */
    public void testUserWithRoleCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");
            int bcount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");

            // execute the test
            tx = session.beginTransaction();
            User user = createUser("jima", "jima");
            user.addRole(createRole("role1"));
            session.save(user);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            int bcount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
            user = (User)session.load(User.class, user.getUid());
            assertTrue(user != null);
            assertTrue(user.getRoles().size() == 1);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
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
        User user = new User();
        user.setArchetypeIdAsString("openvpms-security-security.user.1.0");
        user.setName(name);
        user.setPassword(password);
        
        return user;
    }
    
    /**
     * Creare a role with the specified name
     * 
     * @param name 
     *            the name of the role
     * @return SecurityRole            
     */
    private SecurityRole createRole(String name) throws Exception {
        SecurityRole role = new SecurityRole();
        role.setArchetypeIdAsString("openvpms-security-security.role.1.0");
        role.setName(name);
        
        return role;
     }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

}
