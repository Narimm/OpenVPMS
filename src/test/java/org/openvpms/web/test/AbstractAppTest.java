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
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.component.dialog.PopupDialog;


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
        OpenVPMSApp app = (OpenVPMSApp) applicationContext.getBean("openVPMSApp");
        app.setApplicationContext(applicationContext);
        ApplicationInstance.setActive(app);
        app.doInit();
    }

    /**
     * Helper to click a button on a dialog.
     *
     * @param dialog   the dialog
     * @param buttonId the button identifier
     */
    protected static void fireDialogButton(PopupDialog dialog, String buttonId) {
        Button button = dialog.getButtons().getButton(buttonId);
        assertNotNull(button);
        assertTrue(button.isEnabled());
        button.fireActionPerformed(new ActionEvent(button, button.getActionCommand()));
    }

    protected static <T extends Component> T findComponent(Component component, Class<T> clazz) {
        Component result = (clazz.isAssignableFrom(component.getClass())) ? component : null;
        if (result == null) {
            for (Component child : component.getComponents()) {
                result = findComponent(child, clazz);
                if (result != null) {
                    break;
                }
            }
        }
        return clazz.cast(result);
    }

}
