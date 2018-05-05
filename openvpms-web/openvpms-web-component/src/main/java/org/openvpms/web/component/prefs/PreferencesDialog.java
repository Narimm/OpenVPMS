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

package org.openvpms.web.component.prefs;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpDialog;
import org.openvpms.web.component.help.HelpTopics;
import org.openvpms.web.component.im.edit.AbstractEditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.util.StyleSheetHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

/**
 * Preferences editor dialog.
 *
 * @author Tim Anderson
 */
public class PreferencesDialog extends AbstractEditDialog {

    private final boolean showPrompt;

    /**
     * Constructs a {@link PreferencesDialog}.
     *
     * @param party   the party to edit preferences for
     * @param source  if non-null, specifies the source to copy preferences from if the party has none
     * @param context the layout context
     */
    public PreferencesDialog(Party party, Party source, Context context) {
        this(party, source, false, context);
    }

    /**
     * Constructs a {@link PreferencesDialog}.
     *
     * @param party      the party to edit preferences for
     * @param source     if non-null, specifies the source to copy preferences from if the party has none
     * @param showPrompt if {@code true} display a prompt indicating that the user needs to log out for the changes
     *                   to be seen
     * @param context    the layout context
     */
    public PreferencesDialog(Party party, Party source, boolean showPrompt, Context context) {
        super(null, null, OK_CANCEL, true, context, createHelpContext());
        this.showPrompt = showPrompt;
        setStyleName("PreferencesDialog");

        DefaultLayoutContext layout = new DefaultLayoutContext(context, getHelpContext());
        IMObjectEditor editor = new PreferencesEditor(party, source, layout);
        setEditor(editor);
    }

    /**
     * Saves the current object, if saving is enabled.
     * <p>
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

    /**
     * Sets the component.
     *
     * @param component the component
     * @param group     the focus group
     * @param context   the help context
     */
    @Override
    protected void setComponent(Component component, FocusGroup group, HelpContext context) {
        if (showPrompt) {
            Label label = LabelFactory.create("admin.user.prefs.edit.message", Styles.BOLD);
            component = ColumnFactory.create(Styles.CELL_SPACING, ColumnFactory.create(Styles.INSET, label), component);
        }
        super.setComponent(component, group, context);
    }

    protected static HelpContext createHelpContext() {
        return new HelpContext("entity.preferences/edit", help -> {
            String features = StyleSheetHelper.getProperty("HelpBrowser.features");
            HelpDialog.show(help, ServiceHelper.getBean(HelpTopics.class), ServiceHelper.getArchetypeService(),
                            features);
        });
    }
}
