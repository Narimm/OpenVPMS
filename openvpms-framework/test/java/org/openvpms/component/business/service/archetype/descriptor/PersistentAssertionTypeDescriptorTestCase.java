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
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionTypeDescriptor;
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
                    "regularExpression");
            addActionTypeToDescriptor(adesc, "assert", "StringFunctions", 
                    "evaluateRegEx");
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
            AssertionTypeDescriptor adesc = createAssertionTypeDescriptor(
                    "regularExpression");
            addActionTypeToDescriptor(adesc, "assert", "StringFunctions",
                    "evaluateRegEx");
            session.save(adesc);

            adesc = createAssertionTypeDescriptor("regularExpression1");
            addActionTypeToDescriptor(adesc, "assert", "StringFunctions",
                    "evaluateRegEx1");
            session.save(adesc);

            adesc = createAssertionTypeDescriptor("regularExpression2");
            addActionTypeToDescriptor(adesc, "assert", "StringFunctions",
                    "evaluateRegEx2");
            session.save(adesc);
            
            adesc = createAssertionTypeDescriptor("regularExpression3");
            addActionTypeToDescriptor(adesc, "assert", "StringFunctions",
                    "evaluateRegEx3");
            session.save(adesc);
            
            adesc = createAssertionTypeDescriptor("regularExpression4");
            addActionTypeToDescriptor(adesc, "assert", "StringFunctions",
                    "evaluateRegEx4");
            session.save(adesc);
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
                    "maxLength");
            addActionTypeToDescriptor(adesc, "assert", "LengthFunctions", 
                    "evalMaxLength");
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
                    "minLength");
            addActionTypeToDescriptor(adesc, "assert", "LengthFunctions", 
                    "evalMinLength");
            session.save(adesc);
            tx.commit();
            
            int acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            assertTrue(acount1 == acount + 1);
            
            tx = session.beginTransaction();
            adesc = (AssertionTypeDescriptor)session.load(
                   AssertionTypeDescriptor.class, adesc.getUid());
            assertTrue(adesc != null);
            
            adesc.getActionType("assert").setClassName("MinLengthFunctions");
            session.saveOrUpdate(adesc);
            tx.commit();

            acount1 = HibernateDescriptorUtil.getTableRowCount(
                    session, "assertionTypeDescriptor");
            assertTrue(acount1 == acount + 1);
            adesc = (AssertionTypeDescriptor)session.load(
                    AssertionTypeDescriptor.class, adesc.getUid());
            assertTrue(adesc != null);
            assertTrue(adesc.getActionType("assert").getClassName().equals("MinLengthFunctions"));
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
    private AssertionTypeDescriptor createAssertionTypeDescriptor(String name) {
        AssertionTypeDescriptor desc = new AssertionTypeDescriptor();
        desc.setName(name);
        desc.setPropertyArchetype("openvpms-dum-dum"); 
        
        return desc;
    }
    
    /**
     * Add an action to the specified {@link AssertionTypeDescriptor}
     * 
     * @param descriptor
     *            the source descriptor
     * @param name
     *            the action name
     * @param class
     *            the action class name
     * @param method
     *            the action method name
     */
    private void addActionTypeToDescriptor(AssertionTypeDescriptor descriptor, 
            String name, String clazz, String method) {
        ActionTypeDescriptor action = new ActionTypeDescriptor();
        action.setName(name);
        action.setClassName(clazz);
        action.setMethodName(method);
        descriptor.addActionType(action);
    }
}
