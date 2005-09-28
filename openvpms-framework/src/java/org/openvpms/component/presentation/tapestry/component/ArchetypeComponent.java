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

import java.util.List;
import org.openvpms.component.business.service.archetype.IArchetypeDescriptor;

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
    public abstract IArchetypeDescriptor getArchetypeDescriptor();

    /**
     * @param ArchetypeDescriptor
     */
    public abstract void setArchetypeDescriptor(
            IArchetypeDescriptor ArchetypeDescriptor);

    /**
     * @return
     */
    public abstract String[] getPropertyNames();

    /**
     * @param PropertyNames
     */
    public abstract void setPropertyNames(String[] PropertyNames);

    /**
     * @return
     */
    public List getPropertyDescriptors() {
        if (getPropertyNames() == null || getPropertyNames().length == 0) {
            return getArchetypeDescriptor().getPropertyDescriptors();
        } else {
            return getArchetypeDescriptor().getPropertyDescriptors(
                    getPropertyNames());
        }
    }
}
