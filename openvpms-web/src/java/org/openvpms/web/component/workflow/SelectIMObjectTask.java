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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Task to select an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SelectIMObjectTask<T extends IMObject> extends AbstractTask {

    /**
     * Collective noun for the types of objects this may select.
     */
    private final String type;

    /**
     * The query.
     */
    private final Query<T> query;

    /**
     * Task to delegate to if creation of a new object is selected.
     */
    private final Task createTask;

    /**
     * The dialog title.
     */
    private String title;

    /**
     * The dialog message.
     */
    private String message;

    /**
     * The browser dialog.
     */
    private BrowserDialog<T> dialog;


    /**
     * Constructs a new <tt>SelectIMObjectTask</tt>.
     *
     * @param shortName the short name to query on. May contain wildcards
     * @param context   the context
     */
    public SelectIMObjectTask(String shortName, Context context) {
        this(shortName, context, null);
    }

    /**
     * Constructs a new <tt>SelectIMObjectTask</tt>.
     *
     * @param shortName  the short name to query on. May contain wildcards
     * @param context    the context
     * @param createTask if non-null, handles creation of new objects
     */
    public SelectIMObjectTask(String shortName, Context context,
                              Task createTask) {
        query = QueryFactory.create(shortName, context, IMObject.class);
        type = getType(query.getShortNames());
        this.createTask = createTask;
    }

    /**
     * Constructs a <tt>SelectIMObjectTask</tt>.
     *
     * @param query the query
     */
    public SelectIMObjectTask(Query<T> query) {
        this(getType(query.getShortNames()), query, null);
    }

    /**
     * Constructs a <tt>SelectIMObjectTask</tt>.
     *
     * @param query      the query
     * @param createTask if non-null, handles creation of new objects
     */
    public SelectIMObjectTask(Query<T> query, Task createTask) {
        this(getType(query.getShortNames()), query, createTask);
    }

    /**
     * Constructs a <tt>SelectIMObjectTask</tt>.
     *
     * @param type       the collective noun for the types this may select
     * @param query      the query
     * @param createTask if non-null, handles creation of new objects
     */
    public SelectIMObjectTask(String type, Query<T> query, Task createTask) {
        this.type = type;
        this.query = query;
        this.createTask = createTask;
    }

    /**
     * Sets the dialog title.
     * <p/>
     * If none is specified, one will be generated from the type of objects
     * being queried.
     *
     * @param title the dialog title. May be <tt>null</tt>
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the dialog message.
     *
     * @param message the message. May be <tt>null</tt>
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the browser dialog.
     *
     * @return the browser dialog, or <tt>null</tt> if none is being displayed
     */
    public BrowserDialog<T> getBrowserDialog() {
        return dialog;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        Browser<T> browser = BrowserFactory.create(query);
        if (title == null) {
            title = Messages.get("imobject.select.title", type);
        }
        String[] buttons = isRequired() ? PopupDialog.CANCEL : PopupDialog.SKIP_CANCEL;
        boolean addNew = (createTask != null);
        dialog = new BrowserDialog<T>(title, message, buttons, browser, addNew);
        dialog.addWindowPaneListener(new PopupDialogListener() {

            @Override
            protected void onAction(PopupDialog dialog) {
                try {
                    super.onAction(dialog);
                } finally {
                    SelectIMObjectTask.this.dialog = null;
                }
            }

            @Override
            public void onOK() {
                T selected = dialog.getSelected();
                if (selected != null) {
                    context.addObject(selected);
                    notifyCompleted();
                } else {
                    notifyCancelled(); // shouldn't occur
                }
            }

            @Override
            public void onSkip() {
                notifySkipped();
            }

            @Override
            public void onCancel() {
                notifyCancelled();
            }

            /**
             * Invoked when an unknown button is selected.
             *
             * @param action the dialog action
             */
            @Override
            public void onAction(String action) {
                if (dialog.createNew()) {
                    onNew();
                } else {
                    notifyCancelled();
                }
            }

            /**
             * Invoked to create a new object.
             */
            private void onNew() {
                if (createTask != null) {
                    createTask.addTaskListener(new DefaultTaskListener() {
                        public void taskEvent(TaskEvent event) {
                            notifyEvent(event.getType());
                        }
                    });
                    start(createTask, context);
                } else {
                    // shouldn't occur
                    notifyCancelled();
                }
            }

        });
        dialog.show();
    }

}
