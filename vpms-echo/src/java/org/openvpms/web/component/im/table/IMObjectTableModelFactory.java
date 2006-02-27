package org.openvpms.web.component.im.table;

import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * Factory for {@link IMObjectTableModel} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectTableModelFactory {

    /**
     * Create a new {@link IMObjectTableModel} given a node descriptor.
     *
     * @param descriptor the node descriptor
     * @return a new table model
     */
    public static IMObjectTableModel create(NodeDescriptor descriptor) {
        IMObjectTableModel result;
        List<ArchetypeDescriptor> archetypes;
        archetypes = DescriptorHelper.getArchetypeDescriptors(
                descriptor.getArchetypeRange());
        if (EntityRelationshipTableModel.canHandle(archetypes)) {
            result = new EntityRelationshipTableModel();
        } else {
            result = new IMObjectTableModel();
        }
        return result;
    }
}
