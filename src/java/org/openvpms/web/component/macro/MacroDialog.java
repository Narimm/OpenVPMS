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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.macro;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusCommand;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * A dialog to browse and select active macros.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacroDialog extends PopupDialog {

    /**
     * The focus, prior to the dialog being shown.
     */
    private FocusCommand focus;


    /**
     * Constructs a <tt>MacroDialog</tt>.
     */
    public MacroDialog() {
        super(Messages.get("macros.title"), "MacroDialog", CLOSE);
        focus = new FocusCommand();
        MacroQuery query = new MacroQuery();
        QueryFactory.initialise(query);
        query.setShowInactive(false);
        MacroTableModel model = new MacroTableModel(false);
        Browser<Lookup> browser = BrowserFactory.create(query, query.getDefaultSortConstraint(), model);
        browser.addBrowserListener(new BrowserListener<Lookup>() {

            public void selected(Lookup object) {
                onSelected(object);
            }

            public void browsed(Lookup object) {
            }

            public void query() {
            }
        });
        getLayout().add(ColumnFactory.create("Inset", browser.getComponent()));
        getButtons().addKeyListener(KeyStrokes.VK_ESCAPE, new ActionListener() {
            public void onAction(ActionEvent event) {
                onClose();
            }
        });
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
     * If the focussed component prior to the dialog being opened is an editable text component, the macro's
     * code is added to the end of the text.
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
                String value = text.getText();
                value = (value == null) ? macro.getCode() : value + macro.getCode();
                text.setText(value);
            }
        }
        onClose();
    }

}
