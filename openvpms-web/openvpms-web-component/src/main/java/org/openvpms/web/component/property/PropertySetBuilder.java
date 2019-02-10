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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.util.Variables;
import org.openvpms.macro.Macros;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A builder for {@link PropertySet} instances.
 * <p/>
 * This allows the default attributes of properties to be overridden.
 *
 * @author Tim Anderson
 */
public class PropertySetBuilder {

    /**
     * The object to build properties for. May be {@code null}
     */
    private IMObject object;

    /**
     * The properties, keyed on name.
     */
    private Map<String, Property> properties = new LinkedHashMap<>();

    /**
     * Constructs a {@link PropertySet} from an object.
     *
     * @param object the object
     */
    public PropertySetBuilder(IMObject object) {
        this(object, null);
    }

    /**
     * Constructs a {@link PropertySet} from an object.
     *
     * @param object  the object
     * @param context the layout context. May be {@code null}
     */
    public PropertySetBuilder(IMObject object, LayoutContext context) {
        this(object, getArchetypeDescriptor(object, context), (context != null) ? context.getVariables() : null);
    }

    /**
     * Constructs a {@link PropertySet} for an object and descriptor.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     * @param variables the variables for macro expansion. May be {@code null}
     */
    public PropertySetBuilder(IMObject object, ArchetypeDescriptor archetype, Variables variables) {
        this.object = object;

        if (archetype == null) {
            throw new IllegalStateException(
                    "No archetype descriptor for object, id=" + object.getId() + ", archetypeId="
                    + object.getArchetypeIdAsString());
        }

        for (NodeDescriptor descriptor : archetype.getAllNodeDescriptors()) {
            Property property = new IMObjectProperty(object, descriptor);
            // for editable string properties, register a transformer that supports macro expansion with variables
            if (property.isString() && !property.isDerived() && !property.isReadOnly()) {
                Macros macros = ServiceHelper.getMacros();
                property.setTransformer(new StringPropertyTransformer(property, true, macros, object, variables));
            }
            properties.put(descriptor.getName(), property);
        }
    }

    /**
     * Marks a property as required.
     *
     * @param name the property name
     * @return this builder
     */
    public PropertySetBuilder setRequired(String name) {
        return setRequired(name, true);
    }

    /**
     * Marks a property as required or optional.
     *
     * @param name     the property name
     * @param required if {@code true}, the property is required, otherwise it is optional
     * @return this builder
     */
    public PropertySetBuilder setRequired(String name, boolean required) {
        Property property = getProperty(name);
        properties.put(name, new DelegatingProperty(property) {
            @Override
            public boolean isRequired() {
                return required;
            }

            @Override
            public int getMinCardinality() {
                int min = super.getMinCardinality();
                if (min == 0 && required && property.isCollection()) {
                    min = 1;
                }
                return min;
            }
        });
        return this;
    }

    /**
     * Marks a property as read-only.
     *
     * @param name the property name
     * @return this builder
     */
    public PropertySetBuilder setReadOnly(String name) {
        return setReadOnly(name, true);
    }

    /**
     * Marks a property as read-only or editable.
     *
     * @param name     the property name
     * @param readOnly if {@code true}, the property is read-only, otherwise it is editable
     * @return this builder
     */
    public PropertySetBuilder setReadOnly(String name, boolean readOnly) {
        Property property = getProperty(name);
        properties.put(name, new DelegatingProperty(property) {
            @Override
            public boolean isReadOnly() {
                return readOnly;
            }
        });
        return this;
    }

    /**
     * Marks a property as hidden.
     *
     * @param name the property name
     * @return this builder
     */
    public PropertySetBuilder setHidden(String name) {
        return setHidden(name, true);
    }

    /**
     * Marks a property as hidden or visible.
     *
     * @param name   the property name
     * @param hidden if {@code true}, the property is hidden, otherwise it is visible
     * @return this builder
     */
    public PropertySetBuilder setHidden(String name, boolean hidden) {
        Property property = getProperty(name);
        properties.put(name, new DelegatingProperty(property) {
            @Override
            public boolean isHidden() {
                return hidden;
            }
        });
        return this;
    }

    /**
     * Marks a property as editable.
     *
     * @param name the property name
     * @return this builder
     */
    public PropertySetBuilder setEditable(String name) {
        Property property = getProperty(name);
        properties.put(name, new DelegatingProperty(property) {
            @Override
            public boolean isReadOnly() {
                return false;
            }

            @Override
            public boolean isHidden() {
                return false;
            }
        });
        return this;
    }

    /**
     * Marks a property as editable.
     * <p/>
     * If the property is a collection property, and is made required, any zero minimum cardinality will be
     * changed to {@code 1}.
     *
     * @param name     the property name
     * @param required if {@code true}, the property is required, otherwise it is optional
     * @return this builder
     */
    public PropertySetBuilder setEditable(String name, boolean required) {
        Property property = getProperty(name);
        properties.put(name, new DelegatingProperty(property) {
            @Override
            public boolean isReadOnly() {
                return false;
            }

            @Override
            public boolean isHidden() {
                return false;
            }

            @Override
            public boolean isRequired() {
                return required;
            }

            @Override
            public int getMinCardinality() {
                int min = super.getMinCardinality();
                if (min == 0 && required && property.isCollection()) {
                    min = 1;
                }
                return min;
            }
        });
        return this;
    }

    /**
     * Builds the property set.
     *
     * @return the new property set
     */
    public PropertySet build() {
        return new PropertySet(object, properties.values());
    }

    /**
     * Returns the named property.
     *
     * @param name the property name
     * @return the corresponding property
     * @throws IllegalArgumentException if the property is not found
     */
    protected Property getProperty(String name) {
        Property property = properties.get(name);
        if (property == null) {
            throw new IllegalArgumentException("Argument 'name' doesnt refer to a valid property: " + name);
        }
        return property;
    }

    /**
     * Returns the archetype descriptor for an object.
     *
     * @param object  the object
     * @param context the layout context. May be {@code null}
     * @return the archetype descriptor for the object
     */
    private static ArchetypeDescriptor getArchetypeDescriptor(IMObject object, LayoutContext context) {
        return (context != null) ? context.getArchetypeDescriptor(object)
                                 : DescriptorHelper.getArchetypeDescriptor(object);
    }
}
