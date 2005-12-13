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
 *  $Id: AuditServiceTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.audit;


// commons-lang
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.service.archetype.IArchetypeService;

// openvpms-test-component
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test the
 * {@link org.openvpms.component.business.service.audit.AuditService}
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class AuditServiceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(AuditServiceTestCase.class);

    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService service;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AuditServiceTestCase.class);
    }

    /**
     * Default constructor
     */
    public AuditServiceTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "org/openvpms/component/business/service/audit/audit-service-appcontext.xml" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        this.service = (IArchetypeService)applicationContext
                .getBean("archetypeService");
    }
    
    /**
     * Test that we can successfully startup
     */
    public void testSuccessfulStartUp()
    throws Exception {
        // no op
    }
}
