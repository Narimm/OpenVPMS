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


package org.openvpms.component.system.common.test;


// commons-lang
import junit.framework.TestCase;
import net.wangs.jtestcase.JTestCase;
import org.apache.commons.lang.StringUtils;

/**
 * This calss adds some additional features such as logging, etc which 
 * are necessary for OpenVPMS. All unit test cases must extend this 
 * base class.
 * <p>
 * The base class will also load any data that is required to exercise the
 * test cases. 
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class BaseTestCase extends TestCase {
    /**
     * The name of the file that holds all the test data, which must
     * also reside in the same package as the test case using it
     */
    private static final String TEST_FILE = "test-data.xml";

    /**
     * Data that is relevant to the test case is loaded using jtestcase
     * library.
     */
    protected JTestCase testData;

    /**
     * Delegate to the base class
     */
    public BaseTestCase() {
        super();
    }

    /**
     * Delegate to the base class
     * 
     * @param name
     */
    public BaseTestCase(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.log4j.LoggedTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        setUpTestData();
    }

    /* (non-Javadoc)
     * @see junit.log4j.LoggedTestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        testData = null;
    }

    /**
     * @return Returns the testData.
     */
    protected JTestCase getTestData() {
        return testData;
    }

    /**
     * Return the path to the test data. This is required if the class is 
     * using data driven testing. The default location is a file called
     * test-data.xml, which resides in the same package as the class under
     * test.
     * 
     * @return String
     */
    protected String getPathToTestData() {
        String className = this.getClass().getName();
        String dataFile = className.substring(0, className.lastIndexOf(".") + 1);
        return "target/test-classes/" + StringUtils.replaceChars(dataFile, '.', '/') + TEST_FILE;
    }

    /**
     * Set up the test data file, if the file exists.
     * 
     * @throws Exception
     *            propagate exception to the caller
     */
    protected void setUpTestData()
    throws Exception {
        String dataFile = getPathToTestData();
        testData = new JTestCase(dataFile, this.getClass().getSimpleName());
    }
}
