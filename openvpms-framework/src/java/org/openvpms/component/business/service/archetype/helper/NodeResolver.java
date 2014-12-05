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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.PropertyState;

import java.util.List;

import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidObject;
import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidProperty;


/**
 * A {@link PropertyResolver} that resolves node values given a root {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class NodeResolver implements PropertyResolver {

    /**
     * The root object.
     */
    private final IMObject root;

    /**
     * The archetype descriptor.
     */
    private final ArchetypeDescriptor archetype;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(NodeResolver.class);


    /**
     * Constructs a {@link NodeResolver}.
     *
     * @param root    the root object
     * @param service the archetype service
     */
    public NodeResolver(IMObject root, IArchetypeService service) {
        this.root = root;
        archetype = service.getArchetypeDescriptor(root.getArchetypeId());
        this.service = service;
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
     * Resolves the state corresponding to a property.
     *
     * @param name the property name
     * @return the resolved state
     * @throws PropertyResolverException if the name is invalid
     */
    @Override
    @SuppressWarnings({"deprecation"})
    public PropertyState resolve(String name) {
        PropertyState state;
        IMObject object = root;
        ArchetypeDescriptor archetype = this.archetype;
        int index;
        while ((index = name.indexOf(".")) != -1) {
            String nodeName = name.substring(0, index);
            NodeDescriptor node = archetype.getNodeDescriptor(nodeName);
            if (node == null) {
                throw new NodeResolverException(InvalidProperty, name);
            }
            Object value = getValue(object, node);
            if (value == null) {
                // object missing.
                object = null;
                break;
            } else if (!(value instanceof IMObject)) {
                throw new NodeResolverException(InvalidObject, name);
            }
            object = (IMObject) value;
            archetype = service.getArchetypeDescriptor(
                    object.getArchetypeId());
            name = name.substring(index + 1);
        }
        if (object != null) {
            NodeDescriptor leafNode = archetype.getNodeDescriptor(name);
            Object value;
            if (leafNode == null) {
                if ("displayName".equals(name)) {
                    value = archetype.getDisplayName();
                } else if ("shortName".equals(name)) {
                    value = object.getArchetypeId().getShortName();
                } else if ("uid".equals(name)) {
                    value = object.getId();
                } else {
                    throw new NodeResolverException(InvalidProperty, name);
                }
            } else {
                value = getValue(object, leafNode);
            }
            state = new PropertyState(object, archetype, name, leafNode, value);
        } else {
            state = new PropertyState();
        }
        return state;
    }

    /**
     * Returns the value of a node, converting any object references or
     * single element arrays to their corresponding IMObject instance.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     */
    private Object getValue(IMObject parent, NodeDescriptor descriptor) {
        Object result;
        if (descriptor.isObjectReference()) {
            result = getObject(parent, descriptor);
        } else if (descriptor.isCollection() &&
                   descriptor.getMaxCardinality() == 1) {
            List<IMObject> values = descriptor.getChildren(parent);
            result = (!values.isEmpty()) ? values.get(0) : null;
        } else {
            result = descriptor.getValue(parent);
        }
        return result;
    }

    /**
     * Resolve a reference.
     *
     * @param parent     the parent object
     * @param descriptor the reference descriptor
     */
    private IMObject getObject(IMObject parent, NodeDescriptor descriptor) {
        IMObjectReference ref = (IMObjectReference) descriptor.getValue(parent);
        if (ref != null) {
            try {
                return service.get(ref);
            } catch (OpenVPMSException exception) {
                log.warn(exception, exception);
            }
        }
        return null;
    }

}
