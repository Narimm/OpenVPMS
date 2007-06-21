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

package org.openvpms.web.component.workflow;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Task to edit an {@link IMObject} using an {@link IMObjectEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * This only applies when {@link #interactive} is <tt>false</tt>.
     */
    private boolean showEditorOnError = true;


    /**
     * Constructs a new <code>EditIMObjectTask</code> to edit an object
     * in the {@link TaskContext}.
     *
     * @param shortName the short name of the object to edit
     */
    public EditIMObjectTask(String shortName) {
        this(shortName, false);
    }

    /**
     * Constructs a new <code>EditIMObjectTask</code>, to edit an object
     * in the {@link TaskContext} or create and edit a new one.
     *
     * @param shortName the object short name
     * @param create    if <code>true</code>, create the object
     */
    public EditIMObjectTask(String shortName, boolean create) {
        this(shortName, create, true);
    }

    /**
     * Constructs a new <tt>EditIMObjectTask</tt>, to edit an object
     * in the {@link TaskContext} or create and edit a new one.
     *
     * @param shortName   the object short name
     * @param create      if <tt>true</tt>, create the object
     * @param interactive if <tt>true</tt> create an editor and display it;
     *                    otherwise create it but don't display it
     */
    public EditIMObjectTask(String shortName, boolean create,
                            boolean interactive) {
        this.shortName = shortName;
        this.create = create;
        this.interactive = interactive;
    }

    /**
     * Constructs a new <tt>EditIMObjectTask</tt> to create and edit
     * a new <tt>IMObject</tt>.
     *
     * @param shortName        the object short name
     * @param createProperties the properties to create the object with.
     *                         May be <code>null</code>
     * @param interactive      if <tt>true</tt> create an editor and display it;
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
     * Constructs a new <code>EditIMObjectTask</code>.
     *
     * @param object the object to edit
     */
    public EditIMObjectTask(IMObject object) {
        this(object, true);
    }

    /**
     * Constructs a new <code>EditIMObjectTask</code>.
     *
     * @param object      the object to edit
     * @param interactive if <tt>true</tt> create an editor and display it;
     *                    otherwise create it but don't display it
     */
    public EditIMObjectTask(IMObject object, boolean interactive) {
        this.object = object;
        this.interactive = interactive;
    }

    /**
     * Determines if editing may be skipped.
     * Note that the object may have changed prior to editing being skipped.
     * Defaults to <tt>false</tt>.
     *
     * @param skip if <tt>true</tt> editing may be skipped.
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /**
     * Determines if the editor should be displayed if the object is invalid.
     * This only applies when non-interactive editing was specified at
     * construction. Defaults to <tt>true</tt>.
     *
     * @param show if <tt>true</tt> display the editor if the object is invalid
     */
    public void setShowEditorOnError(boolean show) {
        showEditorOnError = show;
    }

    /**
     * Determines if the object should be deleted if the task is cancelled
     * or skipped. Defaults to <code>false</code>.
     * Note that no checking is performed to see if the object participates
     * in entity relationships before being deleted. To do this,
     * use {@link IMObjectDeletor} instead.
     * Defaults to <tt>false</tt>
     *
     * @param delete if <tt>true</tt> delete the object on cancel or skip
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
                creator.addTaskListener(new TaskListener() {
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
                creator.start(context);
            } else {
                edit(context);
            }
        } else {
            edit(object, context);
        }
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
     *
     * @param object  the object to edit
     * @param context the task context
     */
    protected void edit(final IMObject object, TaskContext context) {
        try {
            LayoutContext layout = new DefaultLayoutContext(true);
            layout.setContext(context);
            final IMObjectEditor editor
                    = IMObjectEditorFactory.create(object, layout);
            GlobalContext.getInstance().setCurrent(object);
            if (interactive) {
                show(editor);
            } else {
                editor.getComponent();
                GlobalContext.getInstance().setCurrent(null);
                edit(editor, context);
                if (editor.isValid() || !showEditorOnError) {
                    if (editor.save()) {
                        notifyCompleted();
                    } else {
                        if (deleteOnCancelOrSkip) {
                            delete(object);
                        }
                        notifyCancelled();
                    }
                } else {
                    // editor invalid. Pop up a dialog.
                    show(editor);
                }
            }
        } catch (OpenVPMSException exception) {
            if (deleteOnCancelOrSkip) {
                delete(object);
            }
            notifyCancelledOnError(exception);
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
     * Shows the editor in an edit dialog.
     *
     * @param editor the editor
     */
    protected void show(final IMObjectEditor editor) {
        final EditDialog dialog = new EditDialog(editor, true, skip);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (EditDialog.SKIP_ID.equals(dialog.getAction())) {
                    if (deleteOnCancelOrSkip) {
                        delete(editor.getObject());
                    }
                    notifySkipped();
                } else {
                    onEditCompleted(editor);
                }
            }

        });
        dialog.show();
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     */
    protected void onEditCompleted(IMObjectEditor editor) {
        GlobalContext.getInstance().setCurrent(null);
        if (editor.isDeleted() || editor.isCancelled()) {
            if (editor.isCancelled() && deleteOnCancelOrSkip) {
                delete(editor.getObject());
            }
            notifyCancelled();
        } else {
            notifyCompleted();
        }
    }

    /**
     * Deletes an object.
     *
     * @param object the object to delete
     */
    private void delete(IMObject object) {
        if (!object.isNew()) {
            try {
                object = IMObjectHelper.reload(object);
                if (object != null) {
                    // make sure the the last saved instance is being deleted
                    // to avoid validation errors
                    ArchetypeServiceHelper.getArchetypeService().remove(object);
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

}