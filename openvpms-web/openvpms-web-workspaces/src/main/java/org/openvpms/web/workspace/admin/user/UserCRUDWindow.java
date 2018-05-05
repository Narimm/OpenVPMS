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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.user;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.ReferenceDataService;
import org.openvpms.smartflow.client.SyncState;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.prefs.PreferencesDialog;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
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
     * The Smart Flow Sheet service factory.
     */
    private final FlowSheetServiceFactory flowSheetServiceFactory;

    /**
     * Edit preferences button id.
     */
    private static final String EDIT_PREFERENCES_ID = "button.editPreferences";

    /**
     * Default preferences button id.
     */
    private static final String DEFAULT_PREFERENCES_ID = "button.defaultPreferences";

    /**
     * Reset preferences button id.
     */
    private static final String RESET_PREFERENCES_ID = "button.resetPreferences";


    /**
     * Synchronise with Smart Flow Sheet button identifier.
     */
    private static final String SYNCH_ID = "button.synchwithsfs";

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
        flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(DEFAULT_PREFERENCES_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onDefaultPreferences();
            }
        });
        buttons.add(EDIT_PREFERENCES_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onEditPreferences();
            }
        });
        buttons.add(RESET_PREFERENCES_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onResetPreferences();
            }
        });
        Party location = getContext().getLocation();
        if (location != null && flowSheetServiceFactory.isSmartFlowSheetEnabled(location)) {
            buttons.add(SYNCH_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onSynchronise();
                }
            });
        }
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
        buttons.setEnabled(DEFAULT_PREFERENCES_ID, getContext().getPractice() != null);
        buttons.setEnabled(EDIT_PREFERENCES_ID, enable);
        buttons.setEnabled(RESET_PREFERENCES_ID, enable);
    }

    /**
     * Edits the default preferences.
     */
    private void onDefaultPreferences() {
        PreferencesDialog dialog = new PreferencesDialog(getContext().getPractice(), null, getContext());
        dialog.setTitle(Messages.get("admin.user.prefs.default.title"));
        dialog.show();
    }

    /**
     * Edits preferences for the selected user.
     */
    private void onEditPreferences() {
        User user = IMObjectHelper.reload(getObject());
        if (user != null) {
            Context context = getContext();
            PreferencesDialog dialog = new PreferencesDialog(user, context.getPractice(), true,
                                                             new LocalContext(context));
            dialog.setTitle(Messages.format("admin.user.prefs.edit.title", user.getName()));
            dialog.show();
        } else {
            onRefresh(getObject());
        }
    }

    /**
     * Resets user preferences to the practice defaults.
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
                    service.reset(user, getContext().getPractice());
                }
            });
        }
    }

    /**
     * Invoked when the 'synchronise with SFS' button is pressed.
     */
    private void onSynchronise() {
        HelpContext help = getHelpContext().subtopic("sync");
        ConfirmationDialog.show(Messages.get("admin.user.sync.title"),
                                Messages.get("admin.user.sync.message"),
                                ConfirmationDialog.YES_NO, help, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        synchroniseClinicians();
                    }
                });
    }

    /**
     * Synchronises clinicians.
     */
    private void synchroniseClinicians() {
        ReferenceDataService service = flowSheetServiceFactory.getReferenceDataService(getContext().getLocation());
        SyncState sync = service.synchroniseMedics();
        String title = Messages.get("admin.user.sync.title");
        String message = sync.changed() ? Messages.get("admin.user.sync.updated")
                                        : Messages.get("admin.user.sync.noupdate");
        InformationDialog.show(title, message);
    }

}
