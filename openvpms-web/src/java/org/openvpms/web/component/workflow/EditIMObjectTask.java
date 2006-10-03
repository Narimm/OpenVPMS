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
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ErrorHelper;


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
     * Properties to create the object with.
     */
    private TaskProperties createProperties;

    /**
     * Determines if the object should be edited without displaying a UI.
     */
    private final boolean background;


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
        this(shortName, create, false);
    }

    /**
     * Constructs a new <code>EditIMObjectTask</code>, to edit an object
     * in the {@link TaskContext} or create and edit a new one.
     *
     * @param shortName  the object short name
     * @param create     if <code>true</code>, create the object
     * @param background the if <code>true</code> create an editor but don't
     *                   display it
     */
    public EditIMObjectTask(String shortName, boolean create,
                            boolean background) {
        this.shortName = shortName;
        this.create = create;
        this.background = background;
    }

    /**
     * Constructs a new <code>EditIMObjectTask</code> to create and edit
     * a new <code>IMObject</code>
     *
     * @param shortName        the object short name
     * @param createProperties the properties to create the object with.
     *                         May be <code>null</code>
     * @param background       the if <code>true</code> create an editor but don't
     *                         display it
     */
    public EditIMObjectTask(String shortName, TaskProperties createProperties,
                            boolean background) {
        this.shortName = shortName;
        create = true;
        this.createProperties = createProperties;
        this.background = background;
    }

    /**
     * Constructs a new <code>EditIMObjectTask</code>.
     *
     * @param object the object to edit
     */
    public EditIMObjectTask(IMObject object) {
        this(object, false);
    }

    /**
     * Constructs a new <code>EditIMObjectTask</code>.
     *
     * @param object     the object to edit
     * @param background the if <code>true</code> create an editor but don't
     *                   display it
     */
    public EditIMObjectTask(IMObject object, boolean background) {
        this.object = object;
        this.background = background;
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
                creator.setTaskListener(new TaskListener() {
                    public void taskEvent(TaskEvent event) {
                        switch (event.getType()) {
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
    protected void edit(IMObject object, TaskContext context) {
        try {
            LayoutContext layout = new DefaultLayoutContext(true);
            layout.setContext(context);
            final IMObjectEditor editor
                    = IMObjectEditorFactory.create(object, layout);
            GlobalContext.getInstance().setCurrent(object);
            if (background) {
                editor.getComponent();
                GlobalContext.getInstance().setCurrent(null);
                if (editor.isValid()) {
                    if (editor.save()) {
                        notifyCompleted();
                    } else {
                        notifyCancelled();
                    }
                } else {
                    // editor invalid. Pop up a dialog.
                    show(editor, layout);
                }
            } else {
                show(editor, layout);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            notifyCancelled();
        }
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     */
    protected void onEditCompleted(IMObjectEditor editor) {
        GlobalContext.getInstance().setCurrent(null);
        if (editor.isDeleted() || editor.isCancelled()) {
            notifyCancelled();
        } else {
            notifyCompleted();
        }
    }

    /**
     * Shows the editor in an edit dialog.
     *
     * @param editor the editor
     * @param layout the layout context
     */
    private void show(final IMObjectEditor editor, LayoutContext layout) {
        EditDialog dialog = new EditDialog(editor, layout);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                onEditCompleted(editor);
            }
        });
        dialog.show();
    }

}