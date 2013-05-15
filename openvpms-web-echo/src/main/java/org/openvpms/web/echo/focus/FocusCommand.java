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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.echo.focus;

import nextapp.echo2.app.Component;

import java.lang.ref.WeakReference;


/**
 * Helper to save the current focus state, so it can be restored later.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FocusCommand {

    /**
     * The current focus.
     */
    private WeakReference<Component> focus;


    /**
     * Constructs a <tt>FocusState</tt>.
     * <p/>
     * This saves the current focussed component.
     */
    public FocusCommand() {
        Component focus = FocusHelper.getFocus();
        if (focus != null) {
            this.focus = new WeakReference<Component>(focus);
        }
    }

    /**
     * Restores the focus to the prior component, if it is still available.
     */
    public void restore() {
        Component component = getComponent();
        if (component != null) {
            FocusHelper.setFocus(component);
        }
    }

    /**
     * Returns the component that had the focus.
     *
     * @return the component that had the focus, or <tt>null</tt> if it doesn't exist.
     */
    public Component getComponent() {
        return (focus != null) ? focus.get() : null;
    }

}
