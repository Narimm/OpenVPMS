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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Helper for {@link NodeFilter} operations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class FilterHelper {

    /**
     * Prevent construction.
     */
    private FilterHelper() {

    }

    /**
     * Filters a list of descriptors returning only those that must be
     * displayed.
     *
     * @param object the object. May be <code>null</code>
     * @param filter     the filter. If <code>null</code>, all descriptors are
     *                   returned
     * @param descriptor the archetype descriptor
     * @return a list of descriptors
     */
    public static List<NodeDescriptor> filter(IMObject object,
                                              NodeFilter filter,
                                              ArchetypeDescriptor descriptor) {
        List<NodeDescriptor> simple;
        List<NodeDescriptor> complex;
        simple = FilterHelper.filter(object, filter,
                                     descriptor.getSimpleNodeDescriptors());
        complex = FilterHelper.filter(object, filter,
                                      descriptor.getComplexNodeDescriptors());
        List<NodeDescriptor> filtered = new ArrayList<NodeDescriptor>(simple);
        filtered.addAll(complex);
        return filtered;
    }

    /**
     * Filters a list of descriptors returning only those that must be
     * displayed.
     *
     * @param object      the object. May be <code>null</code>
     * @param filter      the filter. If <code>null</code> all descriptors are
     *                    returned.
     * @param descriptors the descriptors to filter
     * @return a list of descriptors
     */
    public static List<NodeDescriptor> filter(
            IMObject object, NodeFilter filter, List<NodeDescriptor> descriptors) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        if (filter == null) {
            result.addAll(descriptors);
        } else {
            for (NodeDescriptor descriptor : descriptors) {
                if (filter.include(descriptor, object)) {
                    result.add(descriptor);
                }
            }
        }
        return result;
    }

    /**
     * Chains a list of filters together.
     *
     * @param filters the filters to chain. May contain <code>null</code>s
     * @return a filter containing <code>filters</code>
     */
    public static ChainedNodeFilter chain(NodeFilter ... filters) {
        ChainedNodeFilter result = new ChainedNodeFilter();
        for (NodeFilter filter : filters) {
            if (filter != null) {
                result.add(filter);
            }
        }
        return result;
    }
}
