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
import nextapp.echo2.app.Window;
import org.junit.Before;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.GlobalContext;


/**
 * Abstract base class for tests requiring Spring and Echo2 to be set up.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractAppTest extends ArchetypeServiceTest {

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        ContextApplicationInstance app = new ContextApplicationInstance(new GlobalContext()) {
            /**
             * Switches the current workspace to display an object.
             *
             * @param object the object to view
             */
            @Override
            public void switchTo(IMObject object) {
            }

            /**
             * Switches the current workspace to one that supports a particular archetype.
             *
             * @param shortName the archetype short name
             */
            @Override
            public void switchTo(String shortName) {
            }

            @Override
            public Window init() {
                Window window = new Window();
                return window;
            }
        };
        app.setApplicationContext(applicationContext);
        ApplicationInstance.setActive(app);
        app.doInit();
    }

}
