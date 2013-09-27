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

package org.openvpms.web.component.workflow;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletionListener;
import org.openvpms.web.component.im.util.IMObjectDeleter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.SilentIMObjectDeleter;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Task to edit an {@link IMObject} using an {@link IMObjectEditor}.
 *
 * @author Tim Anderson
 */
public class EditIMObjectTask extends AbstractTask {

    /**
     * The object to edit.
     */
    private IMObject object;

    /**
     * The short name of the object to edit.
     */
    private String shortName;

    /**
     * Determines if the object should be created.
     */
    private boolean create;

    /**
     * Determines if editing may be skipped.
     */
    private boolean skip;

    /**
     * Determines if the object should be deleted on cancel or skip.
     */
    private boolean deleteOnCancelOrSkip;

    /**
     * Properties to create the object with.
     */
    private TaskProperties createProperties;

    /**
     * Determines if the object should be edited displaying a UI.
     */
    private final boolean interactive;

    /**
     * Determines if the UI should be displayed if a the object is invalid.
     * This only applies when {@link #interactive} is {@code false}.
     */
    private boolean showEditorOnError = true;

    /**
     * The current edit dialog.
     */
    private EditDialog dialog;


    /**
     * Constructs a new {@code EditIMObjectTask} to edit an object
     * in the {@link TaskContext}.
     *
     * @param shortName the short name of the object to edit
     */
    public EditIMObjectTask(String shortName) {
        this(shortName, false);
    }

    /**
     * Constructs a new {@code EditIMObjectTask}, to edit an object
     * in the {@link TaskContext} or create and edit a new one.
     *
     * @param shortName the object short name
     * @param create    if {@code true}, create the object
     */
    public EditIMObjectTask(String shortName, boolean create) {
        this(shortName, create, true);
    }

    /**
     * Constructs a new {@code EditIMObjectTask}, to edit an object
     * in the {@link TaskContext} or create and edit a new one.
     *
     * @param shortName   the object short name
     * @param create      if {@code true}, create the object
     * @param interactive if {@code true} create an editor and display it;
     *                    otherwise create it but don't display it
     */
    public EditIMObjectTask(String shortName, boolean create,
                            boolean interactive) {
        this.shortName = shortName;
        this.create = create;
        this.interactive = interactive;
    }

    /**
     * Constructs a new {@code EditIMObjectTask} to create and edit
     * a new {@code IMObject}.
     *
     * @param shortName        the object short name
     * @param createProperties the properties to create the object with.
     *                         May be {@code null}
     * @param interactive      if {@code true} create an editor and display it;
     *                         otherwise create it but don't display it
     */
    public EditIMObjectTask(String shortName, TaskProperties createProperties,
                            boolean interactive) {
        this.shortName = shortName;
        create = true;
        this.createProperties = createProperties;
        this.interactive = interactive;
    }

    /**
     * Constructs a new {@code EditIMObjectTask}.
     *
     * @param object the object to edit
     */
    public EditIMObjectTask(IMObject object) {
        this(object, true);
    }

    /**
     * Constructs a new {@code EditIMObjectTask}.
     *
     * @param object      the object to edit
     * @param interactive if {@code true} create an editor and display it;
     *                    otherwise create it but don't display it
     */
    public EditIMObjectTask(IMObject object, boolean interactive) {
        this.object = object;
        this.interactive = interactive;
    }

    /**
     * Determines if editing may be skipped.
     * Note that the object may have changed prior to editing being skipped.
     * Defaults to {@code false}.
     *
     * @param skip if {@code true} editing may be skipped.
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /**
     * Determines if the editor should be displayed if the object is invalid.
     * This only applies when non-interactive editing was specified at
     * construction. Defaults to {@code true}.
     *
     * @param show if {@code true} display the editor if the object is invalid
     */
    public void setShowEditorOnError(boolean show) {
        showEditorOnError = show;
    }

    /**
     * Determines if the object should be deleted if the task is cancelled
     * or skipped. Defaults to {@code false}.
     * Note that no checking is performed to see if the object participates
     * in entity relationships before being deleted. To do this,
     * use {@link IMObjectDeleter} instead.
     * Defaults to {@code false}
     *
     * @param delete if {@code true} delete the object on cancel or skip
     */
    public void setDeleteOnCancelOrSkip(boolean delete) {
        deleteOnCancelOrSkip = delete;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        if (object == null) {
            if (create) {
                CreateIMObjectTask creator
                        = new CreateIMObjectTask(shortName, createProperties);
                creator.addTaskListener(new DefaultTaskListener() {
                    public void taskEvent(TaskEvent event) {
                        switch (event.getType()) {
                            case SKIPPED:
                                notifySkipped();
                                break;
                            case CANCELLED:
                                notifyCancelled();
                                break;
                            case COMPLETED:
                                edit(context);
                        }
                    }
                });
                start(creator, context);
            } else {
                edit(context);
            }
        } else {
            edit(object, context);
        }
    }

