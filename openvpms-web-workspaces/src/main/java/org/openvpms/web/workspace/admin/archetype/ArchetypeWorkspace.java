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
 */

package org.openvpms.web.workspace.admin.archetype;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;


/**
 * Archetype workspace.
 *
 * @author Tim Anderson
 */
public class ArchetypeWorkspace extends ResultSetCRUDWorkspace<ArchetypeDescriptor> {

    /**
     * Constructs an {@code ArchetypeWorkspace}.
     *
     * @param context the context
     */
    public ArchetypeWorkspace(Context context) {
        super("admin", "archetype", context);
        setArchetypes(ArchetypeDescriptor.class, "descriptor.*");
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected ResultSetCRUDWindow<ArchetypeDescriptor> createCRUDWindow() {
        QueryBrowser<ArchetypeDescriptor> browser = getBrowser();
        return new ArchetypeCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(), getContext(),
                                       getHelpContext());
    }

}
