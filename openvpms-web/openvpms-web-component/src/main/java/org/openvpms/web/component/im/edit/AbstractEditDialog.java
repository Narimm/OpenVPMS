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

package org.openvpms.web.component.im.edit;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.VetoListener;
import org.openvpms.web.echo.event.Vetoable;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;


/**
 * A popup dialog that displays an {@link IMObjectEditor}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEditDialog extends PopupDialog {

    /**
     * The editor.
     */
    private IMObjectEditor editor;

    /**
     * Determines if the dialog should save when apply and OK are pressed.
     */
    private final boolean save;

    /**
     * Determines if saves are disabled.
     */
    private boolean savedDisabled;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Edit dialog style name.
     */
    protected static final String STYLE = "EditDialog";


    /**
     * Constructs an {@code AbstractEditDialog}.
     *
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param context the context
     * @param help    the help context
     */
    public AbstractEditDialog(String title, String[] buttons, boolean save, Context context, HelpContext help) {
        this(null, title, buttons, save, context, help);
    }

    /**
     * Constructs an {@code AbstractEditDialog}.
     *
     * @param editor  the editor
     * @param buttons the buttons to display
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param context the context
     */
    public AbstractEditDialog(IMObjectEditor editor, String[] buttons, boolean save, Context context) {
        this(editor, editor.getTitle(), buttons, save, context, editor.getHelpContext());
    }

    /**
     * Constructs an {@code AbstractEditDialog}.
     *
     * @param editor  the editor. May be {@code null}
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param context the context
     * @param help    the help context
     */
    protected AbstractEditDialog(IMObjectEditor editor, String title, String[] buttons, boolean save,
                                 Context context, HelpContext help) {
        super(title, STYLE, buttons, help);
        this.context = context;
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
     * @return the editor, or {@code null} if none has been set
     */
    public IMObjectEditor getEditor() {
        return editor;
    }

    /**
     * Saves the editor, optionally closing the dialog.
     * <p/>
     * If the the save fails, the dialog will remain open.
     *
     * @param close if {@code true} close the dialog
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
     * @param skip if {@code true} add a skip button, otherwise remove it
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
     * @param editor the editor. May be {@code null}
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
        List<Selection> path;
        if (previous != null) {
            path = previous.getSelectionPath();
            removeEditor(previous);
        } else {
            path = null;
        }
        if (editor != null) {
            addEditor(editor);
            if (path != null) {
                editor.setSelectionPath(path);
            }
        }
    }

    /**
     * Saves the current object, if saving is enabled.
     * <p/>
     * If it is, and the object is valid, then {@link #doSave()} is called. If {@link #doSave()} fails
     * (i.e returns {@code false}), then {@link #saveFailed()} is called.
     *
     * @return {@code true} if the object was saved
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
     * @return {@code true} if the object was saved
     */
    protected boolean doSave() {
        return (editor != null && save(editor));
    }

    /**
     * Saves the editor in a transaction.
     *
     * @param editor the editor
     * @return {@code true} if the save was successful
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
     * Adds the editor to the layout, setting the focus if the dialog is displayed.
     *
     * @param editor the editor
     */
    protected void addEditor(IMObjectEditor editor) {
        getEditorContainer().add(editor.getComponent());
        getFocusGroup().add(0, editor.getFocusGroup());

        if (getParent() != null) {
            // focus in the editor
            editor.getFocusGroup().setFocus();
        }
    }

    /**
     * Removes the editor from the layout.
     *
     * @param editor the editor to remove
     */
    protected void removeEditor(IMObjectEditor editor) {
        getEditorContainer().remove(editor.getComponent());
        getFocusGroup().remove(editor.getFocusGroup());
    }

    /**
     * Returns the component containing the editor.
     * <p/>
     * This implementation returns {@link #getLayout()}.
     *
     * @return the editor container
     */
    protected Component getEditorContainer() {
        return getLayout();
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Invoked when the component changes.
     *
     * @param event the component change event
     */
    protected void onComponentChange(PropertyChangeEvent event) {
        Component container = getEditorContainer();
        container.remove((Component) event.getOldValue());
        container.add((Component) event.getNewValue());
    }

    /**
     * Displays the macros.
     */
    protected void onMacro() {
        MacroDialog dialog = new MacroDialog(context, getHelpContext());
        dialog.show();
    }

    /**
     * Determines if saving has been disabled.
     *
     * @return {@code true} if saves are disabled
     */
    protected boolean isSaveDisabled() {
        return savedDisabled;
    }

    /**
     * Returns the help context.
     * <p/>
     * This implementation returns the help context of the editor, if one is registered
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return (editor != null) ? editor.getHelpContext() : super.getHelpContext();
    }

    /**
     * Helper to determine which buttons should be displayed.
     *
     * @param apply  if {@code true} provide apply and OK buttons
     * @param cancel if {@code true} provide a cancel button
     * @param skip   if {@code true} provide a skip button
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
