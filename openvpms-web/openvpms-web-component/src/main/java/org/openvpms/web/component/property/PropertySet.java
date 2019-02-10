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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.util.ObjectHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Set of {@link Property} instances that tracks modification of derived values.
 *
 * @author Tim Anderson
 */
public class PropertySet {

    /**
     * The object that the properties belong to. May be {@code null}
     */
    private final IMObject object;

    /**
     * The properties.
     */
    private Map<String, Property> properties = new HashMap<>();

    /**
     * The editable properties.
     */
    private Map<String, Property> editable;

    /**
     * Derived property values.
     */
    private Map<Property, Object> derived = new HashMap<>();

    /**
     * Constructs a {@link PropertySet} for an object and list of properties.
     *
     * @param object     the object
     * @param properties the properties
     */
    public PropertySet(IMObject object, Collection<Property> properties) {
        this.object = object;
        setProperties(properties.toArray(new Property[0]));
    }

    /**
     * Constructs a {@link PropertySet} from a list of properties.
     *
     * @param properties the properties
     */
    public PropertySet(Collection<Property> properties) {
        this(properties.toArray(new Property[0]));
    }

    /**
     * Constructs a {@link PropertySet} from a list of properties.
     *
     * @param properties the properties
     */
    public PropertySet(Property... properties) {
        object = null;
        setProperties(properties);
    }

    /**
     * Returns the named property.
     *
     * @param name the name
     * @return the property corresponding to {@code name}, or {@code null} if none exists
     */
    public Property get(String name) {
        return properties.get(name);
    }

    /**
     * Returns a property given its descriptor.
     *
     * @param descriptor the descriptor
     * @return the property corresponding to {@code descriptor}, or {@code null} if none exists
     */
    public Property get(NodeDescriptor descriptor) {
        return get(descriptor.getName());
    }

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    public Collection<Property> getProperties() {
        return properties.values();
    }

    /**
     * Returns the editable properties.
     * <p/>
     * These are the non-hidden, modifiable properties.
     *
     * @return the editable properties
     */
    public Collection<Property> getEditable() {
        if (editable == null) {
            editable = new HashMap<>();
            for (Property property : getProperties()) {
                if (!property.isHidden() && !property.isReadOnly() && !property.isDerived()) {
                    editable.put(property.getName(), property);
                }
            }
        }
        return editable.values();
    }

    /**
     * Determines if any of the properties have been modified.
     *
     * @return {@code true} if at least one property has been modified
     */
    public boolean isModified() {
        for (Property property : getProperties()) {
            if (property.isModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears the modified status of all properties.
     */
    public void clearModified() {
        for (Property property : getProperties()) {
            property.clearModified();
        }
    }

    /**
     * Updates derived properties. Any derived property that has changed
     * since the last call will notify their registered listeners.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void updateDerivedProperties() {
        if (object != null) {
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            service.deriveValues(object);

            for (Property property : derived.keySet()) {
                Object old = derived.get(property);
                Object now = property.getValue();
                if (!ObjectHelper.equals(old, now)) {
                    derived.put(property, now);
                    property.refresh();
                }
            }
        }
    }

    /**
     * Initialises this with the set of properties.
     *
     * @param properties the properties
     */
    private void setProperties(Property[] properties) {
        for (Property property : properties) {
            this.properties.put(property.getName(), property);
            if (property.isDerived()) {
                derived.put(property, property.getValue());
            }
        }
        if (object != null && !derived.isEmpty()) {
            ModifiableListener listener = this::updateDerivedProperties;
            for (Property property : properties) {
                property.addModifiableListener(listener);
            }
        }
    }

    /**
     * Updates derived properties, if the source of the update isn't a derived property itself.
     *
     * @param source the property that triggered the update
     */
    private void updateDerivedProperties(Modifiable source) {
        if (source instanceof Property && !((Property) source).isDerived()) {
            updateDerivedProperties();
        }
    }

}
