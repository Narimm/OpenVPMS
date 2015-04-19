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

package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;


/**
 * Layout strategy for <em>lookup.reportMacro</em> lookups.
 * <p/>
 * This displays the report node with the other nodes - it would normally be treated as a 'complex' node.
 *
 * @author Tim Anderson
 */
public class ReportMacroLookupLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Treat the report node as a simple node.
     */
    private static ArchetypeNodes NODES = new ArchetypeNodes().simple("report");

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return NODES;
    }
}
