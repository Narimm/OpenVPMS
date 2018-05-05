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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertyState;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidObject;

/**
 * Abstract implementation of the {@link PropertyResolver} interface.
 * <p/>
 * Resolves property values given a root and a name of the form <em>propertyName.node1.node2.nodeN</em>.
 * <p/>
 * The <em>propertyName</em> is used to resolve the object.
 * <p/>
 * If the object is an {@code IMObject} or {@code IMObjectReference}, the
 * <em>node*</em> names may be specified to resolve any nodes in the object.
 * The nodes naming follows the convention used by {@link NodeResolver}.
 * <p/>
 * The property name may include '.' characters.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPropertyResolver extends BasePropertyResolver {

    /**
     * Constructs an {@link AbstractPropertyResolver}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public AbstractPropertyResolver(IArchetypeService service, ILookupService lookups) {
        super(service, lookups);
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
    public List<Object> getObjects(String name) {
        List<Object> result;
        Root root = getRoot(name);
        name = root.nodeName;
        if (StringUtils.isEmpty(name)) {
            result = new ArrayList<>();
            if (root.object != null) {
                result.add(root.object);
            }
        } else if (root.object instanceof IMObject) {
            result = getObjects((IMObject) root.object, name);
        } else {
            throw new PropertyResolverException(PropertyResolverException.ErrorCode.InvalidProperty, name);
        }
        return result;
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
        PropertyState result = null;
        Root root = getRoot(name);
        Object object = root.object;
        String nodeName = root.nodeName;
        if (object instanceof IMObject) {
            if (!StringUtils.isEmpty(nodeName)) {
                result = resolve((IMObject) object, nodeName);
            }
        }
        if (result == null) {
            result = new PropertyState(name, object);
        }
        return result;
    }

    /**
     * Resolves the named node.
     *
     * @param name the property name
     * @return the corresponding property
     * @throws PropertyResolverException if the name is invalid
     */
    protected PropertyState resolve(IMObject object, String name) {
        NodeResolver resolver = new NodeResolver(object, getService(), getLookups());
        return resolver.resolve(name);
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws PropertyResolverException if the property doesn't exist
     */
    protected abstract Object get(String name);

    /**
     * Determines if a property exists.
     *
     * @param name the property name
     * @return {@code true} if the property exists
     */
    protected abstract boolean exists(String name);

    /**
     * Returns the root object.
     * @param name the property name
     * @return the root object
     */
    private Root getRoot(String name) {
        int index = 0;
        String objectName = name;
        String nodeName = "";
        Object object = null;
        while (object == null && index != -1) {
            if (exists(objectName)) {
                object = get(objectName);
                if (object instanceof IMObjectReference) {
                    object = resolve((IMObjectReference) object);
                }
                break;
            } else {
                index = name.indexOf('.', index);
                if (index != -1) {
                    objectName = name.substring(0, index);
                    nodeName = name.substring(index + 1);
                    ++index;
                }
            }
        }
        if (index == -1) {
            throw new PropertyResolverException(InvalidObject, name);
        }
        return new Root(object, nodeName);
    }

    private static class Root {

        private final Object object;

        private final String nodeName;

        public Root(Object object, String nodeName) {
            this.object = object;
            this.nodeName = nodeName;
        }

    }

}
