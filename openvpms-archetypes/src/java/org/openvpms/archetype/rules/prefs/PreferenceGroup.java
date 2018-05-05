/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractNodePropertySet;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.HashSet;
import java.util.Set;

/**
 * Preference group.
 *
 * @author Tim Anderson
 */
class PreferenceGroup extends AbstractPropertySet {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The preference properties.
     */
    private final NodePropertySet properties;

    /**
     * Constructs a {@link PreferenceGroup}.
     *
     * @param entity  the preferences group entity
     * @param service the archetype service
     */
    public PreferenceGroup(Entity entity, IArchetypeService service) {
        this.service = service;
        properties = new NodePropertySet(entity);
    }

    /**
     * Returns the property names.
     *
     * @return the property names
     */
    @Override
    public Set<String> getNames() {
        return properties.getNames();
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws OpenVPMSException if the property doesn't exist
     */
    @Override
    public Object get(String name) {
        return properties.get(name);
    }

    /**
     * Sets the value of a property.
     *
     * @param name  the property name
     * @param value the property value
     * @throws OpenVPMSException if the property cannot be set
     */
    public void set(String name, Object value) {
        properties.set(name, value);
    }

    /**
     * Saves the group.
     *
     * @throws OpenVPMSException for any eror
     */
    public void save() {
        service.save(properties.getObject());
    }

    /**
     * Returns the underlying preference group entity.
     *
     * @return the entity
     */
    public Entity getEntity() {
        return (Entity) properties.getObject();
    }

    /**
     * Implementation of {@link PropertySet} for nodes.
     */
    private class NodePropertySet extends AbstractNodePropertySet {

        /**
         * Constructs an {@link NodePropertySet}.
         *
         * @param object the object
         */
        public NodePropertySet(Entity object) {
            super(object);
        }

        /**
         * Returns the property names.
         *
         * @return the property names
         */
        @Override
        public Set<String> getNames() {
            Set<String> result = new HashSet<>();
            for (NodeDescriptor descriptor : getArchetype().getSimpleNodeDescriptors()) {
                if (!descriptor.isHidden() && !descriptor.isReadOnly() && !descriptor.isDerived()) {
                    result.add(descriptor.getName());
                }
            }
            return result;
        }

        /**
         * Returns the archetype service.
         *
         * @return the archetype service
         */
        @Override
        protected IArchetypeService getArchetypeService() {
            return service;
        }
    }

}
