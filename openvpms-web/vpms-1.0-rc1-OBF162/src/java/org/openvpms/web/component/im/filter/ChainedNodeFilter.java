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

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * An {@link NodeFilter} that evaluates a list of node filters to determine if a
 * node should be included.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ChainedNodeFilter implements NodeFilter {

    /**
     * The node filters.
     */
    private List<NodeFilter> _filters;

    /**
     * Construct a new <code>ChainedNodeFilter</code>
     */
    public ChainedNodeFilter() {
        this(new NodeFilter[0]);
    }

    /**
     * Construct a new <code>ChainedNodeFilter</code>
     */
    public ChainedNodeFilter(NodeFilter ... filters) {
        _filters = new ArrayList<NodeFilter>(filters.length);
        for (NodeFilter filter : filters) {
            _filters.add(filter);
        }
    }

    /**
     * Add a filter.
     *
     * @param filter the filter to add
     */
    public void add(NodeFilter filter) {
        _filters.add(filter);
    }

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @param object
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor, IMObject object) {
        boolean result = true;
        for (NodeFilter filter : _filters) {
            if (!filter.include(descriptor, object)) {
                result = false;
                break;
            }
        }
        return result;
    }
}
