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

package org.openvpms.web.workspace.patient.visit;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Links an estimates browser to a CRUD window.
 *
 * @author Tim Anderson
 */
public class EstimateBrowserCRUDWindow extends BrowserCRUDWindow<Act> {

    /**
     * The estimate statuses to query.
     */
    private static final ActStatuses STATUSES = new ActStatuses(EstimateArchetypes.ESTIMATE);

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
            = new SortConstraint[]{new NodeSortConstraint("startTime", false)};


    /**
     * Constructs a {@link EstimateBrowserCRUDWindow}.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param editor   the visit editor
     * @param context  the context
     * @param help     the help context
     */
    public EstimateBrowserCRUDWindow(Party customer, Party patient, VisitEditor editor, Context context,
                                     HelpContext help) {
        Query<Act> query = createQuery(customer, patient);
        Browser<Act> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        setBrowser(browser);

        VisitEstimateCRUDWindow estimateWindow = new VisitEstimateCRUDWindow(context, help);
        estimateWindow.setVisitEditor(editor);
        setWindow(estimateWindow);
    }

    /**
     * Creates a new query that returns all estimates for the specified customer and patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return a new query
     */
    private Query<Act> createQuery(Party customer, final Party patient) {
        String[] shortNames = {EstimateArchetypes.ESTIMATE};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
                customer, "customer", CustomerArchetypes.CUSTOMER_PARTICIPATION, shortNames, STATUSES) {
            @Override
            protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
                return new VisitEstimateResultSet(patient, getArchetypeConstraint(), getParticipantConstraint(),
                                                  getFrom(), getTo(), getStatuses(), getMaxResults(), sort);
            }
        };
        query.setDefaultSortConstraint(DEFAULT_SORT);
        return query;
    }
}
