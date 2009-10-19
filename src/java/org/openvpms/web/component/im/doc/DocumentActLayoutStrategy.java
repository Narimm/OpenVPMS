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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;

import java.util.ArrayList;
import java.util.List;


/**
 * Layout strategy for document acts.
 * <p/>
 * This implementation displays any 'document' node as a simple node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActLayoutStrategy extends ActLayoutStrategy {

    /**
     * The document node.
     */
    public static final String DOCUMENT = "document";


    /**
     * Returns the 'simple' nodes.
     * <p/>
     * This implementation always returns the 'document' node as a simple node, if present.
     *
     * @param archetype the archetype
     * @return the simple nodes
     */
    @Override
    protected List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
        List<NodeDescriptor> nodes = new ArrayList<NodeDescriptor>();
        nodes.addAll(super.getSimpleNodes(archetype));
        boolean found = false;
        for (NodeDescriptor node : nodes) {
            String name = node.getName();
            if (DOCUMENT.equals(name)) {
                found = true;
                break;
            }
        }
        if (!found) {
            NodeDescriptor node = archetype.getNodeDescriptor(DOCUMENT);
            if (node != null) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * Returns the 'complex' nodes.
     * <p/>
     * This implementation filters any 'document' node, if present.
     *
     * @param archetype the archetype
     * @return the complex nodes
     */
    @Override
    protected List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype) {
        return filter(null, archetype.getComplexNodeDescriptors(), new NamedNodeFilter(DOCUMENT));
    }

}
