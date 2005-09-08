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


package org.openvpms.component.business.service.archetype;


// jxpath
import org.apache.commons.jxpath.JXPathContext;

// openvpms-framework
import org.openvpms.component.business.domain.im.EntityIdentity;
import org.openvpms.component.system.common.test.BaseTestCase;
import org.openvpms.component.business.service.archetype.JXPathGenericObjectCreationFactory;

/**
 * Use JXPath to create and domain objects
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class JXPathObjectBuilderTestCase extends BaseTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JXPathObjectBuilderTestCase.class);
    }

    /**
     * Constructor for JXPathObjectBuilderTestCase.
     * @param name
     */
    public JXPathObjectBuilderTestCase(String name) {
        super(name);
    }

    /**
     * Test creating a default {@link EntityIdentity} instance.
     */
    public void testEntityIdentityCreationWithDetails()
    throws Exception {
       EntityIdentity original = new EntityIdentity();
       JXPathContext context = JXPathContext.newContext(original);
       context.setFactory(new JXPathGenericObjectCreationFactory());
       context.createPath("/details");
       
       // retrieve the base value
       EntityIdentity entityIdentity = (EntityIdentity)context.getValue("/");
       assertTrue(entityIdentity.getDetails() != null);
       
       // retrieve
    }
    
    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

}

