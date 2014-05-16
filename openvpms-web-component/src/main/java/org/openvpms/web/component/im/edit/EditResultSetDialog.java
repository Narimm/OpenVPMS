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
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * A edit dialog that allows the results from an {@link ResultSet} to be iterated through and edited.
 *
 * @author Tim Anderson
 */
public class EditResultSetDialog<T extends IMObject> extends AbstractEditDialog {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The iterator over the results.
     */
    private ResultSetIterator<T> iter;

    /**
     * The 'previous' button id.
     */
    private static final String PREVIOUS_ID = "previous";

    /**
     * The 'next' button id.
     */
    private static final String NEXT_ID = "next";

    /**
     * The save button id.
     */
    private static final String SAVE_ID = "save";

    /**
     * The revert button id.
     */
    private static final String REVERT_ID = "revert";

    /**
     * The buttons.
     */
    private static final String[] BUTTONS = {APPLY_ID, OK_ID, CANCEL_ID, PREVIOUS_ID, NEXT_ID};

    /**
     * The confirmation buttons.
     */
    private static final String[] CONFIRMATION = {SAVE_ID, REVERT_ID, CANCEL_ID};


    /**
     * Constructs an {@code EditResultSetDialog}.
     *
     * @param title   the window title
     * @param first   the first object to edit
     * @param set     the set of results to edit
     * @param context the context
     * @param help    the help context
     */
    public EditResultSetDialog(String title, T first, ResultSet<T> set, Context context, HelpContext help) {
        super(title, BUTTONS, true, context, help);
        this.context = context;
        setDefaultCloseAction(CANCEL_ID);
        iter = new ResultSetIterator<T>(set, first);
        if (iter.hasNext()) {
            edit(iter.next());
        }
        enableButtons();
        setModal(true);
    }

    /**
     * Invoked when the 'previous' button is pressed.
     */
    protected void onPrevious() {
        IMObjectEditor editor = getEditor();
        if (!checkModified(editor, false)) {
            long id = (editor != null) ? editor.getObject().getId() : -1;
            if (iter.hasPrevious()) {
                T object = iter.previous();
                if (object.getId() == id) {
                    // list iterator returns the same object if previous()/next() invoked or vice versa
                    if (iter.hasPrevious()) {
                        object = iter.previous();
                    }
                }
                edit(object);
                enableButtons();
            }
        }
    }

    /**
     * Invoked when the 'next' button is pressed.
     */
    protected void onNext() {
        final IMObjectEditor editor = getEditor();
        if (!checkModified(editor, true)) {
            long id = (editor != null) ? editor.getObject().getId() : -1;
            if (iter.hasNext()) {
                T object = iter.next();
                if (object.getId() == id) {
                    if (iter.hasNext()) {
                        // list iterator returns the same object if previous()/next() invoked or vice versa
                        object = iter.next();
                    }
                }
                edit(object);
                enableButtons();
            }
        }
    }

    /**
     * Checks the editor to see if is modified, and if so, displays a confirmation dialog prompting to save or revert
     * changes, or to cancel the operation.
     *
     * @param editor the editor. May be {@code null}
     * @param next   the operation. If {@code true} move next, else move previous
     * @return {@code true} if the editor is modified; {@code false} if the editor hasn't been modified, or is
     *         {@code null}
     */
    private boolean checkModified(final IMObjectEditor editor, final boolean next) {
        boolean result = true;
        if (editor != null && editor.isModified()) {
            String title = Messages.get("imobject.savechanges.title");
            String message = Messages.format("imobject.savechanges.message", editor.getDisplayName());
            ConfirmationDialog dialog = new ConfirmationDialog(title, message, CONFIRMATION);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onAction(String action) {
                    if (SAVE_ID.equals(action)) {
                        if (save()) {
                            if (next) {
                                onNext();
                            } else {
                                onPrevious();
                            }
                        }
                    } else if (REVERT_ID.equals(action)) {
                        edit(editor.getObject()); // re-edit the object to revert it
                    }
                }
            });
            dialog.show();
        } else {
            result = false;
        }
        return result;
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
        if (PREVIOUS_ID.equals(button)) {
            onPrevious();
        } else if (NEXT_ID.equals(button)) {
            onNext();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Edits the specified object.
     *
     * @param object the object to edit
     */
    private void edit(IMObject object) {
        // make sure the latest instance is being used.
        IMObject current = IMObjectHelper.reload(object);
        if (current == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", DescriptorHelper.getDisplayName(object)));
        } else {
            HelpContext help = getHelpContext().topic(object, "edit");
            LayoutContext context = new DefaultLayoutContext(true, this.context, help);
            context.getContext().setCurrent(object); // TODO - requirement for setCurrent()
            IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(current, context);
            setEditor(editor);
        }
    }

    /**
     * Enables/disables the previous and next buttons.
     */
    private void enableButtons() {
        ButtonSet set = getButtons();
        set.setEnabled(PREVIOUS_ID, iter.lastIndex() > 0);
        set.setEnabled(NEXT_ID, iter.hasNext());
    }

}
