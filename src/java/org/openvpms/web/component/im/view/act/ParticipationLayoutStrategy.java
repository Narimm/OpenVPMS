package org.openvpms.web.component.im.view.act;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.util.DescriptorHelper;

/**
 * Participation layout strategy. This displays the entity node.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ParticipationLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object  the object to apply
     * @param factory the component factory
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, IMObjectComponentFactory factory) {
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(object);
        NodeDescriptor descriptor = archetype.getNodeDescriptor("entity");
        return factory.create(object, descriptor);
    }
}
