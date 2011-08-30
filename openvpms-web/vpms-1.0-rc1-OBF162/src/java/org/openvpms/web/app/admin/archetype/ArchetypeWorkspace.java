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

package org.openvpms.web.app.admin.archetype;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.app.subsystem.ShortNameList;


/**
 * Archetype workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeWorkspace extends CRUDWorkspace<ArchetypeDescriptor> {

    /**
     * Constructs a new <tt>ArchetypeWorkspace</tt>.
     */
    public ArchetypeWorkspace() {
        super("admin", "archetype", new ShortNameList("descriptor.*"));
    }

    /**
     * Sets the current object.
     * This is analagous to {@link #setObject} but performs a safe cast to the
     * required type.
     *
     * @param object the current object. May be <tt>null</tt>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof ArchetypeDescriptor) {
            setObject((ArchetypeDescriptor) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + ArchetypeDescriptor.class.getName());
        }
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<ArchetypeDescriptor> createCRUDWindow() {
        return new ArchetypeCRUDWindow(getType(), getShortNames());
    }

}
