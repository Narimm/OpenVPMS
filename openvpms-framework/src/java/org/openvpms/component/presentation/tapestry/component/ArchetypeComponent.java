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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.presentation.tapestry.component;

import java.util.ArrayList;
import java.util.List;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ArchetypeComponent extends OpenVpmsComponent {

    public ArchetypeComponent() {
        super();
    }

    /**
     * @return
     */
    public abstract ArchetypeDescriptor getArchetypeDescriptor();

    /**
     * @param ArchetypeDescriptor
     */
    public abstract void setArchetypeDescriptor(
            ArchetypeDescriptor ArchetypeDescriptor);

    /**
     * @return
     */
    public abstract String[] getNodeNames();

    /**
     * @param nodeNames
     */
    public abstract void setNodeNames(String[] nodeNames);

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List getDescriptors() {
        if (getNodeNames() == null || getNodeNames().length == 0) {
            return new ArrayList(getArchetypeDescriptor()
                    .getNodeDescriptorsAsMap().values());
        } else {
            return getArchetypeDescriptor().getNodeDescriptors(
                    getNodeNames());
        }
    }
}
