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

package org.openvpms.web.component.prefs;

import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpDialog;
import org.openvpms.web.component.help.HelpTopics;
import org.openvpms.web.component.im.edit.AbstractEditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.util.StyleSheetHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.help.HelpListener;
import org.openvpms.web.system.ServiceHelper;

/**
 * Preferences editor dialog.
 *
 * @author Tim Anderson
 */
public class PreferencesDialog extends AbstractEditDialog {

    /**
     * Constructs a {@link PreferencesDialog}.
     *
     * @param user    the user to edit preferences for
     * @param context the context
     */
    public PreferencesDialog(User user, Context context) {
        super(null, null, OK_CANCEL, true, context, createHelpContext());
        setStyleName("PreferencesDialog");

        DefaultLayoutContext layout = new DefaultLayoutContext(context, getHelpContext());
        IMObjectEditor editor = new PreferencesEditor(user, layout);
        setEditor(editor);
    }

    /**
     * Saves the current object, if saving is enabled.
     * <p/>
     * If it is, and the object is valid, then {@link #doSave(IMObjectEditor)} is called.
     * If {@link #doSave(IMObjectEditor)} fails (i.e returns {@code false}), then {@link #saveFailed()} is called.
     *
     * @return {@code true} if the object was saved
     */
    @Override
    public boolean save() {
        boolean save = super.save();
        if (save) {
            UserPreferences preferences = ServiceHelper.getBean(UserPreferences.class);
            preferences.refresh();
        }
        return save;
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return getEditor() != null ? getEditor().getHelpContext() : super.getHelpContext();
    }

    protected static HelpContext createHelpContext() {
        return new HelpContext("entity.preferences/edit", new HelpListener() {
            public void show(HelpContext help) {
                String features = StyleSheetHelper.getProperty("HelpBrowser.features");
                HelpDialog.show(help, ServiceHelper.getBean(HelpTopics.class), ServiceHelper.getArchetypeService(),
                                features);
            }
        });
    }
}
