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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.echo.i18n.Messages;


/**
 * Task to select an {@link IMObject}.
 *
 * @author Tim Anderson
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
     * The help context.
     */
    private final HelpContext help;

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
     * Constructs a {@code SelectIMObjectTask}.
     *
     * @param shortName the short name to query on. May contain wildcards
     * @param context   the context
     * @param help      the help context
     */
    public SelectIMObjectTask(String shortName, Context context, HelpContext help) {
        this(shortName, context, null, help);
    }

    /**
     * Constructs a {@code SelectIMObjectTask}.
     *
     * @param shortName  the short name to query on. May contain wildcards
     * @param context    the context
     * @param createTask if non-null, handles creation of new objects
     * @param help       the help context
     */
    public SelectIMObjectTask(String shortName, Context context, Task createTask, HelpContext help) {
        query = QueryFactory.create(shortName, context, IMObject.class);
        type = getType(query.getShortNames());
        this.createTask = createTask;
        this.help = help;
    }

    /**
     * Constructs a {@code SelectIMObjectTask}.
     *
     * @param query the query
     * @param help  the help context
     */
    public SelectIMObjectTask(Query<T> query, HelpContext help) {
        this(getType(query.getShortNames()), query, null, help);
    }

    /**
     * Constructs a {@code SelectIMObjectTask}.
     *
     * @param query      the query
     * @param createTask if non-null, handles creation of new objects
     * @param help       the help context
     */
    public SelectIMObjectTask(Query<T> query, Task createTask, HelpContext help) {
        this(getType(query.getShortNames()), query, createTask, help);
    }

    /**
     * Constructs a {@code SelectIMObjectTask}.
     *
     * @param type       the collective noun for the types this may select
     * @param query      the query
     * @param createTask if non-null, handles creation of new objects
     * @param help       the help context
     */
    public SelectIMObjectTask(String type, Query<T> query, Task createTask, HelpContext help) {
        this.type = type;
        this.query = query;
        this.createTask = createTask;
        this.help = help;
    }

    /**
     * Sets the dialog title.
     * <p/>
     * If none is specified, one will be generated from the type of objects
     * being queried.
     *
     * @param title the dialog title. May be {@code null}
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the dialog message.
     *
     * @param message the message. May be {@code null}
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the browser dialog.
     *
     * @return the browser dialog, or {@code null} if none is being displayed
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
        LayoutContext layout = new DefaultLayoutContext(context, help.subtopic("select"));
        Browser<T> browser = BrowserFactory.create(query, layout);
        if (title == null) {
            title = Messages.get("imobject.select.title", type);
        }
        String[] buttons = isRequired() ? PopupDialog.CANCEL : PopupDialog.SKIP_CANCEL;
        boolean addNew = (createTask != null);
        dialog = new BrowserDialog<T>(title, message, buttons, browser, addNew, layout.getHelpContext());
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
