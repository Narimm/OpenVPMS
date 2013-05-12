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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.admin.lookup;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;

import java.util.Collections;
import java.util.List;


/**
 * Layout strategy for <em>lookup.reportMacro</em> lookups.
 * <p/>
 * This displays the report node with the other nodes - it would normally be treated as a 'complex' node.
 *
 * @author Tim Anderson
 */
public class ReportMacroLookupLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Returns the 'simple' nodes.
     *
     * @param archetype the archetype
     * @return the simple nodes
     */
    @Override
    protected List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
        return archetype.getAllNodeDescriptors();
    }

    /**
     * Returns the 'complex' nodes.
     *
     * @param archetype the archetype
     * @return the complex nodes
     */
    @Override
    protected List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype) {
        return Collections.emptyList();
    }
}
