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

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Tests the {@link AssertionTypeDescriptorDOImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AssertionTypeDescriptorDOTestCase
        extends HibernateInfoModelTestCase {

    /**
     * The initial no. of assertion type descriptors in the database.
     */
    private int types;


    /**
     * Test the creation of an assertion type descriptor.
     */
    @Test
    public void testCreate() {

        Session session = getSession();
        Transaction tx = session.beginTransaction();

        AssertionTypeDescriptorDO adesc = createAssertionTypeDescriptor(
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

        assertEquals(types + 5, count(AssertionTypeDescriptorDOImpl.class));
    }

    /**
     * Test the deletion of an assertion type descriptor.
     */
    @Test
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        AssertionTypeDescriptorDO adesc = createAssertionTypeDescriptor(
                "maxLength");
        addActionTypeToDescriptor(adesc, "assert", "LengthFunctions",
                                  "evalMaxLength");
        session.save(adesc);
        tx.commit();

        assertEquals(types + 1, count(AssertionTypeDescriptorDOImpl.class));

        adesc = (AssertionTypeDescriptorDO) session.load(
                AssertionTypeDescriptorDOImpl.class, adesc.getId());
        assertNotNull(adesc);
        assertEquals("maxLength", adesc.getName());

        tx = session.beginTransaction();
        session.delete(adesc);
        tx.commit();
        assertEquals(types, count(AssertionTypeDescriptorDOImpl.class));
    }

    /**
     * Test the update of an assertion type descriptor.
     */
    @Test
    public void testUpdate() {

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        AssertionTypeDescriptorDO adesc = createAssertionTypeDescriptor(
                "minLength");
        addActionTypeToDescriptor(adesc, "assert", "LengthFunctions",
                                  "evalMinLength");
        session.save(adesc);
        tx.commit();

        assertEquals(types + 1, count(AssertionTypeDescriptorDOImpl.class));

        tx = session.beginTransaction();
        adesc = (AssertionTypeDescriptorDO) session.load(
                AssertionTypeDescriptorDOImpl.class, adesc.getId());
        assertNotNull(adesc);

        String className = "MinLengthFunctions";
        adesc.getActionType("assert").setClassName(className);
        session.saveOrUpdate(adesc);
        tx.commit();

        assertEquals(types + 1, count(AssertionTypeDescriptorDOImpl.class));

        adesc = (AssertionTypeDescriptorDO) session.load(
                AssertionTypeDescriptorDOImpl.class, adesc.getId());
        assertNotNull(adesc);
        assertEquals(className, adesc.getActionType("assert").getClassName());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        types = count(AssertionTypeDescriptorDOImpl.class);
    }

    /**
     * Create an {@link AssertionTypeDescriptor} with the specified parameters.
     *
     * @param name the name
     * @return AssertionTypeDescriptor
     */
    private AssertionTypeDescriptorDO createAssertionTypeDescriptor(String name) {
        AssertionTypeDescriptorDO desc = new AssertionTypeDescriptorDOImpl();
        desc.setName(name);
        desc.setPropertyArchetype("openvpms-dum-dum");
        return desc;
    }

    /**
     * Add an action to the specified {@link AssertionTypeDescriptor}.
     *
     * @param descriptor the source descriptor
     * @param name       the action name
     * @param clazz      the action class name
     * @param method     the action method name
     */
    private void addActionTypeToDescriptor(AssertionTypeDescriptorDO descriptor,
                                           String name, String clazz,
                                           String method) {
        ActionTypeDescriptorDO action = new ActionTypeDescriptorDOImpl();
        action.setName(name);
        action.setClassName(clazz);
        action.setMethodName(method);
        descriptor.addActionType(action);
    }
}
