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
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.IMObjectTableBrowserFactory;
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
        query = QueryFactory.create(shortName, context);
        type = getType(query.getShortNames());
        this.createTask = createTask;
    }

    /**
     * Constructs a new <tt>SelectIMObjectTask</tt>.
     * The selected object updates the local context.
     *
     * @param query the query
     */
    public SelectIMObjectTask(Query<T> query) {
        this(getType(query.getShortNames()), query, null);
    }

    /**
     * Constructs a new <tt>SelectIMObjectTask</tt>.
     *
     * @param type       the collective noun for the types this may select
     * @param query      the query
     * @param createTask if non-null, handles creation of new objects
     */
    public SelectIMObjectTask(String type, Query<T> query,
                              Task createTask) {
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
     * <p/>
     * If none is specified, no message will be displayed.
     */
    public void setMessage(String message) {
        this.message = message;
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
        Browser<T> browser = IMObjectTableBrowserFactory.create(query);
        if (title == null) {
            title = Messages.get("imobject.select.title", type);
        }
        String[] buttons = isRequired()
                ? PopupDialog.CANCEL : PopupDialog.SKIP_CANCEL;
        boolean addNew = (createTask != null);
        final BrowserDialog<T> dialog
                = new BrowserDialog<T>(title, message, buttons, browser,
                                       addNew);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (dialog.createNew()) {
                    createTask.addTaskListener(getTaskListeners());
                    createTask.start(context);
                } else {
                    T selected = dialog.getSelected();
                    if (selected != null) {
                        context.addObject(selected);
                        notifyCompleted();
                    } else if (PopupDialog.SKIP_ID.equals(dialog.getAction())) {
                        notifySkipped();
                    } else {
                        notifyCancelled();
                    }
                }
            }
        });
        dialog.show();
    }

}
