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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.archetype;

import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface AssertionTypeDescriptorDO extends DescriptorDO {
    /**
     * @return Returns the actionTypes.
     */
    Set<ActionTypeDescriptorDO> getActionTypes();

    /**
     * Add an action type.
     *
     * @param actionType the action type to add
     */
    void addActionType(ActionTypeDescriptorDO actionType);

    /**
     * Retrieves the named action type.
     *
     * @param name the action name
     * @return the corresponding action type, or <tt>null</tt> if none is found
     */
    ActionTypeDescriptorDO getActionType(String name);

    /**
     * Remove the specified action type.
     *
     * @param actionType the action type to remove
     */
    void removeActionType(ActionTypeDescriptorDO actionType);

    /**
     * @return Returns the propertyArchetype.
     */
    String getPropertyArchetype();

    /**
     * @param propertyArchetype The propertyArchetypeQName to set.
     */
    void setPropertyArchetype(String propertyArchetype);
}
