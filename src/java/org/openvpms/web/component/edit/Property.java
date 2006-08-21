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

package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;


/**
 * Property that provides notification on modification.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface Property extends Modifiable {

    /**
     * Set the value of the property.
     *
     * @param value the property value
     * @return <code>true</code> if the value was set
     */
    boolean setValue(Object value);

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    Object getValue();

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor
     */
    NodeDescriptor getDescriptor();

    /**
     * Notify any listeners that they need to refresh and marks this modified.
     */
    void refresh();

}
