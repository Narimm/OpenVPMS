package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.edit.PropertyHandler;


/**
 * Default property handler.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultPropertyHandler extends PropertyHandler {

    /**
     * Construct a new <code>DefaultPropertyHandler</code>.
     *
     * @param descriptor the node descriptor.
     */
    public DefaultPropertyHandler(NodeDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     */
    public Object convert(Object object) throws ValidationException {
        return object;
    }

}
