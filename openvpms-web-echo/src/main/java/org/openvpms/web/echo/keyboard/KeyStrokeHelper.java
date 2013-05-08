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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.keyboard;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.spring.SpringApplicationInstance;
import org.springframework.context.ApplicationContext;


/**
 * Key stroke helper.
 *
 * @author Tim Anderson
 */
public class KeyStrokeHelper {

    /**
     * The default key stroke mask.
     */
    private static final KeyStrokeMask DEFAULT_MASK = new KeyStrokeMask();


    /**
     * Reregisters any keystroke listeners.
     *
     * @see KeyStrokeHandler
     */
    public static void reregisterKeyStrokeListeners() {
        Window root = ApplicationInstance.getActive().getDefaultWindow();
        reregisterKeyStrokeListeners(root);

        // if the focus isn't currently on a visisble component, force it focus back onto the first available component,
        // otherwise Firefox doesn't forward key events to the app
        Component component = FocusHelper.getFocus();
        while (component != null && component != root) {
            component = component.getParent();
        }
        if (component != root) {
            FocusHelper.setFocus(root);
        }
    }

    /**
     * Reregisters any keystroke listeners by traversing the component heirarchy
     * looking for components that inplement the {@link KeyStrokeHandler}
     * interface.
     *
     * @param component the root of the component heirarchy to traverse
     * @see KeyStrokeHandler
     */
    public static void reregisterKeyStrokeListeners(Component component) {
        if (component instanceof KeyStrokeHandler) {
            ((KeyStrokeHandler) component).reregisterKeyStrokeListeners();
        }
        for (Component child : component.getComponents()) {
            if (!(child instanceof WindowPane)) {
                reregisterKeyStrokeListeners(child);
            }
        }
    }

    /**
     * Returns the key code for a key, including the configured key stroke
     * mask.
     *
     * @param key the key
     * @return the key code for the key
     */
    public static int getKeyCode(char key) {
        KeyStrokeMask mask = null;
        if (ApplicationInstance.getActive() instanceof SpringApplicationInstance) {
            SpringApplicationInstance app = (SpringApplicationInstance) SpringApplicationInstance.getActive();
            ApplicationContext context = app.getApplicationContext();
            mask = (KeyStrokeMask) context.getBean("keyStrokeMask");
        }
        if (mask == null) {
            mask = DEFAULT_MASK;
        }
        return mask.getKeyCode(key);
    }

}
