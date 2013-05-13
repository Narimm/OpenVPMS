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

package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.subsystem.CRUDWindow;
import org.openvpms.web.workspace.subsystem.ResultSetCRUDWorkspace;


/**
 * Lookup workspace.
 *
 * @author Tim Anderson
 */
public class LookupWorkspace extends ResultSetCRUDWorkspace<Lookup> {

    /**
     * Constructs a {@code LookupWorkspace}.
     *
     * @param context the context
     */
    public LookupWorkspace(Context context) {
        super("admin", "lookup", context);
        setArchetypes(Lookup.class, "lookup.*");
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Lookup> createCRUDWindow() {
        QueryBrowser<Lookup> browser = getBrowser();
        return new LookupCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(), getContext(),
                                    getHelpContext());
    }

}
