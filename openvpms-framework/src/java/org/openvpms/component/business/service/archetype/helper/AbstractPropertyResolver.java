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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;

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
public abstract class AbstractPropertyResolver implements PropertyResolver {

    /**
     * The archetype service.
     */
    protected final IArchetypeService service;

    /**
     * Constructs an {@code AbstractPropertyResolver}.
     *
     * @param service the archetype service
     */
    public AbstractPropertyResolver(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Resolves the named property.
     *
     * @param name the property name
     * @return the corresponding property
     * @throws PropertyResolverException if the name is invalid
     */
    public Object getObject(String name) {
        int index = 0;
        String objectName = name;
        String nodeName = "";
        Object object = null;
        while (object == null && index != -1) {
            if (exists(objectName)) {
                object = get(objectName);
                if (object instanceof IMObject) {
                    if (!StringUtils.isEmpty(nodeName)) {
                        object = resolve((IMObject) object, nodeName);
                        break;
                    }
                } else if (object instanceof IMObjectReference) {
                    object = service.get((IMObjectReference) object);
                    if (object != null && !StringUtils.isEmpty(nodeName)) {
                        object = resolve((IMObject) object, nodeName);
                    }
                    break;
                } else {
                    break;
                }
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
        return object;
    }

    /**
     * Resolves the named node.
     *
     * @param name the property name
     * @return the corresponding property
     * @throws PropertyResolverException if the name is invalid
     */
    protected Object resolve(IMObject object, String name) {
        NodeResolver resolver = new NodeResolver(object, service);
        return resolver.getObject(name);
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
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }
}
