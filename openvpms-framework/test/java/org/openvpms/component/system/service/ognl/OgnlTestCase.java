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


package org.openvpms.component.system.service.ognl;

// ognl 
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlContext;

// openvpms-framework
import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test aspects of OGNL.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OgnlTestCase extends BaseTestCase {
    /**
     * A reference to the Ognl contect
     */
    OgnlContext context;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(OgnlTestCase.class);
    }

    /**
     * Constructor for OgnlTestCase.
     * @param arg0
     */
    public OgnlTestCase(String arg0) {
        super(arg0);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        context =(OgnlContext)Ognl.createDefaultContext(null);
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the simple OGNL evaluation on the {@link Animal} class
     */
    public void testSimpleOgnlOnAnimal()
    throws Exception {
        // set up the entity
        Animal pet = new Animal();
        pet.setName("bella");
        pet.setColour("red");
        
        assertTrue(Ognl.getValue("name", context, pet).equals("bella"));
        assertTrue(Ognl.getValue("colour", context, pet).equals("red"));
    }
    
    /**
     * Test a call to a instance method with no arguments
     */
    public void testNoArgMethodCall()
    throws Exception {
        assertTrue(Ognl.getValue("nullArgumentMethod()", context, this).equals("null"));
        
        try {
            assertTrue(Ognl.getValue("nullArgumentMethod", context, this).equals("null"));
            fail("Method should not evaluate");
        } catch (NoSuchPropertyException exception) {
            // we should get this exception since the expression is wrong
        }
    }
    
    /**
     * Test a call to a instance method with no arguments
     */
    public void testSingleArgMethodCall()
    throws Exception {
        // set up the entity
        Animal pet = new Animal();
        pet.setName("bella");
        pet.setColour("red");
        assertTrue(Ognl.getValue("singleArgumentMethod(nullArgumentMethod())", 
                context, this).equals("null"));
    }
    
    /**
     * Test a call to JXPath getValue 
     */
    public void testJXPathGetValue()
    throws Exception {
        // set up the entity
        Animal pet = new Animal();
        pet.setName("bella");
        pet.setColour("red");
        
        // set up the JXPath evaluator
        assertTrue(Ognl.getValue("evaluateGetValue()", context, 
                new JXPathEvaluator(pet, "colour")).equals("red"));
        
        // evaluate a set fillowed by a get
        Ognl.getValue("evaluateSetValue()", context,
                new JXPathEvaluator(pet, "colour", "yellow"));
        assertTrue(Ognl.getValue("evaluateGetValue()", context, 
                new JXPathEvaluator(pet, "colour")).equals("yellow"));
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data required.
    }
    
    /**
     * This method takes no parameters ahd returns a String value
     * 
     * @return String
     */
    public String nullArgumentMethod() {
        return "null";
    }
    
    /**
     * This method takes a single parameter and returns a String value
     * 
     * @param name
     *            a name
     * @return String
     */
    public String singleArgumentMethod(String name) {
        return name;
    }
    
    /**
     * This is an expression that evaluates a JXPath 
     * 
     * @param object
     *            the root object
     * @param expression
     *  `         the epression
     * @return Object              
     */
    public Object getValue(Object root, String expression) 
    throws Exception {
        JXPathContext context = JXPathContext.newContext(root);
        return context.getValue(expression);
    }

}
