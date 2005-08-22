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


package org.openvpms.component.business.service.uuid;

import org.openvpms.component.system.test.BaseTestCase;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Exercise the {@link JUGGenerator}
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class JUGGeneratorTestCase extends BaseTestCase {
    /**
     * A reference to the generator
     */
    IUUIDGenerator generator;
    
    /**
     * Main line routine 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUGGeneratorTestCase.class);
    }

    /**
     * Constructor for JUGGeneratorTestCase.
     * 
     * @param name
     *            The name of the test case
     */
    public JUGGeneratorTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        generator = new JUGGenerator(
                UUIDGenerator.getInstance().getDummyAddress().toString());
    }

    /**
     * Test the creation of multiple UUIDs
     */
    public void testMultipleUUIDCreation()
    throws Exception {
        int count = ((Integer)this.getTestData().getTestCaseParameter(
                "testMultipleUUIDCreation", "normal", "generateCount"))
                .intValue();

        for (int index = 0; index < count; index++) {
            String id = generator.nextId();
            debug("Generated id : " + id);
        }
    }
    
    /**
     * Test the creation of multiple UUIDs with a specified prefix
     */
    public void testMultipleUUIDGeneratorWithPrefix()
    throws Exception {
        int count = ((Integer)this.getTestData().getTestCaseParameter(
                "testMultipleUUIDGeneratorWithPrefix", "normal", "generateCount"))
                .intValue();
        String prefix = (String)this.getTestData().getTestCaseParameter(
                "testMultipleUUIDGeneratorWithPrefix", "normal", "prefix");

        for (int index = 0; index < count; index++) {
            String id = generator.nextId(prefix);
            debug("Generated id : " + id);
        }
    }
    
    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        generator = null;
    }

}
