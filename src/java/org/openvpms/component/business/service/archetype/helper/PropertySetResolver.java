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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.PropertyResolverException.ErrorCode.InvalidObject;
import org.openvpms.component.system.common.util.PropertySet;


/**
 * Resolves property values given a root <tt>PropertySet</tt> and a name of
 * the form <em>propertyName.node1.node2.nodeN</em>.
 * <p/>
 * The <em>propertyName</em> is used to resolve the object in set.
 * If the object is an <tt>IMObject</tt> or <tt>IMObjectReference</em>, the
 * <em>node*</em> names may be specified to resolve any nodes in the object.
 * The nodes naming follows the convention used by {@link NodeResolver}.
 * <p/>
 * The property name may include '.' characters.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PropertySetResolver implements PropertyResolver {

    /**
     * The property set.
     */
    private final PropertySet set;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>PropertySetResolver</tt>.
     *
     * @param set     the property set
     * @param service the archetype service
     */
    public PropertySetResolver(PropertySet set, IArchetypeService service) {
        this.set = set;
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
            if (set.getNames().contains(objectName)) {
                object = set.get(objectName);
                if (object instanceof IMObject) {
                    if (!StringUtils.isEmpty(nodeName)) {
                        NodeResolver resolver = new NodeResolver(
                                (IMObject) object, service);
                        object = resolver.getObject(nodeName);
                        break;
                    }
                } else if (object instanceof IMObjectReference) {
                    object = service.get((IMObjectReference) object);
                    if (object != null && !StringUtils.isEmpty(nodeName)) {
                        NodeResolver resolver = new NodeResolver(
                                (IMObject) object, service);
                        object = resolver.getObject(nodeName);
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
}
