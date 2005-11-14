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

package org.openvpms.component.business.service.archetype.descriptor;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;

/**
 * Tests out the assertion type hibernate mapping.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentAssertionTypeDescriptorTestCase extends HibernateInfoModelTestCase {

    /**
     * main line
     * 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentAssertionTypeDescriptorTestCase.class);
    }

    /**
     * Constructor for PersistentArchetypeDescriptorTestCase.
     * 
     * @param name
     */
    public PersistentAssertionTypeDescriptorTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test the creation of a simple assertion type descriptor
     */
    public void testCreateSimpleAssertionTypeDescriptor() throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            tx = session.beginTransaction();
            AssertionTypeDescriptor adesc = createAssertionTypeDescriptor(
                    "regularExpression", "StringFunctions", "evaluateRegEx");
            session.save(adesc);
            tx.commit();
            
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            assertTrue(acount1 == acount + 1);
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
     * Test the creation of multiple assertion type descriptor
     */
    public void testCreateMultipleAssertionTypeDescriptor() throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            tx = session.beginTransaction();
            session.save(createAssertionTypeDescriptor(
                    "regularExpression", "StringFunctions", "evaluateRegEx"));
            session.save(createAssertionTypeDescriptor(
                    "regularExpression1", "StringFunctions", "evaluateRegEx1"));
            session.save(createAssertionTypeDescriptor(
                    "regularExpression2", "StringFunctions", "evaluateRegEx2"));
            session.save(createAssertionTypeDescriptor(
                    "regularExpression3", "StringFunctions", "evaluateRegEx3"));
            session.save(createAssertionTypeDescriptor(
                    "regularExpression4", "StringFunctions", "evaluateRegEx4"));
            tx.commit();
            
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            assertTrue(acount1 == acount + 5);
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
     * Test the deletion of an assertion type descriptor
     */
    public void testDeletionAssertionTypeDescriptor()
    throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            tx = session.beginTransaction();
            AssertionTypeDescriptor adesc = createAssertionTypeDescriptor(
                    "maxLength", "LengthFunctions", "evalMaxLength");
            session.save(adesc);
            tx.commit();
            
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            assertTrue(acount1 == acount + 1);
            
            adesc = (AssertionTypeDescriptor)session.load(
                   AssertionTypeDescriptor.class, adesc.getUid());
            assertTrue(adesc != null);
            assertTrue(adesc.getName().equals("maxLength"));
            
            tx = session.beginTransaction();
            session.delete(adesc);
            tx.commit();

            acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
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
     * Test the update of an assertion type descriptor
     */
    public void testUpdateAssertionTypeDescriptor()
    throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            int acount = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            tx = session.beginTransaction();
            AssertionTypeDescriptor adesc = createAssertionTypeDescriptor(
                    "minLength", "LengthFunctions", "evalMinLength");
            session.save(adesc);
            tx.commit();
            
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            assertTrue(acount1 == acount + 1);
            
            tx = session.beginTransaction();
            adesc = (AssertionTypeDescriptor)session.load(
                   AssertionTypeDescriptor.class, adesc.getUid());
            assertTrue(adesc != null);
            
            adesc.setType("MinLengthFunctions");
            session.saveOrUpdate(adesc);
            tx.commit();

            acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            assertTrue(acount1 == acount + 1);
            adesc = (AssertionTypeDescriptor)session.load(
                    AssertionTypeDescriptor.class, adesc.getUid());
            assertTrue(adesc != null);
            assertTrue(adesc.getType().equals("MinLengthFunctions"));
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        currentSession().flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

    /**
     * Create an {@link AssertionTypeDescriptor} with the specified parameters
     * 
     * @return AssertionTypeDescriptor
     */
    private AssertionTypeDescriptor createAssertionTypeDescriptor(String name,
            String type, String method) {
        AssertionTypeDescriptor desc = new AssertionTypeDescriptor();
        desc.setName(name);
        desc.setType(type);
        desc.setMethodName(method);
        
        return desc;
    }
}
