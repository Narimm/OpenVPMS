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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.util;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.PropertyResolver;

/**
 * The state of a property determined by a {@link PropertyResolver}.
 *
 * @author Tim Anderson
 */
public class PropertyState {

    /**
     * The parent of the node, or {@code null} if the parent can't be determined.
     */
    private final IMObject parent;

    /**
     * The archetype descriptor of {@code parent}.
     */
    private final ArchetypeDescriptor archetype;

    /**
     * The property name.
     */
    private final String name;

    /**
     * The property value.
     */
    private final Object value;

    /**
     * The node descriptor corresponding to the property. May be {@code null}
     */
    private final NodeDescriptor node;

    /**
     * Default constructor.
     */
    public PropertyState() {
        this(null, null, null, null, null);
    }

    /**
     * Constructs a {@link PropertyState} for a property.
     *
     * @param name  the property name. May be {@code null}
     * @param value the property value. May be {@code null}
     */
    public PropertyState(String name, Object value) {
        this(null, null, name, null, value);
    }

    /**
     * Constructs a {@link PropertyState}.
     *
     * @param parent    the parent object, or {@code null} if there is no parent.
     * @param archetype the archetype descriptor of the parent, or {@code null} if there is no parent
     * @param name      the property name, or the leaf name corresponding to the last element in a composite name
     * @param node      the leaf node descriptor corresponding to the last element in the name. May be {@code null}
     * @param value     the property value. May be {@code null}
     */
    public PropertyState(IMObject parent, ArchetypeDescriptor archetype,
                         String name, NodeDescriptor node, Object value) {
        this.parent = parent;
        this.archetype = archetype;
        this.name = name;
        this.node = node;
        this.value = value;
    }

    /**
     * Returns the parent of the node.
     *
     * @return the parent of the node, or {@code null} if it couldn't be determined
     */
    public IMObject getParent() {
        return parent;
    }

    /**
     * Returns the archetype of the parent of the node.
     *
     * @return the parent archetype. May be {@code null}
     */
    public ArchetypeDescriptor getParentArchetype() {
        return archetype;
    }

    /**
     * Returns the property name. For composite names, this corresponds to the last element.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * The node descriptor associated with the property.
     *
     * @return the node descriptor, or {@code null} if the property isn't associated with a node
     */
    public NodeDescriptor getNode() {
        return node;
    }

    /**
     * Returns the property value.
     *
     * @return the property value. May be {@code null}
     */
    public Object getValue() {
        return value;
    }

}

