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

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * CRUD window for administering users.
 *
 * @author Tim Anderson
 */
public class UserCRUDWindow extends ResultSetCRUDWindow<User> {

    /**
     * Reset preferences button id.
     */
    private static final String RESET_PREFERENCES_ID = "button.resetPreferences";

    /**
     * Constructs an {@link UserCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public UserCRUDWindow(Archetypes<User> archetypes, Query<User> query, ResultSet<User> set, Context context,
                          HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(RESET_PREFERENCES_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onResetPreferences();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(RESET_PREFERENCES_ID, enable);
    }

    /**
     * Resets user preferences.
     */
    private void onResetPreferences() {
        final User user = getObject();
        if (user != null) {
            String title = Messages.get("admin.user.resetprefs.title");
            String message = Messages.format("admin.user.resetprefs.message", user.getName());
            ConfirmationDialog.show(title, message, PopupDialog.YES_NO, new WindowPaneListener() {
                @Override
                public void onClose(WindowPaneEvent event) {
                    PreferenceService service = ServiceHelper.getBean(PreferenceService.class);
                    service.reset(user);
                }
            });
        }
    }
}
