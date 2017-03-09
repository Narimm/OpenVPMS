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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertyState;

import java.util.List;

import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidNode;
import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidObject;


/**
 * A {@link PropertyResolver} that resolves node values given a root {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class NodeResolver extends BasePropertyResolver {

    /**
     * The root object.
     */
    private final IMObject root;

    /**
     * The archetype descriptor.
     */
    private final ArchetypeDescriptor archetype;

    /**
     * Constructs a {@link NodeResolver}.
     *
     * @param root    the root object
     * @param service the archetype service
     */
    public NodeResolver(IMObject root, IArchetypeService service) {
        this(root, service, null);
    }

    /**
     * Constructs a {@link NodeResolver}.
     *
     * @param root    the root object
     * @param service the archetype service
     * @param lookups the lookup service. May be {@code null}
     */
    public NodeResolver(IMObject root, IArchetypeService service, ILookupService lookups) {
        super(service, lookups);
        this.root = root;
        archetype = service.getArchetypeDescriptor(root.getArchetypeId());
    }

    /**
     * Returns the archetype of the root object.
     *
     * @return the archetype of the root object
     */
    public ArchetypeDescriptor getArchetype() {
        return archetype;
    }

    /**
     * Resolves the named property.
     *
     * @param name the property name
     * @return the corresponding property
     * @throws PropertyResolverException if the name is invalid
     */
    public Object getObject(String name) {
        return resolve(name).getValue();
    }

    /**
     * Returns all objects matching the named property.
     * <p/>
     * Unlike {@link #getObject(String)}, this method handles collections of arbitrary size.
     *
     * @param name the property name
     * @return the corresponding property values
     * @throws PropertyResolverException if the name is invalid
     */
    @Override
    public List<Object> getObjects(String name) {
        return getObjects(root, name);
    }

    /**
     * Resolves the state corresponding to a property.
     *
     * @param name the property name
     * @return the resolved state
     * @throws PropertyResolverException if the name is invalid
     */
    @Override
    public PropertyState resolve(String name) {
        PropertyState state;
        IMObject object = root;
        ArchetypeDescriptor archetype = this.archetype;
        int index;
        while ((index = name.indexOf(".")) != -1) {
            String nodeName = name.substring(0, index);
            NodeDescriptor node = getNode(archetype, nodeName);
            if (node == null) {
                throw new PropertyResolverException(InvalidNode, name, archetype);
            }
            Object value = getValue(object, node, true);
            if (value == null) {
                // object missing.
                object = null;
                break;
            } else if (!(value instanceof IMObject)) {
                throw new PropertyResolverException(InvalidObject, name);
            }
            object = (IMObject) value;
            archetype = getArchetype(object);
            name = name.substring(index + 1);
        }
        if (object != null) {
            state = getLeafPropertyState(object, name, archetype);
        } else {
            state = new PropertyState();
        }
        return state;
    }

}
