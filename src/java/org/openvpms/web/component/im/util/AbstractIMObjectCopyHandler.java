package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

/**
 * Abstract implementation of the {@link IMObjectCopyHandler} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectCopyHandler
        implements IMObjectCopyHandler {

    /**
     * Determines how a node should be copied.
     *
     * @param source the source node
     * @param target the target archetype
     * @return a node to copy source to, or <code>null</code> if the node
     *         shouldn't be copied
     */
    public NodeDescriptor getNode(NodeDescriptor source,
                                  ArchetypeDescriptor target) {
        NodeDescriptor result = null;
        if (isCopyable(source, true)) {
            result = getTargetNode(source, target);
        }
        return result;
    }

    /**
     * Returns a target node for a given source node.
     *
     * @param source the source node
     * @param target the target archetype
     * @return a node to copy source to, or <code>null</code> if the node
     *         shouldn't be copied
     */
    protected NodeDescriptor getTargetNode(NodeDescriptor source,
                                           ArchetypeDescriptor target) {
        NodeDescriptor result = null;
        NodeDescriptor desc = target.getNodeDescriptor(source.getName());
        if (desc != null && isCopyable(desc, false)) {
            result = desc;
        }
        return result;
    }

    /**
     * Helper to determine if a node is copyable.
     *
     * @param node   the node descriptor
     * @param source if <code>true</code> the node is the source; otherwise its
     *               the target
     * @return <code>true</code> if the node is copyable; otherwise
     *         <code>false</code>
     */
    protected boolean isCopyable(NodeDescriptor node, boolean source) {
        boolean result = !node.isHidden();
        if (result && !source) {
            result = (!node.isReadOnly() && !node.isDerived());
        }
        return result;
    }
}
