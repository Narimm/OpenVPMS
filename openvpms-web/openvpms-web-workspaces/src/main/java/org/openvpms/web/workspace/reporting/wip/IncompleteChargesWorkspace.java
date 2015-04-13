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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.wip;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;


/**
 * Workspace to detail customer charges that are works-in-progress, i.e not POSTED.
 *
 * @author Tim Anderson
 */
public class IncompleteChargesWorkspace extends ResultSetCRUDWorkspace<Act> {

    /**
     * Constructs an {@code IncompleteChargesWorkspace}.
     *
     * @param context the context
     */
    public IncompleteChargesWorkspace(Context context, MailContext mailContext) {
        super("reporting", "wip", context);
        setArchetypes(Archetypes.create(IncompleteChargesQuery.SHORT_NAMES, Act.class));
        setMailContext(mailContext);
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Act> createQuery() {
        return new IncompleteChargesQuery(new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(getContext(), getHelpContext());
        layoutContext.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
        IMObjectTableModel<Act> model = new IncompleteChargesTableModel(layoutContext);
        return BrowserFactory.create(query, null, model, layoutContext);
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return {@code true}
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Act> createCRUDWindow() {
        QueryBrowser<Act> browser = getBrowser();
        return new IncompleteChargesCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(),
                                               getContext(), getHelpContext());
    }

    class IncompleteChargesTableModel extends AbstractActTableModel {

        public IncompleteChargesTableModel(LayoutContext context) {
            super(IncompleteChargesQuery.SHORT_NAMES, context);
        }

        /**
         * Returns a list of descriptor names to include in the table.
         *
         * @return the list of descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return new String[]{"customer", "status", "startTime", "amount"};
        }
    }
}
