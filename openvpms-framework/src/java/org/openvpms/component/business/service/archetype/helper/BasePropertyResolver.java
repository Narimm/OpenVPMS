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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertyState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidNode;
import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidProperty;

/**
 * Base implementation of the {@link PropertyResolver} interface.
 *
 * @author Tim Anderson
 */
public abstract class BasePropertyResolver implements PropertyResolver {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service. May be {@code null}
     */
    private final ILookupService lookups;


    /**
     * Constructs a {@link BasePropertyResolver}.
     *
     * @param service the archetype service.
     * @param lookups the lookup service. May be {@code null}
     */
    public BasePropertyResolver(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service. May be {@code null}
     */
    protected ILookupService getLookups() {
        return lookups;
    }

    /**
     * Returns all objects for the specified property.
     *
     * @param root the root object
     * @param name the property name
     * @return the objects
     */
    protected List<Object> getObjects(IMObject root, String name) {
        List<Object> result = new ArrayList<>();
        List<IMObject> parents = new ArrayList<>();
        if (StringUtils.isEmpty(name)) {
            result.add(root);
        } else {
            parents.add(root);
            int index;
            while ((index = name.indexOf(".")) != -1) {
                List<IMObject> children = new ArrayList<>();
                ArchetypeDescriptor archetype = null;
                for (IMObject parent : parents) {
                    archetype = getArchetype(parent, archetype);
                    String node = name.substring(0, index);
                    NodeDescriptor descriptor = getNode(archetype, node);
                    if (descriptor.isCollection()) {
                        children.addAll(descriptor.getChildren(parent));
                    } else {
                        Object child = getValue(parent, descriptor, true);
                        if (child != null) {
                            if (child instanceof IMObject) {
                                children.add((IMObject) child);
                            } else {
                                // not the last node in the composite node, but not an IMObject
                                throw new PropertyResolverException(InvalidProperty, name);
                            }
                        }
                    }
                }
                parents = children;
                name = name.substring(index + 1);
            }
            ArchetypeDescriptor archetype = null;
            for (IMObject object : parents) {
                archetype = getArchetype(object, archetype);
                NodeDescriptor leafNode = (archetype != null) ? archetype.getNodeDescriptor(name) : null;
                Object value = getLeafValue(object, name, archetype, leafNode);
                if (value != null) {
                    if (value instanceof Collection) {
                        result.addAll((Collection) value);
                    } else {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the state of the last property in the composite property name.
     *
     * @param object    the parent object
     * @param name      the property name
     * @param archetype the archetype descriptor
     * @return the state
     * @throws PropertyResolverException if the property name is invalid
     */
    protected PropertyState getLeafPropertyState(IMObject object, String name, ArchetypeDescriptor archetype) {
        NodeDescriptor leafNode = (archetype != null) ? archetype.getNodeDescriptor(name) : null;
        Object value = getLeafValue(object, name, archetype, leafNode);
        return new PropertyState(object, archetype, name, leafNode, value);
    }

    /**
     * Returns the value of the last property in the composite property name.
     *
     * @param object    the parent object
     * @param name      the property name
     * @param archetype the archetype descriptor
     * @param node      the node descriptor. May be {@code null} if the name doesn't correspond to a node
     * @return the value. May be {@code null}
     * @throws PropertyResolverException if the property name is invalid
     */
    protected Object getLeafValue(IMObject object, String name, ArchetypeDescriptor archetype, NodeDescriptor node) {
        Object value;
        if (node == null) {
            if ("displayName".equals(name) && archetype != null) {
                value = archetype.getDisplayName();
            } else if ("shortName".equals(name)) {
                value = object.getArchetypeId().getShortName();
            } else if ("uid".equals(name)) {
                // legacy...
                value = object.getId();
            } else if (object instanceof Lookup) {
                // local lookup
                if ("name".equals(name)) {
                    value = object.getName();
                } else if ("code".equals(name)) {
                    value = ((Lookup) object).getCode();
                } else {
                    throw new PropertyResolverException(InvalidNode, name, object.getArchetypeId().getShortName());
                }
            } else {
                throw new PropertyResolverException(InvalidProperty, name);
            }
        } else {
            value = getValue(object, node, false);
        }
        return value;
    }

    /**
     * Returns the value of a node, converting any object references or single element arrays to their corresponding
     * IMObject instance.
     * <p>
     * If the lookup service wasn't specified at construction, lookups will not be resolved.
     *
     * @param parent         the parent object
     * @param descriptor     the node descriptor
     * @param resolveLookups if {@code true}, retrieve lookups, rather than return their codes
     */
    protected Object getValue(IMObject parent, NodeDescriptor descriptor, boolean resolveLookups) {
        Object result;
        if (descriptor.isObjectReference()) {
            result = resolve(parent, descriptor);
        } else if (descriptor.isCollection()) {
            List<IMObject> values = descriptor.getChildren(parent);
            int size = values.size();
            if (size == 0) {
                result = null;
            } else if (size == 1) {
                result = values.get(0);
            } else {
                result = values;
            }
        } else {
            result = descriptor.getValue(parent);
            if (result != null && resolveLookups && lookups != null && descriptor.isLookup()) {
                LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, lookups);
                result = assertion.getLookup(parent, result.toString());
            }
        }
        return result;
    }

    /**
     * Resolves an object reference.
     *
     * @param ref the reference. May be {@code null}
     * @return the corresponding object, or {@code null} if it is not found
     */
    protected IMObject resolve(IMObjectReference ref) {
        return ref != null ? service.get(ref) : null;
    }

    /**
     * Returns the archetype descriptor for an object.
     *
     * @param object the object
     * @return the archetype descriptor
     */
    protected ArchetypeDescriptor getArchetype(IMObject object, ArchetypeDescriptor existing) {
        if (existing != null && existing.getType().equals(object.getArchetypeId())) {
            return existing;
        }
        return getArchetype(object);
    }

    /**
     * Returns the archetype descriptor for an object.
     *
     * @param object the object
     * @return the archetype descriptor
     */
    protected ArchetypeDescriptor getArchetype(IMObject object) {
        return service.getArchetypeDescriptor(object.getArchetypeId());
    }

    /**
     * Returns a node descriptor for the given name.
     *
     * @param archetype the archetype
     * @param name      the node name
     * @return the descriptor
     * @throws PropertyResolverException if the name is invalid
     */
    protected NodeDescriptor getNode(ArchetypeDescriptor archetype, String name) {
        NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
        if (descriptor == null) {
            throw new PropertyResolverException(InvalidNode, name, archetype.getType().getShortName());
        }
        return descriptor;
    }

    /**
     * Resolve a reference.
     *
     * @param parent     the parent object
     * @param descriptor the reference descriptor
     */
    private IMObject resolve(IMObject parent, NodeDescriptor descriptor) {
        IMObjectReference ref = (IMObjectReference) descriptor.getValue(parent);
        return resolve(ref);
    }

}
