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


package org.openvpms.component.business.domain.entitytype;

// junit-core
import junit.framework.TestCase;

// openvpms-domain-datatype
import org.openvpms.component.business.domain.datatype.DtIdentifier;
import org.openvpms.component.business.domain.datatype.DtText;

/**
 * This will exercise the {@link EntityType} class
 * 
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class EntityTypeTestCase extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EntityTypeTestCase.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Create an type called Person with the following fields
     * first name, last name, title and age
     */
    public void testCreatePersonEntity()
    throws Exception {
        EntityType type = new EntityType(new DtText("Person"));
        type.addProperty(new PropertyType(new DtText("firstName"),
                new DtText(DtText.class.getName())));
        type.addProperty(new PropertyType(new DtText("lastName"),
                new DtText(DtText.class.getName())));
        type.addProperty(new PropertyType(new DtText("title"),
                new DtText(DtText.class.getName())));
        type.addProperty(new PropertyType(new DtText("age"),
                new DtText(DtText.class.getName())));
        
        assertTrue(type.getPropertyNames().contains(new DtText("firstName")));
        assertTrue(type.getPropertyNames().contains(new DtText("lastName")));
        assertTrue(type.getPropertyNames().contains(new DtText("title")));
        assertTrue(type.getPropertyNames().contains(new DtText("age")));
        assertFalse(type.getPropertyNames().contains(new DtText("ages")));
    }
    
    /**
     * Test entity equality
     */
    public void testEntityEqualityWithNoProperties()
    throws Exception {
        EntityType typea = new EntityType(new DtText("Person"));
        EntityType typeb = new EntityType(new DtText("Person"));
        assertTrue(typea.equals(typeb));
        typeb = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"));
        assertFalse(typea.equals(typeb));
        typeb = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"),
                new DtText("1.0"));
        assertFalse(typea.equals(typeb));

        typea = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"));
        typeb = new EntityType(new DtText("Person"));
        assertFalse(typea.equals(typeb));
        typeb = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"));
        assertTrue(typea.equals(typeb));
        typeb = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"),
                new DtText("1.0"));
        assertFalse(typea.equals(typeb));

        typea = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"),
                new DtText("1.0"));
        typeb = new EntityType(new DtText("Person"));
        assertFalse(typea.equals(typeb));
        typeb = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"));
        assertFalse(typea.equals(typeb));
        typeb = new EntityType(new DtText("{Person"), 
                new DtText("org.openvpms"),
                new DtText("1.0"));
        assertTrue(typea.equals(typeb));
        
        // test against null
        assertFalse(typea.equals(null));
        
        // test for reference equality
        assertTrue(typea.equals(typea));
    }
    
    /**
     * Test entity equality with properties
     */
    public void testEntityEqualityWithProperties()
    throws Exception {
        EntityType typea = new EntityType(new DtText("Person"));
        typea.addProperty(new PropertyType(new DtText("id"), 
                new DtText(DtIdentifier.class.getName())));
        typea.addProperty(new PropertyType(new DtText("name"), 
                new DtText(DtText.class.getName())));

        EntityType typeb = new EntityType(new DtText("Person"));
        typeb.addProperty(new PropertyType(new DtText("id"), 
                new DtText(DtIdentifier.class.getName())));
        typeb.addProperty(new PropertyType(new DtText("name"), 
                new DtText(DtText.class.getName())));
        
        // test for equality
        assertTrue(typea.equals(typeb));
        
        // remove a property from typeb and test for equality
        typeb.removeProperty(new DtText("name"));
        assertFalse(typea.equals(typeb));
        
        // add a new property and test for equality
        typeb.addProperty(new PropertyType(new DtText("firstName"), 
                new DtText(DtText.class.getName())));
        assertFalse(typea.equals(typeb));
        
        // remove all the properties and test for equality
        typeb.removeAllProperties();
        assertFalse(typea.equals(typeb));
        
        // remove all the properties from typea and test for equality
        typea.removeAllProperties();
        assertTrue(typea.equals(typeb));
    }
    
    /**
     * Test adding duplicate property
     */
    public void testDuplicateProperty()
    throws Exception {
        EntityType typea = new EntityType(new DtText("Person"));
        typea.addProperty(new PropertyType(new DtText("id"), 
                new DtText(DtIdentifier.class.getName())));
        
        try {
            typea.addProperty(new PropertyType(new DtText("id"), 
                    new DtText(DtText.class.getName())));
            fail("EntityTypeException should have been thrown");
        } catch (EntityTypeException exception) {
            if (exception.getErrorCode() != 
                EntityTypeException.ErrorCode.PROPERTY_TYPE_ALREADY_EXISTS) {
                fail("Incorrect error code thrown");
            }
        }
    }
    
    /**
     * Test adding an invalid property
     */
    public void testaAddInvalidProperty()
    throws Exception {
        EntityType typea = new EntityType(new DtText("Person"));
        typea.addProperty(new PropertyType(new DtText("id"), 
                new DtText(DtIdentifier.class.getName())));
        
        try {
            typea.addProperty(new PropertyType(new DtText("id"), null)); 
            fail("EntityTypeException should have been thrown");
        } catch (EntityTypeException exception) {
            if (exception.getErrorCode() != 
                EntityTypeException.ErrorCode.INVALID_PROPERTY_TYPE) {
                fail("Incorrect error code thrown");
            }
        }
    }
}
