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
 *
 *  $Id$
 */

package org.openvpms.web.component.im.filter;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Node filter that enables nodes to be excluded by value.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ValueNodeFilter implements NodeFilter {

    /**
     * The node name.
     */
    private final String _name;

    /**
     * The node value.
     */
    private final Object _value;


    /**
     * Construct a new <code>ValueNodeFilter</code>.
     *
     * @param name  the name of the node to filter
     * @param value the value to exclude the the node on
     */
    public ValueNodeFilter(String name, Object value) {
        _name = name;
        _value = value;
    }

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @param object     the object. May be <code>null</code>
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor, IMObject object) {
        boolean result;
        if (object == null) {
            result = true;
        } else if (!descriptor.getName().equals(_name)) {
            result = true;
        } else {
            Object other = descriptor.getValue(object);
            if (ObjectUtils.equals(_value, other)) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }
}
