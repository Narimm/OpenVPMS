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

package org.openvpms.web.component.im.view.layout;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.util.ArrayList;
import java.util.List;


/**
 * Participation layout strategy. This filters out the "act" node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParticipationLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Returns the 'simple' nodes. This includes the "entity" node, if present.
     *
     * @param archetype the archetype
     * @return the simple nodes
     */
    @Override
    protected List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor node : archetype.getAllNodeDescriptors()) {
            if (!node.isComplexNode() || node.getName().equals("entity")) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Returns the 'complex' nodes. This excludes the "entity" node.
     *
     * @param archetype the archetype
     * @return the complex nodes
     */
    @Override
    protected List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor node : super.getComplexNodes(archetype)) {
            if (!node.getName().equals("entity")) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters
     * the "act" node.
     *
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(LayoutContext context) {
        NodeFilter filter = new NamedNodeFilter("act");
        return getNodeFilter(context, filter);
    }
}
