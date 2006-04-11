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

package org.openvpms.web.component.im.table;

import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * Factory for {@link IMObjectTableModel} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectTableModelFactory {

    /**
     * Create a new {@link IMObjectTableModel} given a node descriptor.
     *
     * @param descriptor the node descriptor
     * @param context    the layout context
     * @return a new table model
     */
    public static IMObjectTableModel create(NodeDescriptor descriptor,
                                            LayoutContext context) {
        DefaultIMObjectTableModel result;
        List<ArchetypeDescriptor> archetypes;
        archetypes = DescriptorHelper.getArchetypeDescriptors(
                descriptor.getArchetypeRange());
        if (EntityRelationshipTableModel.canHandle(archetypes)) {
            result = new EntityRelationshipTableModel(context);
        } else {
            result = new DefaultIMObjectTableModel();
        }
        return result;
    }
}
