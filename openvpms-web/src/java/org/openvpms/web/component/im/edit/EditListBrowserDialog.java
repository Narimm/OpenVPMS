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
 */
package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.focus.FocusCommand;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * An {@link BrowserDialog} which allows items in the browser to be edited.
 *
 * @author Tim Anderson
 */
public class EditListBrowserDialog<T extends IMObject> extends BrowserDialog<T> {

    /**
     * The edit button id.
     */
    private static final String EDIT_ID = "edit";

    /**
     * The select button id.
     */
    private static final String SELECT_ID = "select";

    /**
     * The buttons to display.
     */
    private static final String[] BUTTONS = {EDIT_ID, SELECT_ID, CANCEL_ID};


    /**
     * Constructs an {@code EditBrowserDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param help    the help context
     */
    public EditListBrowserDialog(String title, Browser<T> browser, HelpContext help) {
        super(title, null, BUTTONS, browser, true, help);
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (EDIT_ID.equals(button)) {
            T selected = getSelected();
            if (selected != null) {
                onEdit(selected);
            }
        } else if (SELECT_ID.equals(button)) {
            T selected = getSelected();
            if (selected != null) {
                onOK();
            }
        } else {
            super.onButton(button);
        }
    }

    /**
     * Invoked to edit an object.
     *
     * @param object the object to edit
     */
    private void onEdit(T object) {
        // make sure the latest instance is being used.
        T current = IMObjectHelper.reload(object);
        if (current == null) {
            ErrorDialog.show(Messages.get("imobject.noexist", DescriptorHelper.getDisplayName(object)));
        } else {
            final FocusCommand focus = new FocusCommand();
            LayoutContext context = new DefaultLayoutContext(true, getHelpContext());
            IMObjectEditor editor = IMObjectEditorFactory.create(current, context);
            EditDialog dialog = EditDialogFactory.create(editor, getHelpContext());
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    focus.restore();
                }
            });
            dialog.show();
        }
    }

}
