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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.macro.MacroDialog;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.VetoListener;
import org.openvpms.web.echo.event.Vetoable;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.springframework.transaction.TransactionStatus;

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
     * The alert manager.
     */
    private final AlertManager alerts;

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
     * The current component.
     */
    private Component current;

    /**
     * The current component focus group.
     */
    private FocusGroup currentGroup;

    /**
     * The current help context.
     */
    private HelpContext helpContext;

    /**
     * Edit dialog style name.
     */
    protected static final String STYLE = "EditDialog";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractEditDialog.class);

    /**
     * Constructs an {@link AbstractEditDialog}.
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
     * Constructs an {@link AbstractEditDialog}.
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
     * Constructs an {@link AbstractEditDialog}.
     *
     * @param editor  the editor. May be {@code null}
     * @param title   the dialog title. May be {@code null}
     * @param buttons the buttons to display
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param context the context
     * @param help    the help context. May be {@code null}
     */
    protected AbstractEditDialog(IMObjectEditor editor, String title, String[] buttons, boolean save,
                                 Context context, HelpContext help) {
        super(title, STYLE, buttons, help);
        this.context = context;
        alerts = new AlertManager(getContentPane(), 2);
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
     * Saves the current object, if saving is enabled.
     * <p/>
     * If it is, and the object is valid, then {@link #doSave(IMObjectEditor)} is called.
     * If {@link #doSave(IMObjectEditor)} fails (i.e returns {@code false}), then {@link #saveFailed()} is called.
     *
     * @return {@code true} if the object was saved
     */
    public boolean save() {
        boolean result = false;
        if (canSave()) {
            IMObjectEditorSaver saver = new IMObjectEditorSaver() {
                @Override
                protected void save(IMObjectEditor editor, TransactionStatus status) {
                    AbstractEditDialog.this.doSave(editor);
                }

                @Override
                protected boolean reload(IMObjectEditor editor) {
                    return AbstractEditDialog.this.reload(editor);
                }

                @Override
                protected void failed(IMObjectEditor editor, Throwable exception) {
                    super.failed(editor, exception);
                    saveFailed();
                }

                @Override
                protected void reloaded(String title, String message) {
                    AbstractEditDialog.this.reloaded(title, message);
                }
            };
            result = saver.save(editor);
        }
        return result;
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
     * Returns the help context.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return (helpContext != null) ? helpContext : super.getHelpContext();
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
     * Determines if the current object can be saved.
     *
     * @return {@code true} if the current object can be saved
     */
    protected boolean canSave() {
        return !savedDisabled && save && editor != null;
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
     * <p/>
     * If there is an existing editor, its selection path will be set on the editor.
     *
     * @param editor the editor. May be {@code null}
     */
    protected void setEditor(IMObjectEditor editor) {
        IMObjectEditor previous = this.editor;
        List<Selection> path = (editor != null && previous != null) ? previous.getSelectionPath() : null;
        setEditor(editor, path);
    }

    /**
     * Sets the editor.
     *
     * @param editor the editor. May be {@code null}
     * @param path   the selection path. May be {@code null}
     */
    protected void setEditor(IMObjectEditor editor, List<Selection> path) {
        IMObjectEditor previous = this.editor;
        if (editor != null) {
            setTitle(editor.getTitle());
            editor.addPropertyChangeListener(
                    IMObjectEditor.COMPONENT_CHANGED_PROPERTY, new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent event) {
                            onComponentChange();
                        }
                    });
        }
        this.editor = editor;
        if (previous != null) {
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
     * Saves the current object.
     *
     * @param editor the editor
     * @throws OpenVPMSException if the save fails
     */
    protected void doSave(IMObjectEditor editor) {
        editor.save();
    }

    /**
     * Invoked to reload the object being edited when save fails.
     *
     * @param editor the editor
     * @return a {@code true} if the editor was reloaded
     */
    protected boolean reload(IMObjectEditor editor) {
        IMObjectEditor newEditor = null;
        try {
            newEditor = editor.newInstance();
            if (newEditor != null) {
                setEditor(newEditor);
            }
        } catch (Throwable exception) {
            log.error("Failed to reload editor", exception);
        }
        return newEditor != null;
    }

    /**
     * Invoked to display a message that saving failed, and the editor has been reverted.
     *
     * @param title   the message title
     * @param message the message
     */
    protected void reloaded(String title, String message) {
        ErrorHandler.getInstance().error(title, message, null, null);
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
        setComponent(editor.getComponent(), editor.getFocusGroup(), editor.getHelpContext());

        // register a listener to handle alerts
        editor.setAlertListener(getAlertListener());
    }

    /**
     * Removes the editor from the layout.
     *
     * @param editor the editor to remove
     */
    protected void removeEditor(IMObjectEditor editor) {
        editor.setAlertListener(null);
        removeComponent();
    }

    /**
     * Sets the component.
     *
     * @param component the component
     * @param group     the focus group
     * @param context   the help context
     */
    protected void setComponent(Component component, FocusGroup group, HelpContext context) {
        setComponent(component, group, context, true);
    }

    /**
     * Sets the component.
     *
     * @param component the component
     * @param group     the focus group
     * @param context   the help context
     * @param focus     if {@code true}, move the focus
     */
    protected void setComponent(Component component, FocusGroup group, HelpContext context, boolean focus) {
        SplitPane layout = getLayout();
        if (current != null) {
            layout.remove(current);
        }
        if (currentGroup != null) {
            getFocusGroup().remove(currentGroup);
        }
        layout.add(component);
        getFocusGroup().add(0, group);
        if (focus && getParent() != null) {
            // focus in the component
            group.setFocus();
        }
        current = component;
        currentGroup = group;
        helpContext = context;
    }

    /**
     * Removes the existing component and any alerts.
     */
    protected void removeComponent() {
        if (current != null) {
            getLayout().remove(current);
            current = null;
        }
        alerts.clear();
        if (currentGroup != null) {
            getFocusGroup().remove(currentGroup);
            currentGroup = null;
        }
        helpContext = null;
    }

    /**
     * Returns the alert listener.
     *
     * @return the alert listener
     */
    protected AlertListener getAlertListener() {
        return alerts.getListener();
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
     * Invoked when the editor component changes.
     */
    private void onComponentChange() {
        setComponent(editor.getComponent(), editor.getFocusGroup(), editor.getHelpContext(), false);
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