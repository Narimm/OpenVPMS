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

import org.apache.commons.lang.ArrayUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Node filter that enables nodes to be excluded by name.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NamedNodeFilter implements NodeFilter {

    /**
     * Set of nodes to exclude. May be <code>null</code>.
     */
    private final String[] _exclude;


    /**
     * Construct a new <code>NamedNodeFilter</code>.
     *
     * @param exclude the names of the nodes to exclude
     */
    public NamedNodeFilter(String... exclude) {
        _exclude = exclude;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj argument; <tt>false</tt> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamedNodeFilter)) {
            return false;
        }
        NamedNodeFilter other = (NamedNodeFilter) obj;
        if (_exclude.length != other._exclude.length) {
            return false;
        }
        for (String excluded : _exclude) {
            if (!ArrayUtils.contains(other._exclude, excluded)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @param object     the object
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor, IMObject object) {
        boolean result = true;
        for (String excluded : _exclude) {
            if (excluded.equals(descriptor.getName())) {
                result = false;
                break;
            }
        }
        return result;
    }
}
