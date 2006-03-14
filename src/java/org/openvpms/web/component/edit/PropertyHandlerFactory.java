package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.edit.PropertyHandler;


/**
 * Factory for {@link PropertyHandler} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PropertyHandlerFactory {

    /**
     * Create a new node validator.
     *
     * @param descriptor the node descriptor
     */
    public static PropertyHandler create(NodeDescriptor descriptor) {
        PropertyHandler result;
        if (descriptor.isNumeric()) {
            result = new NumericPropertyHandler(descriptor);
        } else {
            result = new DefaultPropertyHandler(descriptor);
        }
        return result;
    }
}
