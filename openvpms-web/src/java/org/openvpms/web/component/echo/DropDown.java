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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.echo;

import nextapp.echo2.app.Component;

/**
 * Workaround for a limitation in the EPNG DropDown class which doesn't size its drop-downs.
 * <p/>
 * This exists to enable {@link PopUpPeer} to be used to specify a modified javascript file,
 * <em>org/openvpms/web/resource/js/popup.js</em>.
 * The binding is specified in <em>META-INF\nextapp\echo2\SynchronizePeerBindings.properties</em>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DropDown extends echopointng.DropDown {

    /**
     * Constructs a <tt>DropDown</tt> with no target component and no drop down component
     */
    public DropDown() {
        super();
    }

    /**
     * Constructs a <tt>DropDown</tt> with the specified target and popup component in place.
     *
     * @param targetComponent   the target component of the drop down
     * @param dropDownComponent the component to be shown in the drop down box
     */
    public DropDown(Component targetComponent, Component dropDownComponent) {
        super(targetComponent, dropDownComponent);
    }
}
