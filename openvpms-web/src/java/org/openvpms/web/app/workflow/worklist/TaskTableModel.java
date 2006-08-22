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

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Task table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskTableModel extends DescriptorTableModel<Act> {

    /**
     * Creates a new <code>TaskTableModel</code>.
     *
     * @param context the layout context
     */
    public TaskTableModel(LayoutContext context) {
        super(new String[]{"act.customerTask"}, context);
    }

    /**
     * Returns a filtered list of descriptors for an archetype.
     *
     * @param archetype the archetype
     * @param context   the layout context
     * @return a filtered list of descriptors for the archetype
     */
    @Override
    protected List<NodeDescriptor> getDescriptors(
            ArchetypeDescriptor archetype, LayoutContext context) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        addDescriptor("status", archetype, result);
        addDescriptor("taskType", archetype, result);
        addDescriptor("customer", archetype, result);
        addDescriptor("patient", archetype, result);
        addDescriptor("reason", archetype, result);
        addDescriptor("description", archetype, result);
        return result;
    }

    /**
     * Helper to add a node descriptor to a list of descriptors.
     *
     * @param node        the node name
     * @param archetype   the archetype
     * @param descriptors the descriptors to add to
     */
    private void addDescriptor(String node, ArchetypeDescriptor archetype,
                               List<NodeDescriptor> descriptors) {
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor != null) {
            descriptors.add(descriptor);
        }
    }

}
