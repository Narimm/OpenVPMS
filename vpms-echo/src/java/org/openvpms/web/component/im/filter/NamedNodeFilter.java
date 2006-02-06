package org.openvpms.web.component.im.filter;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.NodeFilter;


/**
 * Node filter that enables nodes to be excluded by name.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
    public NamedNodeFilter(String ... exclude) {
        _exclude = exclude;
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
        for (int i = 0; i < _exclude.length; ++i) {
            if (_exclude[i].equals(descriptor.getName())) {
                result = false;
                break;
            }
        }
        return result;
    }
}
