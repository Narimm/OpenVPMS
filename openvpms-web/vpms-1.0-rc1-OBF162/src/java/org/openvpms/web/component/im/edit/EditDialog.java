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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.dialog.PopupDialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * A popup window that displays an {@link IMObjectEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EditDialog extends PopupDialog {

    /**
     * The editor.
     */
    private final IMObjectEditor editor;

    /**
     * Determines if the dialog should save when apply and OK are pressed.
     */
    private final boolean save;

    /**
     * Edit window style name.
     */
    private static final String STYLE = "EditDialog";


    /**
     * Constructs a new <code>EditDialog</code>.
     *
     * @param editor the editor
     */
    public EditDialog(IMObjectEditor editor) {
        this(editor, true);
    }

    /**
     * Constructs a new <code>EditDialog</code>.
     *
     * @param editor the editor
     * @param save   if <code>true</code>, display an 'apply' and 'OK' button
     *               that save the editor when pressed. If <code>false</code>
     *               display an 'OK' and 'CANCEL' button that simply close the
     *               dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save) {
        this(editor, save, false);
    }

    /**
     * Constructs a new <code>EditDialog</code>.
     *
     * @param editor the editor
     * @param save   if <code>true</code>, display an 'apply' and 'OK' button
     *               that save the editor when pressed. If <code>false</code>
     *               display an 'OK' and 'CANCEL' button that simply close the
     *               dialog
     * @param skip   if <code>triue</code> display a 'skip' button that simply
     *               closes the dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean skip) {
        super(editor.getTitle(), STYLE, getButtons(save, skip));
        this.editor = editor;
        this.save = save;
        setModal(true);

        getLayout().add(editor.getComponent());
        this.editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onComponentChange(event);
                    }
                });
        getFocusGroup().add(0, editor.getFocusGroup());
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    protected IMObjectEditor getEditor() {
        return editor;
    }

    /**
     * Saves the current object, if saving is enabled.
     */
    @Override
    protected void onApply() {
        save();
    }

    /**
     * Saves the current object, if saving is enabled, and closes the editor.
     */
    @Override
    protected void onOK() {
        if (save) {
            if (save()) {
                close(OK_ID);
            }
        } else {
            close(OK_ID);
        }
    }

    /**
     * Close the editor, discarding any unsaved changes.
     */
    @Override
    protected void onCancel() {
        editor.cancel();
        close(CANCEL_ID);
    }

    /**
     * Saves the current object, is saving is enabled.
     *
     * @return <code>true</code> if the object was saved
     */
    protected boolean save() {
        boolean result = false;
        if (save) {
            result = editor.save();
        }
        return result;
    }

    /**
     * Invoked when the component changes.
     *
     * @param event the component change event
     */
    protected void onComponentChange(PropertyChangeEvent event) {
        getLayout().remove((Component) event.getOldValue());
        getLayout().add((Component) event.getNewValue());
    }

    /**
     * Determines which buttons should be displayed.
     *
     * @param save if <code>true</code> provide apply, OK, delete and cancel
     *             buttons, otherwise provide OK and cancel buttons
     * @param skip if <code>triue</code> display a 'skip' button
     * @return the button identifiers
     */
    private static String[] getButtons(boolean save, boolean skip) {
        if (save && skip) {
            return new String[]{APPLY_ID, OK_ID, SKIP_ID, CANCEL_ID};
        } else if (save) {
            return APPLY_OK_CANCEL;
        } else if (skip) {
            return new String[]{OK_ID, SKIP_ID, CANCEL_ID};
        } else {
            return OK_CANCEL;
        }
    }

}
