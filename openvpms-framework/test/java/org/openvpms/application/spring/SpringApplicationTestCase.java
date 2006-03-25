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
 *  $Id: SpringApplicationTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.application.spring;

// log4j
import java.util.List;

import org.apache.log4j.Logger;

// spring-test
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * This test cases is used to bring up a complete system configured in a spring
 * application context.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class SpringApplicationTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(SpringApplicationTestCase.class);

    /**
     * Holds a reference to the archetype service
     */
    private IArchetypeService service;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SpringApplicationTestCase.class);
    }

    /**
     * Default constructor
     */
    public SpringApplicationTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "org/openvpms/component/business/service/security/memory-security-service-appcontext.xml" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (IArchetypeService) applicationContext
                .getBean("archetypeService");
        assertTrue(service != null);
    }

    /**
     * This will just bring up the container as configured by the application
     * context.
     * 
     */
    public void testApplication() throws Exception {
        long start = System.currentTimeMillis();
        List<IMObject> objects = service.get("entity", "act", null, null, false, 
                null, null).getRows();
        assertTrue(objects != null);
        logger.debug("Time to complete " + (System.currentTimeMillis() - start)
                + "ms");
    }
}
