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
package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * A popup dialog that displays an {@link IMObjectEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractEditDialog extends PopupDialog {

    /**
     * The editor.
     */
    protected IMObjectEditor editor;

    /**
     * Determines if the dialog should save when apply and OK are pressed.
     */
    protected final boolean save;

    /**
     * Edit dialog style name.
     */
    protected static final String STYLE = "EditDialog";


    /**
     * Constructs an <tt>AbstractEditDialog</tt>.
     *
     * @param editor the editor
     */
    public AbstractEditDialog(IMObjectEditor editor) {
        this(editor.getTitle(), getButtons(true, true, false), true);
    }

    /**
     * Constructs an <tt>AbstractEditDialog</tt>.
     *
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param save    if <tt>true</tt>, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     */
    public AbstractEditDialog(String title, String[] buttons, boolean save) {
        this(null, title, buttons, save);
    }

    /**
     * Constructs an <tt>AbstractEditDialog</tt>.
     *
     * @param editor  the editor
     * @param buttons the buttons to display
     * @param save    if <tt>true</tt>, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     */
    public AbstractEditDialog(IMObjectEditor editor, String[] buttons, boolean save) {
        this(editor, editor.getTitle(), buttons, save);
    }

    /**
     * Constructs an <tt>AbstractEditDialog</tt>.
     *
     * @param editor  the editor. May be <tt>null</tt>
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param save    if <tt>true</tt>, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     */
    public AbstractEditDialog(IMObjectEditor editor, String title, String[] buttons, boolean save) {
        super(title, STYLE, buttons);
        setModal(true);
        setEditor(editor);
        this.save = save;
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or <tt>null</tt> if none has been set
     */
    public IMObjectEditor getEditor() {
        return editor;
    }

    /**
     * Determines if a skip button should be added.
     *
     * @param skip if <tt>true</tt> add a skip button, otherwise remove it
     */
    public void addSkip(boolean skip) {
        ButtonSet buttons = getButtons();
        Button button = buttons.getButton(SKIP_ID);
        if (skip) {
            if (button == null) {
                addButton(SKIP_ID, false);
            }
        } else {
            if (button != null) {
                buttons.remove(button);
            }
        }
    }

    /**
     * Saves the current object, if saving is enabled.
     */
    @Override
    public void onApply() {
        save();
    }

    /**
     * Saves the current object, if saving is enabled, and closes the editor.
     */
    @Override
    public void onOK() {
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
    public void onCancel() {
        if (editor != null) {
            editor.cancel();
        }
        close(CANCEL_ID);
    }

    /**
     * Sets the editor.
     *
     * @param editor the editor. May be <tt>null</tt>
     */
    protected void setEditor(IMObjectEditor editor) {
        IMObjectEditor previous = this.editor;
        if (editor != null) {
            setTitle(editor.getTitle());
            editor.addPropertyChangeListener(
                    IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent event) {
                            onComponentChange(event);
                        }
                    });
        }
        this.editor = editor;
        if (previous != null) {
            getLayout().remove(previous.getComponent());
            getFocusGroup().remove(previous.getFocusGroup());
        }
        if (editor != null) {
            getLayout().add(editor.getComponent());
            getFocusGroup().add(0, editor.getFocusGroup());
        }
    }

    /**
     * Saves the current object, if saving is enabled.
     * <p/>
     * If it is, and the object is valid, then {@link #doSave()} is called. If {@link #doSave()} fails
     * (i.e returns <tt>false</tt>), then {@link #saveFailed()} is called.
     *
     * @return <tt>true</tt> if the object was saved
     */
    protected boolean save() {
        boolean result = false;
        if (save && editor != null) {
            Validator validator = new Validator();
            if (editor.validate(validator)) {
                result = doSave();
                if (!result) {
                    saveFailed();
                }
            } else {
                ValidationHelper.showError(validator);
            }
        }
        return result;
    }

    /**
     * Saves the current object.
     *
     * @return <tt>true</tt> if the object was saved
     */
    protected boolean doSave() {
        return editor != null && SaveHelper.save(editor);
    }

    /**
     * Invoked by {@link #save} when saving fails.
     * <p/>
     * This implementation delegates to {@link #onCancel()}, discarding any changes and closing the dialog.
     * TODO - this is a workaround for OVPMS-855
     */
    protected void saveFailed() {
        onCancel();
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
     * Helper to determine which buttons should be displayed.
     *
     * @param apply  if <tt>true</tt> provide apply and OK buttons
     * @param cancel if <tt>true</tt> provide a cancel button
     * @param skip   if <tt>true</tt> provide a skip button
     * @return the button identifiers
     */
    protected static String[] getButtons(boolean apply, boolean cancel, boolean skip) {
        if (apply && skip && cancel) {
            return new String[]{APPLY_ID, OK_ID, SKIP_ID, CANCEL_ID};
        } else if (apply && cancel) {
            return APPLY_OK_CANCEL;
        } else if (apply && skip) {
            return new String[]{APPLY_ID, OK_ID, SKIP_ID};
        } else if (apply) {
            return new String[]{APPLY_ID, OK_ID};
        } else if (skip && cancel) {
            return OK_SKIP_CANCEL;
        } else if (skip) {
            return new String[]{OK_ID, SKIP_ID};
        } else if (cancel) {
            return OK_CANCEL;
        } else {
            return OK;
        }
    }
}
