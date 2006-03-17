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
 *  $Id: SecurityLoader.java 190 2005-10-07 01:31:29Z jalateras $
 */

package org.openvpms.tools.security.loader;

// java core
import java.io.FileReader;
import java.util.List;

// log4j
import org.apache.log4j.Logger;

// hibernate
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.Query;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;

/**
 * This tool will process an XML document and load all the users, roles and 
 * authorities in the database. It will also set up the relationships between
 * users and roles
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-10-07 11:31:29 +1000 (Fri, 07 Oct 2005) $
 */
public class SecurityLoader {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SecurityLoader.class);

    /**
     * A Hibernate session factory.
     */
    private SessionFactory sessionFactory;

    /**
     * A reference ot the security data, which will be processed
     */
    private SecurityData data;

    /**
     * static to hold all session
     */
    public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

    /**
     * Process the data in the specified file.
     * 
     * @param fileName
     *            the file that holds the security data
     */
    public SecurityLoader(String fileName) throws Exception {
        // init
        init();

        // read the security data
        data = (SecurityData)SecurityData.unmarshal(new FileReader(fileName));
    }

    /**
     * The main line
     * 
     * @param args
     *            the file where the data is stored is passed in as the first
     *            argument
     */
    public static void main(String[] args) throws Exception {
        logger.info("Start Loading Security Data");
        SecurityLoader loader = new SecurityLoader(args[0]);
        loader.processRoles();
        loader.processUsers();
        logger.info("End Loading Security Data");
    }

    /**
     * Process the role elements
     */
    protected void processRoles() throws Exception {
        Session session = currentSession();
        for (RoleData roleData : data.getRoleData()) {
            // delete if the role already exists
            // TODO Something to do with the mapping
            deleteIfRoleExists(session, roleData.getName());

            Transaction tx = session.beginTransaction();
            SecurityRole secRole = new SecurityRole();
            secRole.setArchetypeIdAsString("openvpms-system-security.role.1.0");
            secRole.setName(roleData.getName());
            secRole.setDescription(roleData.getDescription());
            for (AuthorityData authData : roleData.getAuthorityData()) {
              ArchetypeAwareGrantedAuthority auth = new ArchetypeAwareGrantedAuthority();
              auth.setArchetypeIdAsString("openvpms-system-security.archetypeAuthority.1.0"); 
              auth.setName(authData.getName());
              auth.setDescription(authData.getDescription());
              auth.setServiceName(authData.getService());
              auth.setMethod(authData.getMethod());
              auth.setArchetypeShortName(authData.getArchShortName());
              secRole.addAuthority(auth);
            }
            session.saveOrUpdate(secRole);
            tx.commit();
            logger.info("Loaded role " + roleData.getName());
        }
    }

    /**
     * Process the user elements
     */
    protected void processUsers() throws Exception {
        Session session = currentSession();
        for (UserData userData : data.getUserData()) {
            // delete if the user already exists
            deleteIfUserExists(session, userData.getName());

            Transaction tx = session.beginTransaction();
            User user = new User();
            user.setArchetypeIdAsString("openvpms-system-security.user.1.0");
            user.setName(userData.getName());
            user.setDescription(userData.getDescription());
            for (Role role : userData.getRole()) {
                SecurityRole secRole = findRoleWithName(session, role.getName());
                user.addRole(secRole);
            }
            session.saveOrUpdate(user);
            tx.commit();
            logger.info("Loaded user " + userData.getName());
        }
    }


    /**
     * Locate the security role with the specified name
     * 
     * @param name
     *            the name of the role
     * @return SecurityRole
     */
    private SecurityRole findRoleWithName(Session session, String name) {
        Query query = session
                .getNamedQuery("securityRole.getByName");
        query.setString("name", name);

        List list = query.list();
        if (list.size() == 0) {
            throw new RuntimeException("Could not locate a role with name:"  + name);
        }
        
        if (list.size() > 1) {
            throw new RuntimeException("More than one record with role name: " + name);
        }
        
        return (SecurityRole)list.get(0);
    }
    
    /**
     * Delete the specified role, if it exists
     * 
     * @param session
     *            the hibernate session to use
     * @param name
     *            the name of the role to delete
     */
    private void deleteIfRoleExists(Session session, String name) 
    throws Exception {
        Transaction tx = session.beginTransaction();
        
        Query query = session.getNamedQuery("securityRole.getByName");
        query.setString("name", name);
        List list = query.list();
        for (Object role : list) {
            SecurityRole secRole = (SecurityRole)role;
            for (User user : secRole.getUsers()) {
                user.removeRole(secRole);
            }
            session.delete(role);
        }
        tx.commit();
    }
    
    /**
     * Delete the specified user, if it exists
     * 
     * @param session
     *            the hibernate session to use
     * @param name
     *            the name of the user to delete
     */
    private void deleteIfUserExists(Session session, String name) 
    throws Exception {
        Transaction tx = session.beginTransaction();
        
        Query query = session.getNamedQuery("user.getByName");
        query.setString("name", name);
        List list = query.list();
        for (Object role : list) {
            session.delete(role);
        }
        tx.commit();
    }
    

    /**
     * Initialise the sesion factory
     */
    private void init() throws Exception {
        Configuration config = new Configuration();
        config.addClass(Contact.class);
        config.addClass(Classification.class);
        config.addClass(Entity.class);
        config.addClass(Act.class);
        config.addClass(ActRelationship.class);
        config.addClass(Participation.class);
        config.addClass(EntityRelationship.class);
        config.addClass(EntityIdentity.class);
        config.addClass(Lookup.class);
        config.addClass(LookupRelationship.class);
        config.addClass(ArchetypeDescriptor.class);
        config.addClass(NodeDescriptor.class);
        config.addClass(AssertionDescriptor.class);
        config.addClass(AssertionTypeDescriptor.class);
        config.addClass(ActionTypeDescriptor.class);
        config.addClass(ProductPrice.class);
        config.addClass(SecurityRole.class);
        config.addClass(ArchetypeAwareGrantedAuthority.class);
        sessionFactory = config.buildSessionFactory();
    }

    /**
     * Get the current hibernate session
     * 
     * @return Session
     * @throws Exception
     */
    private Session currentSession() throws Exception {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = sessionFactory.openSession();
            session.set(s);
        }
        
        return s;
    }

    /**
     * Close the current hibernate session
     * 
     * @throws Exception
     */
    public void closeSession() throws Exception {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
            s.close();
    }
}
