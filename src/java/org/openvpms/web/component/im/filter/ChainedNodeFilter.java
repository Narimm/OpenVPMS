package org.openvpms.web.component.im.filter;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


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
