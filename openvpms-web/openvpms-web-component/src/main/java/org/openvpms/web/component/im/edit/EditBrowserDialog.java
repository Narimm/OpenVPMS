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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.focus.FocusCommand;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.util.DoubleClickMonitor;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * An {@link BrowserDialog} that allows items in the browser to be edited.
 *
 * @author Tim Anderson
 */
public class EditBrowserDialog<T extends IMObject> extends BrowserDialog<T> {

    /**
     * The edit button id.
     */
    public static final String EDIT_ID = "button.edit";

    /**
     * Used to determine if an object may be edited.
     */
    private final IMObjectActions<T> actions;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Helper to monitor double clicks. When an object is double clicked, an edit dialog is displayed
     */
    private final DoubleClickMonitor click = new DoubleClickMonitor();

    /**
     * Constructs an {@link EditBrowserDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param buttons the buttons to display
     * @param actions determines if an object may be edited
     * @param context the context
     * @param help    the help context
     */
    public EditBrowserDialog(String title, String[] buttons, Browser<T> browser, IMObjectActions<T> actions,
                             Context context, HelpContext help) {
        this(title, buttons, browser, actions, false, context, help);
    }

    /**
     * Constructs an {@link EditBrowserDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param buttons the buttons to display
     * @param actions determines if an object may be edited
     * @param addNew  if {@code true} add a 'new' button
     * @param context the context
     * @param help    the help context
     */
    public EditBrowserDialog(String title, String[] buttons, Browser<T> browser, IMObjectActions<T> actions,
                             boolean addNew, Context context, HelpContext help) {
        super(title, null, buttons, browser, addNew, help);
        this.actions = actions;
        this.context = context;
        setCloseOnSelection(false);
    }

    /**
     * Invoked when the 'OK' button is pressed. This closes the dialog.
     */
    @Override
    protected void onOK() {
        close(OK_ID);
    }

    /**
     * Sets the selected object.
     *
     * @param object the selected object. May be {@code null}
     */
    @Override
    protected void setSelected(T object) {
        super.setSelected(object);
        if (object != null) {
            boolean enabled = actions.canEdit(object);
            getButtons().setEnabled(EDIT_ID, enabled);
        }
    }

    /**
     * Invoked when an object is selected. If it is double click, and the object is editable,
     * invokes {@link #onEdit(IMObject)}.
     *
     * @param object the selected object
     */
    @Override
    protected void onSelected(T object) {
        super.onSelected(object);
        if (actions.canEdit(object) && click.isDoubleClick(object.getId())) {
            onEdit(object);
        }
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
            ErrorDialog.show(Messages.format("imobject.noexist", DescriptorHelper.getDisplayName(object)));
        } else if (actions.canEdit(current)) {
            final FocusCommand focus = new FocusCommand();
            HelpContext help = getHelpContext().topic(object, "edit");
            LayoutContext context = new DefaultLayoutContext(true, this.context, help);
            IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(current, context);
            EditDialog dialog = EditDialogFactory.create(editor, this.context);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    Browser<T> browser = getBrowser();
                    browser.query();
                    setSelected(browser.getSelected());
                    focus.restore();
                }
            });
            dialog.show();
        }
    }

}
