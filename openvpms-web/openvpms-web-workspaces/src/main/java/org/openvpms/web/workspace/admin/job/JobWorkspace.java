/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.job;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;


/**
 * Job workspace.
 *
 * @author Tim Anderson
 */
public class JobWorkspace extends ResultSetCRUDWorkspace<Entity> {

    /**
     * Constructs a {@link JobWorkspace}.
     *
     * @param context the context
     */
    public JobWorkspace(Context context) {
        super("admin.job", context);
        setArchetypes(Entity.class, "entity.job*");
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code false}
     */
    @Override
    public boolean canUpdate(String shortName) {
        return false;
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Entity> createCRUDWindow() {
        QueryBrowser<Entity> browser = getBrowser();
        return new JobCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(), getContext(),
                                 getHelpContext());
    }
}
