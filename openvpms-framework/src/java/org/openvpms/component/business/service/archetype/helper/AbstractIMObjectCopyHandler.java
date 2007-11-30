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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Abstract implementation of the {@link IMObjectCopyHandler} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-06-09 04:54:07Z $
 */
public abstract class AbstractIMObjectCopyHandler
        implements IMObjectCopyHandler {

    /**
     * Determines how {@link IMObjectCopier} should treat an object. This
     * implementation always returns a new instance, of the same archetype as
     * <tt>object</tt>.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <tt>object</tt> if the object shouldn't be copied,
     *         <tt>null</tt> if it should be replaced with <tt>null</tt>,
     *         or a new instance if the object should be copied
     */
    public IMObject getObject(IMObject object, IArchetypeService service) {
        return service.create(object.getArchetypeId());
    }

    /**
     * Determines how a node should be copied.
     *
     * @param source the source node
     * @param target the target archetype
     * @return a node to copy source to, or <tt>null</tt> if the node
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
     * @return a node to copy source to, or <tt>null</tt> if the node
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
     * @param source if <tt>true</tt> the node is the source; otherwise its
     *               the target
     * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
     */
    protected boolean isCopyable(NodeDescriptor node, boolean source) {
        boolean result = !node.getName().equals("uid");  // NON-NLS
        if (result && !source) {
            // Removed readOnly check for temporary OBF-115 fix.
//            result = (!node.isReadOnly() && !node.isDerived());
            result = !node.isDerived();
        }
        return result;
    }
}
