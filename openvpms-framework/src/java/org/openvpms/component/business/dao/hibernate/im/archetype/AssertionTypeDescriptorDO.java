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

import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;

/**
 * This is used to define the assertion type. It is used to map an assertion to
 * its type information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-04-16 16:01:53 +1000 (Wed, 16 Apr 2008) $
 */
public class AssertionTypeDescriptorDO extends DescriptorDO {

    /**
     * A list of well known actions which may be supported by assertions
     */
    public enum Actions {
        create, validate
    }

    /**
     * This is the fully qualified archetype of the object used to collect
     * property information for this assertion type.
     */
    private String propertyArchetype;

    /**
     * A list of actions associated with this assertion type
     */
    private Set<ActionTypeDescriptorDO> actionTypes = new HashSet<ActionTypeDescriptorDO>();

    /**
     * Default constructor
     */
    public AssertionTypeDescriptorDO() {
        setArchetypeId(new ArchetypeId("descriptor.assertionType.1.0"));
    }

    /**
     * @return Returns the actionTypes.
     */
    public Set<ActionTypeDescriptorDO> getActionTypes() {
        return actionTypes;
    }

    /**
     * Add an action type.
     *
     * @param actionType the action type to add
     */
    public void addActionType(ActionTypeDescriptorDO actionType) {
        actionTypes.add(actionType);
    }

    /**
     * Retrieves the named action type.
     *
     * @param name the action name
     * @return the corresponding action type, or <tt>null</tt> if none is found
     */
    public ActionTypeDescriptorDO getActionType(String name) {
        for (ActionTypeDescriptorDO actionType : actionTypes) {
            if (actionType.getName().equals(name)) {
                return actionType;
            }
        }
        return null;
    }

    /**
     * Remove the specified action type.
     *
     * @param actionType the action type to remove
     */
    public void removeActionType(ActionTypeDescriptorDO actionType) {
        actionTypes.remove(actionType);
    }

    /**
     * @return Returns the propertyArchetype.
     */
    public String getPropertyArchetype() {
        return propertyArchetype;
    }

    /**
     * @param propertyArchetype The propertyArchetypeQName to set.
     */
    public void setPropertyArchetype(String propertyArchetype) {
        this.propertyArchetype = propertyArchetype;
    }

    /**
     * @param actionTypes The actionTypes to set.
     */
    protected void setActionTypes(Set<ActionTypeDescriptorDO> actionTypes) {
        this.actionTypes = actionTypes;
    }

}
