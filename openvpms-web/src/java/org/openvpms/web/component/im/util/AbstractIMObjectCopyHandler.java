/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Abstract implementation of the {@link IMObjectCopyHandler} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
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
        boolean result = !node.getClazz().equals(DynamicAttributeMap.class);
        if (result && !source) {
            result = (!node.isReadOnly() && !node.isDerived());
        }
        return result;
    }
}
