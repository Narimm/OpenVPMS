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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.property;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import static org.junit.Assert.assertNotNull;

/**
 * Helper for property tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class PropertyTestHelper {

    /**
     * Helper to return a node descriptor.
     *
     * @param object the object
     * @param node   the node name
     * @return the node descriptor
     */
    public static NodeDescriptor getDescriptor(IMObject object, String node) {
        ArchetypeDescriptor type = DescriptorHelper.getArchetypeDescriptor(object);
        assertNotNull(type);
        NodeDescriptor result = type.getNodeDescriptor(node);
        assertNotNull(result);
        return result;
    }

}
