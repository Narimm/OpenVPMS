package org.openvpms.web.component.im.filter;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.NodeFilter;


/**
 * Basic implementation of the {@link NodeFilter} interface, that enables hidden
 * and optional fields to be included or excluded.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BasicNodeFilter implements NodeFilter {

    /**
     * If <code>true</code> show optional fields, as well as mandatory ones.
     */
    private final boolean _showOptional;

    /**
     * If <code>true</code>, show hidden fields.
     */
    private final boolean _showHidden;


    /**
     * Construct a new <code>BasicNodeFilter</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     * @param showHidden   if <code>true</code> show hidden fields
     */
    public BasicNodeFilter(boolean showOptional, boolean showHidden) {
        _showOptional = showOptional;
        _showHidden = showHidden;
    }

    /**
     * Determines if optional fields should be displayed.
     *
     * @return <code>true</code> if optional fields should be displayed
     */
    public boolean showOptional() {
        return _showOptional;
    }

    /**
     * Determines if hidden fields should be displayed.
     *
     * @return <code>true</code> if hidden fields should be displayed
     */
    public boolean showHidden() {
        return _showHidden;
    }

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor) {
        boolean result = false;
        if (descriptor.isHidden()) {
            if (showHidden()) {
                result = true;
            }
        } else if (showOptional()) {
            result = true;
        } else {
            result = descriptor.isRequired();
        }
        return result;
    }
}
