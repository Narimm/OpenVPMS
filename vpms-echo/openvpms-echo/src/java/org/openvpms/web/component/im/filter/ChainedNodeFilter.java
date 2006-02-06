package org.openvpms.web.component.im.filter;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.NodeFilter;


/**
 * An {@link NodeFilter} that evaluates a list of node filters to determine if a
 * node should be included.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ChainedNodeFilter implements NodeFilter {

    /**
     * The node filters.
     */
    private List<NodeFilter> _filters = new ArrayList<NodeFilter>();


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
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor) {
        boolean result = true;
        for (NodeFilter filter : _filters) {
            if (!filter.include(descriptor)) {
                result = false;
                break;
            }
        }
        return result;
    }
}
