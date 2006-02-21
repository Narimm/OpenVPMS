package org.openvpms.web.component.im.filter;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;


/**
 * Filters nodes rendered by an {@link IMObjectLayoutStrategy}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface NodeFilter {

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor);
}
