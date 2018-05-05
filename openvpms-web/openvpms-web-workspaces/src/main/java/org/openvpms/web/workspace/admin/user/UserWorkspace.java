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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.user;

import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;


/**
 * User workspace.
 *
 * @author Tim Anderson
 */
public class UserWorkspace extends ResultSetCRUDWorkspace<User> {

    /**
     * Constructs an {@link UserWorkspace}.
     */
    public UserWorkspace(Context context) {
        super("admin.user", context);
        setArchetypes(Archetypes.create(UserArchetypes.USER_ARCHETYPES, User.class, UserArchetypes.USER,
                                        DescriptorHelper.getDisplayName(UserArchetypes.USER)));
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<User> createCRUDWindow() {
        QueryBrowser<User> browser = getBrowser();
        return new UserCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(), getContext(),
                                  getHelpContext());
    }
}
