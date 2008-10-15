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
     * Constructs a new <tt>EditDialog</tt>.
     *
     * @param editor the editor
     */
    public EditDialog(IMObjectEditor editor) {
        this(editor, true);
    }

    /**
     * Constructs a new <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     */
    public EditDialog(IMObjectEditor editor, boolean save) {
        this(editor, save, false);
    }

    /**
     * Constructs a new <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     * @param skip   if <tt>true</tt> display a 'Skip' button that simply
     *               closes the dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean skip) {
        this(editor, save, true, skip);
    }

    /**
     * Constructs a new <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     * @param apply  if <tt>true</tt>, display an 'Apply' button
     * @param skip   if <tt>true</tt> display a 'Skip' button that simply
     *               closes the dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean apply,
                      boolean skip) {
        super(editor.getTitle(), STYLE, getButtons(save, apply, skip));
        this.editor = editor;
        this.save = save;
        setModal(true);

        this.editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onComponentChange(event);
                    }
                });
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    public IMObjectEditor getEditor() {
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
     * Saves the current object, if saving is enabled.
     *
     * @return <tt>true</tt> if the object was saved
     */
    protected boolean save() {
        boolean result = false;
        if (save) {
            result = SaveHelper.save(editor);
        }
        return result;
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        getLayout().add(editor.getComponent());
        getFocusGroup().add(0, editor.getFocusGroup());
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
     * @param save if <tt>true</tt> provide apply, OK, delete and cancel
     *             buttons, otherwise provide OK and cancel buttons
     * @param skip if <tt>triue</tt> display a 'skip' button
     * @return the button identifiers
     */
    private static String[] getButtons(boolean save, boolean apply,
                                       boolean skip) {
        if (save && apply && skip) {
            return new String[]{APPLY_ID, OK_ID, SKIP_ID, CANCEL_ID};
        } else if (save && apply) {
            return APPLY_OK_CANCEL;
        } else if (skip) {
            return new String[]{OK_ID, SKIP_ID, CANCEL_ID};
        } else {
            return OK_CANCEL;
        }
    }

}
