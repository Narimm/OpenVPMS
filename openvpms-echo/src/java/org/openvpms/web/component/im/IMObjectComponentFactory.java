package org.openvpms.web.component.im;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Factory for creating components for displaying {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectComponentFactory {

    /**
     * Create a component to display the supplied object.
     *
     * @param object     the object to display
     * @param descriptor the object's descriptor
     * @return a component to display <code>object</code>
     */
    Component create(IMObject object, NodeDescriptor descriptor);
}
