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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.archetype.assertion;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Tests the {@link StringAssertions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StringAssertionsTestCase extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Bean wrapping an instance of an <em>lookup.case</em>.
     */
    private IMObjectBean bean;


    /**
     * Verifies that the {@link StringAssertions#propercase)} method is invoked when the <em>propercase</em> assertion
     * is defined for a node.
     */
    public void testProperCase() {
        bean.setValue("name", "king george IV");
        assertEquals("King George Iv", bean.getValue("name"));

        // verify that the proper casing can be overridden if the only difference is a case change
        bean.setValue("name", "King George IV");
        assertEquals("King George IV", bean.getValue("name"));

        // verify the value can be overridden with a different string
        bean.setValue("name", "king george v");
        assertEquals("King George V", bean.getValue("name"));
    }

    /**
     * Verifies that the {@link StringAssertions#uppercase)} method is invoked when the <em>uppercase</em> assertion
     * is defined for a node.
     */
    public void testUppercase() {
        bean.setValue("code", "test code");
        assertEquals("TEST CODE", bean.getValue("code"));
    }

    /**
     * Verifies that the {@link StringAssertions#lowercase)} method is invoked when the <em>lowercase</em> assertion
     * is defined for a node.
     */
    public void testLowercase() {
        bean.setValue("description", "TEST DESCRIPTION");
        assertEquals("test description", bean.getValue("description"));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        IArchetypeService service = (IArchetypeService) applicationContext.getBean("archetypeService");
        Lookup lookup = (Lookup) service.create("lookup.case");
        assertNotNull(lookup);
        bean = new IMObjectBean(lookup);
    }

    /**
     * Returns the spring application context paths.
     *
     * @return the spring application context paths
     */
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

}
