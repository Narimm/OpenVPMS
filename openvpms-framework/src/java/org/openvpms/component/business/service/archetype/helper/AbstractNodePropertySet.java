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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Set;

import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.NodeDescriptorNotFound;

/**
 * Implementation of {@link PropertySet} for nodes.
 *
 * @author Tim Anderson
 */
public abstract class AbstractNodePropertySet extends AbstractPropertySet {

    /**
     * The object.
     */
    private final IMObject object;

    /**
     * The archetype descriptor.
     */
    private ArchetypeDescriptor archetype;

    /**
     * Constructs an {@link AbstractNodePropertySet}.
     *
     * @param object the object
     */
    public AbstractNodePropertySet(IMObject object) {
        this.object = object;
    }

    /**
     * Returns the object.
     *
     * @return the object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the property names.
     *
     * @return the property names
     */
    public Set<String> getNames() {
        return getArchetype().getNodeDescriptors().keySet();
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws IMObjectBeanException if the descriptor doesn't exist
     */
    public Object get(String name) {
        NodeDescriptor node = getNode(name);
        return node.getValue(object);
    }

    /**
     * Sets the value of a property.
     *
     * @param name  the propery name
     * @param value the property value
     * @throws IMObjectBeanException if the descriptor doesn't exist
     * @throws OpenVPMSException     if the property cannot be set
     */
    public void set(String name, Object value) {
        NodeDescriptor node = getNode(name);
        node.setValue(object, value);
    }

    /**
     * Returns the archetype descriptor.
     *
     * @return the archetype descriptor
     * @throws IMObjectBeanException if the archetype does not exist
     */
    public ArchetypeDescriptor getArchetype() {
        if (archetype == null) {
            String type = object.getArchetype();
            archetype = getArchetypeService().getArchetypeDescriptor(type);
            if (archetype == null) {
                throw new IMObjectBeanException(ArchetypeNotFound, type);
            }
        }
        return archetype;
    }

    /**
     * Returns a node descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to {@code name}
     * @throws IMObjectBeanException if the descriptor doesn't exist
     */
    public NodeDescriptor getNode(String name) {
        NodeDescriptor node = getArchetype().getNodeDescriptor(name);
        if (node == null) {
            String shortName = object.getArchetype();
            throw new IMObjectBeanException(NodeDescriptorNotFound, name, shortName);
        }
        return node;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected abstract IArchetypeService getArchetypeService();
}
