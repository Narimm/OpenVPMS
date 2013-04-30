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

package org.openvpms.web.component.im.filter;

import org.apache.commons.lang.ArrayUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.List;


/**
 * Node filter that enables nodes to be included or excluded by name.
 *
 * @author Tim Anderson
 */
public class NamedNodeFilter implements NodeFilter {

    /**
     * Set of nodes to include/exclude.
     */
    private final String[] names;

    /**
     * Determines if nodes are to be included or excluded.
     */
    private final boolean exclude;


    /**
     * Constructs a <tt>NamedNodeFilter</tt>.
     *
     * @param names the names of the nodes to exclude
     */
    public NamedNodeFilter(String... names) {
        this(true, names);
    }

    /**
     * Constructs a <tt>NamedNodeFilter</tt>.
     *
     * @param exclude if {@code true} exclude the named nodes otherwise include them
     * @param names   the names of the nodes to include/exclude
     */
    public NamedNodeFilter(boolean exclude, String... names) {
        this.exclude = exclude;
        this.names = names;
    }

    /**
     * Constructs a <tt>NamedNodeFilter</tt>.
     *
     * @param names the names of the nodes to exclude
     */
    public NamedNodeFilter(List<String> names) {
        this.names = names.toArray(new String[names.size()]);
        this.exclude = true;
    }

    /**
     * Creates a filter to include the specified nodes.
     *
     * @param names the names of the nodes to include
     * @return a new filter
     */
    public static NamedNodeFilter include(String... names) {
        return new NamedNodeFilter(false, names);
    }

    /**
     * Creates a filter to include the specified nodes.
     *
     * @param names the names of the nodes to exclude
     * @return a new filter
     */
    public static NamedNodeFilter exclude(String... names) {
        return new NamedNodeFilter(names);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamedNodeFilter)) {
            return false;
        }
        NamedNodeFilter other = (NamedNodeFilter) obj;
        if (names.length != other.names.length) {
            return false;
        }
        for (String name : names) {
            if (!ArrayUtils.contains(other.names, name)) {
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
     * @return {@code true} if the node should be included; otherwise {@code false}
     */
    public boolean include(NodeDescriptor descriptor, IMObject object) {
        boolean result = exclude;
        for (String name : names) {
            if (name.equals(descriptor.getName())) {
                result = !exclude;
                break;
            }
        }
        return result;
    }
}
