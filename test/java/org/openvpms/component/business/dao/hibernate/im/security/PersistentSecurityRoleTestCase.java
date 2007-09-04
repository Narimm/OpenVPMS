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
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;

/**
 * Test that we can manage persistent users.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-06 12:05:11 +1100 (Tue, 06 Dec 2005) $
 */
public class PersistentSecurityRoleTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentSecurityRoleTestCase.class);
    }

    /**
     * Constructor for PersistentUserTestCase.
     *
     * @param name
     */
    public PersistentSecurityRoleTestCase(String name) {
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
    public void testSimpleRoleCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");

            // execute the test
            tx = session.beginTransaction();
            SecurityRole role = createSecurityRole("admin");
            session.save(role);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
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
     * Test the deletion of a role
     */
    public void testRoleDeletion() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");

            // execute the test
            tx = session.beginTransaction();
            SecurityRole role = createSecurityRole("admin");
            session.save(role);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            
            // delete the role
            tx = session.beginTransaction();
            session.delete(role);
            tx.commit();
            acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
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
     * Test the modification of a role
     */
    public void testRoleModification() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");

            // execute the test
            tx = session.beginTransaction();
            SecurityRole role = createSecurityRole("backup");
            session.save(role);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            
            // modify the role
            tx = session.beginTransaction();
            role.setName("backup-engineer");
            session.save(role);
            tx.commit();

            acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            
            // retrieve and check the role name
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            assertTrue(role.getName().equals("backup-engineer"));
            
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
     * Test the creation and addition of a grant authority
     */
    public void testAdditionOfGrantAuthorities() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            int bcount = HibernateSecurityUtil.getTableRowCount(session, "archetypeAwareGrantedAuthority");

            // execute the test
            tx = session.beginTransaction();
            SecurityRole role = createSecurityRole("admin");
            ArchetypeAwareGrantedAuthority authority = createAuthority(
                    "modify-add-products", "archetype", "create", "product.*");
            session.save(authority);
            role.addAuthority(authority);
            
            authority = createAuthority(
                    "modify-remove-products", "archetype", "remove", "product.*");
            session.save(authority);
            role.addAuthority(authority);
            
            session.save(role);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            int bcount1 = HibernateSecurityUtil.getTableRowCount(session, "archetypeAwareGrantedAuthority");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 2);
            
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            assertTrue(role.getAuthorities().size() == 2);
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
     * Test the removal of grant authhorities
     */
    public void testRemovalOfGrantAuthorities() throws Exception {
        final int count = 10;
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            int bcount = HibernateSecurityUtil.getTableRowCount(session, "archetypeAwareGrantedAuthority");

            // execute the test
            tx = session.beginTransaction();
            SecurityRole role = createSecurityRole("admin");
            for (int index = 0; index < count; index++) {
                ArchetypeAwareGrantedAuthority authority = createAuthority(
                        "authority" + index, "archetype", "create" + index, 
                        "product.*");
                session.save(authority);
                role.addAuthority(authority);
            }
            
            session.save(role);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            int bcount1 = HibernateSecurityUtil.getTableRowCount(session, "archetypeAwareGrantedAuthority");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + count);
            
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            assertTrue(role.getAuthorities().size() == count);
            
            // remove the first authority and resave
            tx = session.beginTransaction();
            role.removeAuthority(role.getAuthorities().iterator().next());
            session.save(role);
            
            // check that we actually did delete it
            acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            bcount1 = HibernateSecurityUtil.getTableRowCount(session, "archetypeAwareGrantedAuthority");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + count);
            
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            assertTrue(role.getAuthorities().size() == count - 1);
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
     * Test the modification of grant authorities
     */
    public void testModificationOfGrantAuthorities() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            int bcount = HibernateSecurityUtil.getTableRowCount(session, "archetypeAwareGrantedAuthority");

            // execute the test
            tx = session.beginTransaction();
            SecurityRole role = createSecurityRole("admin");
            ArchetypeAwareGrantedAuthority authority = createAuthority(
                    "modify-all", "archetype", "create", "*");
            session.save(authority);
            
            role.addAuthority(authority);
            session.save(role);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            int bcount1 = HibernateSecurityUtil.getTableRowCount(session, "archetypeAwareGrantedAuthority");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            assertTrue(role.getAuthorities().size() == 1);

            // modify the authority
            authority = role.getAuthorities().iterator().next();
            authority.setMethod("modify");
            tx = session.beginTransaction();
            session.save(role);
            tx.commit();
            
            // retrieve an check it
            role = (SecurityRole)session.load(SecurityRole.class, role.getUid());
            assertTrue(role != null);
            assertTrue(role.getAuthorities().size() == 1);
            authority = role.getAuthorities().iterator().next();
            assertTrue(authority.getMethod().equals("modify"));
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
     * Test the addition of a role to a user
     */
    public void testAdditionOfRoleToUser() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");
            int bcount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");

            // execute the test
            tx = session.beginTransaction();
            User user =  createUser("bernief", "bernief");
            user.addRole(createSecurityRole("worker"));
            session.save(user);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            int bcount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
            // attempt to retrieve the role by itself
            assertTrue(session.load(SecurityRole.class, user.getRoles().iterator().next().getUid()) != null);
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
     * Test the removal of a role from a user
     */
    public void testRemovalOfRoleFromUser() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateSecurityUtil.getTableRowCount(session, "user");
            int bcount = HibernateSecurityUtil.getTableRowCount(session, "securityRole");

            // execute the test
            tx = session.beginTransaction();
            User user =  createUser("bernief", "bernief");
            SecurityRole role = createSecurityRole("manager");
            user.addRole(role);
            session.save(user);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            int bcount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
            // attempt to retrieve the role by itself
            user = (User)session.load(User.class, user.getUid());
            assertTrue(user != null);
            
            tx = session.beginTransaction();
            user.removeRole(user.getRoles().iterator().next());
            session.save(user);
            tx.commit();
            
            // check that the role still exists but the link between
            // user and security role does not exist
            acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            bcount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            user = (User)session.load(User.class, user.getUid());
            assertTrue(user != null);
            assertTrue(user.getRoles().size() == 0);
            
            // now delete the actual role;
            tx = session.beginTransaction();
            session.delete(role);
            tx.commit();
            acount1 = HibernateSecurityUtil.getTableRowCount(session, "user");
            bcount1 = HibernateSecurityUtil.getTableRowCount(session, "securityRole");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount);
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
     * Create a role with the given name
     * 
     * @param name
     *            the name of the role
     */
    private SecurityRole createSecurityRole(String name) throws Exception {
       SecurityRole role = new SecurityRole();
       role.setArchetypeIdAsString("openvpms-security-security.role.1.0");
       role.setName(name);
       
       return role;
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
        user.setUsername(name + System.currentTimeMillis()); // ensure unique
        user.setName(name);
        user.setPassword(password);
        
        return user;
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
     * @param archetype
     *            the archetype short name that is secured (can be regex).                                     
     * @return ArchetypeAwareGrantedAuthority
     */
    private ArchetypeAwareGrantedAuthority createAuthority(String name, String service, 
            String method, String archetype) throws Exception {
        ArchetypeAwareGrantedAuthority authority = 
            new ArchetypeAwareGrantedAuthority();
        authority.setArchetypeIdAsString("openvpms-security-security.authority.1.0");
        authority.setName(name);
        authority.setServiceName(service);
        authority.setMethod(method);
        authority.setArchetypeShortName(archetype);
        
        return authority;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

}
