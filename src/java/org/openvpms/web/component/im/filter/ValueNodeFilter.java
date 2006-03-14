package org.openvpms.web.component.im.filter;

import org.apache.commons.lang.ObjectUtils;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Node filter that enables nodes to be excluded by value.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ValueNodeFilter implements NodeFilter {

    /**
     * The node name.
     */
    private final String _name;

    /**
     * The node value.
     */
    private final Object _value;


    /**
     * Construct a new <code>ValueNodeFilter</code>.
     *
     * @param name  the name of the node to filter
     * @param value the value to exclude the the node on
     */
    public ValueNodeFilter(String name, Object value) {
        _name = name;
        _value = value;
    }

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @param object     the object. May be <code>null</code>
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor, IMObject object) {
        boolean result;
        if (object == null) {
            result = true;
        } else if (!descriptor.getName().equals(_name)) {
            result = true;
        } else {
            Object other = descriptor.getValue(object);
            if (ObjectUtils.equals(_value, other)) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }
}
