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
 *  $Id$
 */


package org.openvpms.component.business.dao.hibernate.im.party;

// hbernate
import java.util.List;

import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

// openehr java kernel
import org.openehr.rm.datatypes.text.DvText;

// jug
import org.safehaus.uuid.UUIDGenerator;

//openvpms framework
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.support.ItemStructureUtil;
import org.openvpms.component.system.common.test.BaseTestCase;
import org.openvpms.component.system.service.uuid.JUGGenerator;

/**
 * Test the persistence of the {@link Address} object from the information
 * model
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PersistentAddressTestCase extends BaseTestCase {

    /**
     * A UUID generator.
     */
    private JUGGenerator generator;
    
    /**
     * A Hibernate session factory.
     */
    private SessionFactory sessionFactory;
    
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentAddressTestCase.class);
    }


    /**
     * Constructor for PersistentAddressTestCase.
     * 
     * @param name
     */
    public PersistentAddressTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        generator = new JUGGenerator(
                UUIDGenerator.getInstance().getDummyAddress().toString());
        
        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(Address.class);
        this.sessionFactory = config.buildSessionFactory();
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Persist a simple {@link Address oject}
     */
    public void testPersistSingleAddressObject() 
    throws Exception {
        String id = generator.nextId();
        Address address = new Address(
                id, 
                "openVPMS-ADDRESS-MAILING.draft.v1",
                "0.1",
                "at0002", 
                new DvText("address.mailing"), 
                ItemStructureUtil.createItemList("at0003", 
                        new DvText("address.mail.details")));
        assertTrue(address != null);
        
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(address);
            tx.commit();
            
            assertTrue(getAddressById(session, id) != null);
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            session.close();
        }
    }
    
    /**
     * Test the insertion of 100 Address records
     */
    public void testCreate100Addresses()
    throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            for (int index = 0; index < 100; index++) {
                tx = session.beginTransaction();
                Address address = new Address(
                        generator.nextId(), 
                        "openVPMS-ADDRESS-MAILING.draft.v1",
                        "0.1",
                        "at0002", 
                        new DvText("address.mailing"), 
                        ItemStructureUtil.createItemList("at0003", 
                                new DvText("address.mail.details")));
                session.save(address);
                tx.commit();
            }
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            session.close();
        }
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }
    
    /**
     * Retrieve the {@link Address} with the specified id
     * 
     * @param id
     *            the id 
     * @return Address
     *            the corresponding address or null            
     */
    protected Address getAddressById(Session session, String id) {
        Address result = null;
        try {
            Query query = session.getNamedQuery("address.getAddressById");
            query.setString("id", id);
            List rs = query.list();
            
            if (rs.size() != 1) {
                this.error("The query to address.getAddressById returned more than 1 record.");
            } else {
                result = (Address)rs.get(0);
            }
        } catch (Exception exception) {
            this.error("Failed in getAddressById", exception);
        }
        
        return result;
    }
}
