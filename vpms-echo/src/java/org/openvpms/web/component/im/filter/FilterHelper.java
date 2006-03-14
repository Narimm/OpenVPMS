package org.openvpms.web.component.im.filter;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;


/**
 * Helper for {@link NodeFilter} operations.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
     * @param filter     the filter. If <code>null</code>, all descriptors are
     *                   returned
     * @param descriptor the archetype descriptor
     * @return a list of descriptors
     */
    public static List<NodeDescriptor> filter(NodeFilter filter,
                                              ArchetypeDescriptor descriptor) {
        List<NodeDescriptor> simple;
        List<NodeDescriptor> complex;
        simple = FilterHelper.filter(filter,
                                     descriptor.getSimpleNodeDescriptors());
        complex = FilterHelper.filter(filter,
                                      descriptor.getComplexNodeDescriptors());
        List<NodeDescriptor> filtered = new ArrayList<NodeDescriptor>(simple);
        filtered.addAll(complex);
        return filtered;
    }

    /**
     * Filters a list of descriptors returning only those that must be
     * displayed.
     *
     * @param filter      the filter. If <code>null</code> all descriptors are
     *                    returned.
     * @param descriptors the descriptors to filter
     * @return a list of descriptors
     */
    public static List<NodeDescriptor> filter(
            NodeFilter filter, List<NodeDescriptor> descriptors) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        if (filter == null) {
            result.addAll(descriptors);
        } else {
            for (NodeDescriptor descriptor : descriptors) {
                if (filter.include(descriptor)) {
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
