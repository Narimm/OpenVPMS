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
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.TableBrowser;
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
     * Determines if the selected object should update the global or local
     * context.
     */
    private final boolean global;


    /**
     * Constructs a new <code>SelectIMObjectTask</code>.
     * The selected object updates the local context.
     *
     * @param shortName the short name to query on. May contain wildcards
     */
    public SelectIMObjectTask(String shortName) {
        this(shortName, false);
    }

    /**
     * Constructs a new <code>SelectIMObjectTask</code>.
     *
     * @param shortName the short name to query on. May contain wildcards
     * @param global    if <code>true</code> update the global context;
     *                  othwerwise update the local context
     */
    public SelectIMObjectTask(String shortName, boolean global) {
        query = QueryFactory.create(shortName);
        type = getType(query.getShortNames());
        this.global = global;
    }

    /**
     * Constructs a new <code>SelectIMObjectTask</code>.
     * The selected object updates the local context.
     *
     * @param query the query
     */
    public SelectIMObjectTask(Query<T> query) {
        this(getType(query.getShortNames()), query, false);
    }

    /**
     * Constructs a new <code>SelectIMObjectTask</code>.
     *
     * @param query  the query
     * @param global if <code>true</code> update the global context; othwerwise
     *               update the local context
     */
    public SelectIMObjectTask(Query<T> query, boolean global) {
        this(getType(query.getShortNames()), query, global);
    }

    /**
     * Constructs a new <code>SelectIMObjectTask</code>.
     *
     * @param type   the collective noun for the types this may select
     * @param query  the query
     * @param global if <code>true</code> update the global context;
     *               othwerwise update the local context
     */
    public SelectIMObjectTask(String type, Query<T> query, boolean global) {
        this.type = type;
        this.query = query;
        this.global = global;
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
        Browser<T> browser = new TableBrowser<T>(query);
        String title = Messages.get(
                "imobject.select.title", type);
        final BrowserDialog<T> dialog = new BrowserDialog<T>(title, browser);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                T selected = dialog.getSelected();
                if (selected != null) {
                    context.addObject(selected, global);
                    notifyCompleted();
                } else {
                    notifyCancelled();
                }
            }
        });
        dialog.show();
    }


}
