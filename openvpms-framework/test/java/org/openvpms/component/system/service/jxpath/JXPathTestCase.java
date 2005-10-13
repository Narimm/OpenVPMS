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


package org.openvpms.component.system.service.jxpath;

// java 
import java.util.Hashtable;

// jxpath
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

// openvpms-framework
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the JXPath expressions on etity objects and descriptors.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class JXPathTestCase extends BaseTestCase {

    /**
     * Cache a reference to the Archetype service
     */
    private ArchetypeService service;
    
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(JXPathTestCase.class);
    }

    /**
     * Constructor for JXPathTestCase.
     * @param arg0
     */
    public JXPathTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        Hashtable gparams = getTestData().getGlobalParams();
        String afile = (String)gparams.get("assertionFile");
        String dir = (String)gparams.get("dir");
        String extension = (String)gparams.get("extension");
        
        service = new ArchetypeService(dir, new String[] {extension}, afile);
        assertTrue(service != null);
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test JXPath on node descriptors
     */
    public void testPersonNodeDescriptors()
    throws Exception {
        
        // retrieve the node descriptor for animal.pet
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("person.person");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("firstName");
        assertTrue(ndesc !=  null);
        assertTrue(((Boolean)getValue(adesc, "nodeDescriptorsAsMap/firstName/string")).booleanValue());
        assertTrue(((Boolean)getValue(adesc, "nodeDescriptorsAsMap/lastName/string")).booleanValue());
        assertTrue(((Boolean)getValue(adesc, "nodeDescriptorsAsMap/lastName/identifier")).booleanValue() == false);
        assertTrue(((Boolean)getValue(adesc, "boolean(nodeDescriptorsAsMap/lastName/string)")).booleanValue());
        assertTrue(getValue(adesc, "nodeDescriptorsAsMap/jimbo") == null);
    }

    /**
     * Test JXPath using the TestPage
     */
    public void testPageWithNodeDescriptorAndValue() 
    throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("person.person");

        Person person = (Person)service.createDefaultObject("person.person");
        person.setFirstName("jim");
        person.setLastName("alateras");

        TestPage page = new TestPage(person, adesc);
        assertTrue(page != null);
        
        assertTrue(getValue(page, "pathToObject(model,  node/nodeDescriptorsAsMap/lastName/path)").equals("alateras"));
        assertTrue(getValue(page, "pathToObject(model,  node/nodeDescriptorsAsMap/firstName/path)").equals("jim"));

        setValue(page, "pathToObject(model,  node/nodeDescriptorsAsMap/firstName/path)", "Bernie");
        assertTrue(getValue(page, "pathToObject(model,  node/nodeDescriptorsAsMap/firstName/path)").equals("Bernie"));
    }
    
    /**
     * Test that JXPath can evaulate complex boolean expressions
     */
    public void testBooleanExpressionEvaulation()
    throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("person.person");
        assertTrue(((Boolean)getValue(adesc, "nodeDescriptorsAsMap/firstName/string and nodeDescriptorsAsMap/lastName/string")).booleanValue());
        assertTrue(((Boolean)getValue(adesc, "nodeDescriptorsAsMap/firstName/string and not(nodeDescriptorsAsMap/lastName/string)")).booleanValue() == false);
        assertTrue(((Boolean)getValue(adesc, "nodeDescriptorsAsMap/firstName/string and not(nodeDescriptorsAsMap/firstName/number)")).booleanValue());
    }
    
    /**
     * This performs a get using an object and a jxpath expression and 
     * returns the resolved object
     * 
     * @param source
     *            the source object
     * @param path 
     *            the path expression
     * @return Object                        
     */
    private Object getValue(Object source, String path) {
        /**
        Object obj = JXPathContext.newContext(source).getValue(path);
        if (obj instanceof Pointer) {
            obj = ((Pointer)obj).getValue();
        }
        
        return obj;
        **/ 
        
        return JXPathContext.newContext(source).getValue(path);
    }
    
    /**
     * This performs a set using an object an jxpath expression and 
     * a value object
     * 
     * @param source
     *            the source object
     * @param path 
     *            the path expression
     * @param value
     *            the value to set                        
     */
    private void setValue(Object source, String path, Object value) {
        JXPathContext.newContext(source).setValue(path, value);
    }
}
