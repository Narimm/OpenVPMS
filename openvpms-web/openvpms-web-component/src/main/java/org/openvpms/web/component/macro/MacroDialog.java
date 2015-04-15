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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.macro;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.focus.FocusCommand;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.resource.i18n.Messages;


/**
 * A dialog to browse and select active macros.
 *
 * @author Tim Anderson
 */
public class MacroDialog extends PopupDialog {

    /**
     * The focus, prior to the dialog being shown.
     */
    private FocusCommand focus;


    /**
     * Constructs a {@code MacroDialog}.
     *
     * @param context the context
     * @param help    the help context
     */
    public MacroDialog(Context context, HelpContext help) {
        super(Messages.get("macros.title"), "MacroDialog", CLOSE, help);
        focus = new FocusCommand();
        MacroQuery query = new MacroQuery();
        QueryFactory.initialise(query);
        query.setShowInactive(false);
        DefaultLayoutContext layout = new DefaultLayoutContext(context, help);
        MacroTableModel model = new MacroTableModel(false, false, layout);
        Browser<Lookup> browser = BrowserFactory.create(query, query.getDefaultSortConstraint(), model, layout);
        browser.addBrowserListener(new BrowserListener<Lookup>() {

            public void selected(Lookup object) {
                onSelected(object);
            }

            public void browsed(Lookup object) {
            }

            public void query() {
            }
        });
        getLayout().add(ColumnFactory.create(Styles.INSET, browser.getComponent()));
        getFocusGroup().add(0, browser.getFocusGroup());
        getButtons().addKeyListener(KeyStrokes.VK_ESCAPE, new ActionListener() {
            public void onAction(ActionEvent event) {
                onClose();
            }
        });
        query.setFocus();
        setModal(true);
    }

    /**
     * Processes a user request to close the window
     * <p/>
     * This restores the previous focus
     */
    @Override
    public void userClose() {
        focus.restore();
        super.userClose();
    }

    /**
     * Invoked when a macro is selected.
     * <p/>
     * If the focused component prior to the dialog being opened is an editable text component, the macro's
     * code is inserted into the text at the cursor position.
     * <p/>
     * Finally, the dialog is closed.
     *
     * @param macro the macro
     */
    private void onSelected(Lookup macro) {
        Component component = focus.getComponent();
        if (component != null && component instanceof TextComponent) {
            TextComponent text = (TextComponent) component;
            if (text.isEnabled() && text.isVisible()) {
                int position = text.getCursorPosition();
                String value = text.getText();
                String code = macro.getCode();
                if (value != null) {
                    if (position < value.length()) {
                        value = value.substring(0, position) + code + value.substring(position);
                    } else {
                        value += code;
                    }
                } else {
                    value = code;
                }
                text.setText(value);

                // move the cursor along to either the end of the macro (if it fails to expand), or the end of
                // the macro expansion
                text.setCursorPosition(text.getCursorPosition() + code.length());
            }
        }
        onClose();
    }

}
