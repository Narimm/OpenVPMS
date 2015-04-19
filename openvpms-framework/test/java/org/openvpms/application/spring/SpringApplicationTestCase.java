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

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * This test cases is used to bring up a complete system configured in a spring
 * application context.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
@ContextConfiguration("openvpms-app-appcontext.xml")
public class SpringApplicationTestCase extends AbstractJUnit4SpringContextTests {

    /**
     * This will just bring up the container as configured by the application context.
     */
    @Test
    public void testApplication() {
        IArchetypeService service = (IArchetypeService) applicationContext.getBean("archetypeService");
        assertNotNull(service);
    }
}
