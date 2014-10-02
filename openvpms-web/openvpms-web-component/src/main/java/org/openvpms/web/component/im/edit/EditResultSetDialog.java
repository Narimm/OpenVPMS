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
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * A edit dialog that allows the results from an {@link ResultSet} to be iterated through and edited.
 * <p/>
 * An object may only be edited if {@link IMObjectActions#canEdit(IMObject)} returns {@code true}.
 * If not, it will be viewed instead.
 *
 * @author Tim Anderson
 */
public class EditResultSetDialog<T extends IMObject> extends AbstractEditDialog {

    /**
     * The context.
     */
    private final Context context;

    /**
     * Determines if an object may be edited.
     */
    private final IMObjectActions<T> actions;

    /**
     * The iterator over the results.
     */
    private ResultSetIterator<T> iter;

    /**
     * The viewer, non-null if an object can't be edited.
     */
    private IMObjectViewer viewer;

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
     * Constructs an {@link EditResultSetDialog}.
     *
     * @param title   the window title
     * @param first   the first object to edit
     * @param set     the set of results to edit
     * @param actions determines if an object may be edited
     * @param context the context
     * @param help    the help context
     */
    public EditResultSetDialog(String title, T first, ResultSet<T> set, IMObjectActions<T> actions, Context context,
                               HelpContext help) {
        super(title, BUTTONS, true, context, help);
        this.context = context;
        this.actions = actions;
        setDefaultCloseAction(CANCEL_ID);
        iter = new ResultSetIterator<T>(set, first);
        if (iter.hasNext()) {
            select(iter.next());
        }
        enableButtons();
        setModal(true);
    }

    /**
     * Saves the current object, if saving is enabled, and closes the editor.
     */
    @Override
    protected void onOK() {
        if (getEditor() != null) {
            super.onOK();
        } else {
            close(OK_ID);
        }
    }

    /**
     * Invoked when the 'previous' button is pressed.
     */
    protected void onPrevious() {
        IMObjectEditor editor = getEditor();
        if (!checkModified(editor, false)) {
            previous();
        }
    }

    /**
     * Invoked when the 'next' button is pressed.
     */
    protected void onNext() {
        final IMObjectEditor editor = getEditor();
        if (!checkModified(editor, true)) {
            next();
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
        if (PREVIOUS_ID.equals(button)) {
            onPrevious();
        } else if (NEXT_ID.equals(button)) {
            onNext();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Saves the current object.
     *
     * @return {@code true} if the object was saved
     */
    @Override
    protected boolean doSave() {
        boolean result = super.doSave();
        if (result) {
            IMObjectEditor editor = getEditor();
            if (editor != null) {
                T object = (T) editor.getObject();
                if (!actions.canEdit(object)) {
                    select(object);
                }
            }
        }
        return result;
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
                            navigate(next);
                        }
                    } else if (REVERT_ID.equals(action)) {
                        navigate(next);
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
     * Navigates to the next or previous object.
     *
     * @param next if {@code true}, navigate next, otherwise navigate previous
     */
    private void navigate(boolean next) {
        if (next) {
            next();
        } else {
            previous();
        }
    }

    /**
     * Displays the previous object, if any.
     */
    private void previous() {
        if (iter.hasPrevious()) {
            long id = getCurrentId();
            T object = iter.previous();
            if (object.getId() == id) {
                // list iterator returns the same object if previous()/next() invoked or vice versa
                if (iter.hasPrevious()) {
                    object = iter.previous();
                }
            }
            select(object);
            enableButtons();
        }
    }

    /**
     * Displays the next object, if any.
     */
    private void next() {
        if (iter.hasNext()) {
            long id = getCurrentId();
            T object = iter.next();
            if (object.getId() == id) {
                if (iter.hasNext()) {
                    // list iterator returns the same object if previous()/next() invoked or vice versa
                    object = iter.next();
                }
            }
            select(object);
            enableButtons();
        }
    }

    /**
     * Selects specified object.
     *
     * @param object the object to select
     */
    private void select(T object) {
        // make sure the latest instance is being used.
        T existing = object;
        object = IMObjectHelper.reload(object);
        if (object == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", DescriptorHelper.getDisplayName(existing)));
        } else {
            List<Selection> path = getSelectionPath();
            if (actions.canEdit(object)) {
                HelpContext help = getHelpContext().topic(object, "edit");
                LayoutContext context = new DefaultLayoutContext(true, this.context, help);
                context.getContext().setCurrent(object); // TODO - requirement for setCurrent()
                IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(object, context);
                setViewer(null, null);
                setEditor(editor, path);
            } else {
                HelpContext help = getHelpContext().topic(object, "view");
                LayoutContext context = new DefaultLayoutContext(true, this.context, help);
                setEditor(null, null);
                IMObjectViewer viewer = new IMObjectViewer(object, context);
                setViewer(viewer, path);
            }
        }
    }

    /**
     * Returns the current object identifier.
     *
     * @return the current object identifier, or {@code -1} if none is being edited
     */
    private long getCurrentId() {
        long id = -1;
        IMObjectEditor editor = getEditor();
        if (editor != null) {
            id = editor.getObject().getId();
        } else if (viewer != null) {
            id = viewer.getObject().getId();
        }
        return id;
    }

    /**
     * Returns the current selection path.
     *
     * @return the current selection path, or {@code null} if there is none
     */
    private List<Selection> getSelectionPath() {
        IMObjectEditor editor = getEditor();
        if (editor != null) {
            return editor.getSelectionPath();
        }
        if (viewer != null) {
            return viewer.getSelectionPath();
        }
        return null;
    }

    /**
     * Sets the viewer.
     *
     * @param viewer the viewer. May be {@code null}
     * @param path   the selection path. May be {@code null}
     */
    private void setViewer(IMObjectViewer viewer, List<Selection> path) {
        IMObjectViewer previous = this.viewer;
        if (previous != null) {
            removeComponent(previous.getComponent(), previous.getFocusGroup());
        }
        this.viewer = viewer;
        if (viewer != null) {
            setTitle(Messages.format("imobject.view.title", viewer.getTitle()));
            setComponent(viewer.getComponent(), viewer.getFocusGroup(), viewer.getHelpContext());
            if (path != null) {
                viewer.setSelectionPath(path);
            }
        }
    }

    /**
     * Enables/disables the buttons.
     */
    private void enableButtons() {
        ButtonSet set = getButtons();
        IMObjectEditor editor = getEditor();
        set.setEnabled(APPLY_ID, editor != null);
        set.setEnabled(PREVIOUS_ID, iter.lastIndex() > 0);
        set.setEnabled(NEXT_ID, iter.hasNext());
    }

}
