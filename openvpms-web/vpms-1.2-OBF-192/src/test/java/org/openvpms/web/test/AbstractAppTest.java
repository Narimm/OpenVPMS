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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.test;

import nextapp.echo2.app.ApplicationInstance;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import org.openvpms.web.app.OpenVPMSApp;


/**
 * Abstract base class for tests requiring Spring and Echo2 to be set up.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractAppTest
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        OpenVPMSApp app = (OpenVPMSApp) applicationContext.getBean("openVPMSApp");
        app.setApplicationContext(applicationContext);
        ApplicationInstance.setActive(app);
        app.doInit();
    }
}
