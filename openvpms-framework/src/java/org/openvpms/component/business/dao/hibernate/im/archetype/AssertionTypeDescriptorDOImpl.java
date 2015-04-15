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
 * Data object interface corresponding to the {@link AssertionTypeDescriptorDO}
 * class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-04-16 16:01:53 +1000 (Wed, 16 Apr 2008) $
 */
public class AssertionTypeDescriptorDOImpl extends DescriptorDOImpl
        implements AssertionTypeDescriptorDO {

    /**
     * A list of well known actions which may be supported by assertions.
     */
    public enum Actions {
        create, validate, set
    }

    /**
     * The fully qualified archetype of the object used to collect property
     * information for this assertion type.
     */
    private String propertyArchetype;

    /**
     * A list of actions associated with this assertion type.
     */
    private Set<ActionTypeDescriptorDO> actionTypes
            = new HashSet<ActionTypeDescriptorDO>();


    /**
     * Default constructor.
     */
    public AssertionTypeDescriptorDOImpl() {
        setArchetypeId(new ArchetypeId("descriptor.assertionType.1.0"));
    }

    /**
     * Returns the action types.
     *
     * @return the action types
     */
    public Set<ActionTypeDescriptorDO> getActionTypes() {
        return actionTypes;
    }

    /**
     * Add an action type.
     *
     * @param type the action type to add
     */
    public void addActionType(ActionTypeDescriptorDO type) {
        actionTypes.add(type);
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
     * Removes the specified action type.
     *
     * @param type the action type to remove
     */
    public void removeActionType(ActionTypeDescriptorDO type) {
        actionTypes.remove(type);
    }

    /**
     * Returns the property archetype.
     *
     * @return the property archetype
     */
    public String getPropertyArchetype() {
        return propertyArchetype;
    }

    /**
     * Returns the property archetype.
     *
     * @param propertyArchetype the property archetype
     */
    public void setPropertyArchetype(String propertyArchetype) {
        this.propertyArchetype = propertyArchetype;
    }

    /**
     * Sets the action types.
     *
     * @param types the action types to set.
     */
    protected void setActionTypes(Set<ActionTypeDescriptorDO> types) {
        this.actionTypes = types;
    }

}