    /**
     * Returns the edit dialog.
     *
     * @return the edit dialog, or {@code null} if none is being displayed
     */
    public EditDialog getEditDialog() {
        return dialog;
    }

    /**
     * Edits an object located in the context.
     *
     * @param context the task context
     */
    protected void edit(TaskContext context) {
        IMObject object = context.getObject(shortName);
        if (object != null) {
            edit(object, context);
        } else {
            notifyCancelled();
        }
    }

    /**
     * Edits an object.
     * <p/>
     * Creates a new editor via {@link #createEditor} before delegating
     * to {@link #interactiveEdit}, if editing is interactive, or {@link #backgroundEdit} if not.
     *
     * @param object  the object to edit
     * @param context the task context
     */
    protected void edit(IMObject object, TaskContext context) {
        HelpContext help = context.getHelpContext().topic(object, "edit");
        context = new DefaultTaskContext(context, null, help);

        try {
            final IMObjectEditor editor = createEditor(object, context);
            if (interactive) {
                interactiveEdit(editor, context);
            } else {
                backgroundEdit(editor, context);
            }
        } catch (OpenVPMSException exception) {
            if (deleteOnCancelOrSkip) {
                delete(object, context);
            }
            notifyCancelledOnError(exception);
        }
    }

    /**
     * Creates a new editor for an object.
     *
     * @param object  the object to edit
     * @param context the task context
     * @return a new editor
     */
    protected IMObjectEditor createEditor(IMObject object, TaskContext context) {
        LayoutContext layout = new DefaultLayoutContext(true, context, context.getHelpContext());
        return IMObjectEditorFactory.create(object, layout);
    }

    /**
     * Shows the editor in an edit dialog.
     *
     * @param editor  the editor
     * @param context the task context
     */
    protected void interactiveEdit(final IMObjectEditor editor, final TaskContext context) {
        context.setCurrent(object);
        dialog = createEditDialog(editor, skip, context);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                context.setCurrent(null);
                String action = dialog.getAction();
                dialog = null;
                if (PopupDialog.OK_ID.equals(action)) {
                    onEditCompleted();
                } else if (PopupDialog.SKIP_ID.equals(action)) {
                    onEditSkipped(editor, context);
                } else {
                    onEditCancelled(editor, context);
                }
            }

        });
        dialog.show();
    }

    /**
     * Attempts to edit an object in the background. Editing is delegated
     * to {@link #edit(IMObjectEditor, TaskContext)}.
     * <p/>
     * If the editor is valid after editing, the object will be saved.
     * If not,  and {@link #showEditorOnError} is {@code true}, an interactive
     * edit will occur, otherwise the edit will be cancelled.
     *
     * @param editor  the editor
     * @param context the task context
     */
    protected void backgroundEdit(IMObjectEditor editor, TaskContext context) {
        context.setCurrent(null);
        editor.getComponent();
        edit(editor, context);
        if (editor.isValid() || !showEditorOnError) {
            if (SaveHelper.save(editor)) {
                notifyCompleted();
            } else {
                if (deleteOnCancelOrSkip) {
                    delete(editor.getObject(), context);
                }
                notifyCancelled();
            }
        } else {
            // editor invalid. Pop up a dialog.
            interactiveEdit(editor, context);
        }
    }

    /**
     * Edits an object in the background.
     * This implementation is a no-op.
     *
     * @param editor  the editor
     * @param context the task context
     */
    protected void edit(IMObjectEditor editor, TaskContext context) {
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor  the editor
     * @param skip    if {@code true}, editing may be skipped
     * @param context the help context
     * @return a new edit dialog
     */
    protected EditDialog createEditDialog(IMObjectEditor editor, boolean skip, TaskContext context) {
        EditDialog dialog = EditDialogFactory.create(editor, context);
        dialog.addSkip(skip);
        return dialog;
    }

    /**
     * Invoked when editing is complete.
     */
    protected void onEditCompleted() {
        notifyCompleted();
    }

    /**
     * Invoked when editing is skipped.
     *
     * @param editor  the editor
     * @param context the task context
     */
    protected void onEditSkipped(IMObjectEditor editor, TaskContext context) {
        if (deleteOnCancelOrSkip) {
            delete(editor.getObject(), context);
        }
        notifySkipped();
    }

    /**
     * Invoked when editing is cancelled.
     *
     * @param editor  the editor
     * @param context the task context
     */
    protected void onEditCancelled(IMObjectEditor editor, TaskContext context) {
        if (deleteOnCancelOrSkip) {
            delete(editor.getObject(), context);
        }
        notifyCancelled();
    }

    /**
     * Deletes an object.
     *
     * @param object  the object to delete
     * @param context the task context
     */
    private void delete(IMObject object, TaskContext context) {
        if (!object.isNew()) {
            try {
                object = IMObjectHelper.reload(object);
                if (object != null) {
                    // make sure the the last saved instance is being deleted to avoid validation errors
                    IMObjectDeleter deleter = new SilentIMObjectDeleter(context);
                    deleter.delete(object, context.getHelpContext(), new DefaultIMObjectDeletionListener());
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

}