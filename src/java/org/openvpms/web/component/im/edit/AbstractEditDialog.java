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

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.component.util.Vetoable;

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
     * Determines if saves are disabled.
     */
    private boolean savedDisabled;


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
        getButtons().addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_M, new ActionListener() {
            public void onAction(ActionEvent event) {
                onMacro();
            }
        });
        setCancelListener(new VetoListener() {
            public void onVeto(Vetoable action) {
                onCancel(action);
            }
        });
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
     * Saves the editor, optionally closing the dialog.
     * <p/>
     * If the the save fails, the dialog will remain open.
     *
     * @param close if <tt>true</tt> close the dialog
     */
    public void save(boolean close) {
        if (!close) {
            onApply();
        } else {
            onOK();
        }
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
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        super.doLayout();
        if (editor != null) {
            FocusGroup group = editor.getFocusGroup();
            if (group != null) {
                group.setFocus();
            }
        }
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
    protected void doCancel() {
        if (editor != null) {
            editor.cancel();
        }
        super.doCancel();
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

            if (getParent() != null) {
                // focus in the editor
                editor.getFocusGroup().setFocus();
            }
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
        if (!savedDisabled) {
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
        }
        return result;
    }

    /**
     * Saves the current object.
     *
     * @return <tt>true</tt> if the object was saved
     */
    protected boolean doSave() {
        return (editor != null && save(editor));
    }

    /**
     * Saves the editor in a transaction.
     *
     * @param editor the editor
     * @return <tt>true</tt> if the save was successful
     */
    protected boolean save(IMObjectEditor editor) {
        return SaveHelper.save(editor);
    }

    /**
     * Invoked by {@link #save} when saving fails.
     * <p/>
     * This implementation disables saves.
     * TODO - this is a workaround for OVPMS-855
     */
    protected void saveFailed() {
        savedDisabled = true;
        ButtonSet buttons = getButtons();
        for (Component component : buttons.getContainer().getComponents()) {
            if (component instanceof Button) {
                Button button = (Button) component;
                if (!CANCEL_ID.equals(button.getId())) {
                    buttons.setEnabled(button.getId(), false);
                }
            }
        }
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
     * Displays the macros.
     */
    protected void onMacro() {
        MacroDialog dialog = new MacroDialog();
        dialog.show();
    }

    /**
     * Determines if saving has been disabled.
     *
     * @return <tt>true</tt> if saves are disabled
     */
    protected boolean isSaveDisabled() {
        return savedDisabled;
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

    /**
     * Invoked to veto/allow a cancel request.
     *
     * @param action the vetoable action
     */
    private void onCancel(final Vetoable action) {
/*
     TODO - no longer prompt for cancellation due to incorrect isModified() results. See OVPMS-987 for details.

        if (editor != null && editor.isModified() && !savedDisabled) {
            String title = Messages.get("editor.cancel.title");
            String message = Messages.get("editor.cancel.message", editor.getDisplayName());
            final ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent e) {
                    if (ConfirmationDialog.YES_ID.equals(dialog.getAction())) {
                        action.veto(false);
                    } else {
                        action.veto(true);
                    }
                }
            });
            dialog.show();
        } else {
            action.veto(false);
        }
*/
        action.veto(false);
    }

}
