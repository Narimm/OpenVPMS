package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Handler to determine how {@link IMObjectCopier} should copy objects.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectCopyHandler {

    /**
     * Determines how {@link IMObjectCopier} should treat an object.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <code>object</code> if the object shouldn't be copied,
     *         <code>null</code> if it should be replaced with
     *         <code>null</code>, or a new instance if the object should be
     *         copied
     */
    IMObject getObject(IMObject object, IArchetypeService service);

    /**
     * Determines how a node should be copied
     *
     * @param source the source node
     * @param target the target archetype
     * @return a node to copy source to, or <code>null</code> if the node
     *         shouldn't be copied
     */
    NodeDescriptor getNode(NodeDescriptor source, ArchetypeDescriptor target);

}
