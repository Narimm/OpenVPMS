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

package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.property.Property;


/**
 * Factory for creating components for displaying {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IMObjectComponentFactory {

    /**
     * Create a component to display a property.
     *
     * @param property the property to display
     * @param context  the context object
     * @return a component to display <tt>object</tt>
     */
    ComponentState create(Property property, IMObject context);

    /**
     * Create a component to display an object.
     *
     * @param object  the object to display
     * @param context the object's parent. May be <tt>null</tt>
     * @return a component to display <tt>object</tt>
     */
    ComponentState create(IMObject object, IMObject context);
}
