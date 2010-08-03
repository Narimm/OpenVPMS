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
import org.openvpms.web.component.im.query.ResultSetBrowser;
import org.openvpms.web.app.subsystem.ResultSetCRUDWindow;
import org.openvpms.web.app.subsystem.ResultSetCRUDWorkspace;


/**
 * Archetype workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeWorkspace extends ResultSetCRUDWorkspace<ArchetypeDescriptor> {

    /**
     * Constructs an <tt>ArchetypeWorkspace</tt>.
     */
    public ArchetypeWorkspace() {
        super("admin", "archetype");
        setArchetypes(ArchetypeDescriptor.class, "descriptor.*");
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected ResultSetCRUDWindow<ArchetypeDescriptor> createCRUDWindow() {
        ResultSetBrowser<ArchetypeDescriptor> browser = getBrowser();
        return new ArchetypeCRUDWindow(getArchetypes(), browser.getResultSet());
    }

}
