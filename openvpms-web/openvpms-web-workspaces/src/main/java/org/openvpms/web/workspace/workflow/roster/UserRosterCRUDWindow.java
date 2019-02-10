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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;

import java.util.Date;

/**
 * Roster CRUD window for the {@link UserRosterBrowser} roster view.
 *
 * @author Tim Anderson
 */
class UserRosterCRUDWindow extends RosterCRUDWindow {
    /**
     * Constructs a {@link RosterCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public UserRosterCRUDWindow(UserRosterBrowser browser, Context context, HelpContext help) {
        super(browser, context, help);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    @Override
    protected void onCreated(Act object) {
        IMObjectBean bean = new IMObjectBean(object);
        RosterBrowser browser = getBrowser();
        bean.setTarget("user", browser.getSelectedEntity());
        Date date = browser.getSelectedDate();
        bean.setValue("startTime", date);
        bean.setValue("endTime", date);
        super.onCreated(object);
    }

    /**
     * Populates an entity, copying or moving an event.
     *
     * @param editor the editor
     * @param entity the entity
     */
    @Override
    protected void setEntity(RosterEventEditor editor, Entity entity) {
        editor.setUser((User) entity);
    }
}
