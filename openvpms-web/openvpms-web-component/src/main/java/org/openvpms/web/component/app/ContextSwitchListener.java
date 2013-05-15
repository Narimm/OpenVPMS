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
 * A listener to change the view.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ContextSwitchListener {

    /**
     * Switches the current view to display an object.
     *
     * @param object the object to view
     */
    void switchTo(IMObject object);

    /**
     * Switches the current view to one that supports a particular archetype.
     *
     * @param shortName the archetype short name
     */
    void switchTo(String shortName);

}
