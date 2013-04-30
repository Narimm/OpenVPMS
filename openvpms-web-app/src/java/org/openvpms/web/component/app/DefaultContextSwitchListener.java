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
package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Default implementation of the {@link ContextSwitchListener} interface.
 * <p/>
 * This delegates to {@link ContextApplicationInstance}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultContextSwitchListener implements ContextSwitchListener {

    /**
     * Singleton instance.
     */
    public static final DefaultContextSwitchListener INSTANCE = new DefaultContextSwitchListener();

    /**
     * Default constructor.
     */
    private DefaultContextSwitchListener() {
    }

    /**
     * Switches the current view to display an object.
     *
     * @param object the object to view
     */
    public void switchTo(IMObject object) {
        ContextApplicationInstance.getInstance().switchTo(object);
    }

    /**
     * Switches the current view to one that supports a particular archetype.
     *
     * @param shortName the archetype short name
     */
    public void switchTo(String shortName) {
        ContextApplicationInstance.getInstance().switchTo(shortName);
    }
}
