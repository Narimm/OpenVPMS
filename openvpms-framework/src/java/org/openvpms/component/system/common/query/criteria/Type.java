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

package org.openvpms.component.system.common.query.criteria;

import org.openvpms.component.model.archetype.ArchetypeDescriptor;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.object.IMObject;

import javax.persistence.TupleElement;
import java.util.Map;
import java.util.Set;

/**
 * Manages the type of a {@link TupleElement}.
 *
 * @author Tim Anderson
 */
public class Type<T> {

    /**
     * Boolean type.
     */
    public static final Type<Boolean> BOOLEAN = new Type<>(Boolean.class);

    /**
     * Long type.
     */
    public static final Type<Long> LONG = new Type<>(Long.class);

    /**
     * The Java type.
     */
    private final Class<T> type;

    /**
     * The archetype descriptors associated with this type, or {@code null} if the type is not an {@link IMObject}.
     */
    private final Map<String, ArchetypeDescriptor> descriptors;

    /**
     * The node descriptor, or {@code null} if the type is not associated with a node.
     */
    private final NodeDescriptor node;

    /**
     * Constructs a {@link Type}
     *
     * @param type the Java type
     */
    public Type(Class<T> type) {
        this(type, null, null);
    }

    /**
     * Constructs a {@link Type}.
     *
     * @param type        the Java type
     * @param descriptors the archetype descriptors associated with the type, or {@code null} if the type is not an
     *                    {@link IMObject}
     * @param node        the node descriptor, or {@code null} if the type is not associated with a node
     */
    public Type(Class<T> type, Map<String, ArchetypeDescriptor> descriptors, NodeDescriptor node) {
        this.type = type;
        this.descriptors = descriptors;
        this.node = node;
    }

    /**
     * Returns the Java type.
     *
     * @return the Java type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the archetype descriptors, keyed on archetype.
     *
     * @return the archetype descriptors, or {@code null} if the type is not an {@link IMObject}
     */
    public Map<String, ArchetypeDescriptor> getArchetypeDescriptors() {
        return descriptors;
    }

    /**
     * Returns the archetypes.
     *
     * @return the archetypes, or {@code null} if the type is not an {@link IMObject}
     */
    public Set<String> getArchetypes() {
        return descriptors != null ? descriptors.keySet() : null;
    }

    /**
     * Returns the node descriptor.
     *
     * @return or {@code null} if the type is not associated with a node
     */
    public NodeDescriptor getNode() {
        return node;
    }

}
