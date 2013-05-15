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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.patient.visit;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
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
import org.openvpms.web.echo.help.HelpContext;

/**
 * Links a patient reminder/alerts browser to a CRUD window.
 *
 * @author Tim Anderson
 */
public class ReminderBrowserCRUDWindow extends BrowserCRUDWindow<Act> {

    /**
     * The reminder statuses to query.
     */
    private static final ActStatuses STATUSES = new ActStatuses(ReminderArchetypes.REMINDER);

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
        = new SortConstraint[]{new NodeSortConstraint("startTime", false)};


    /**
     * Constructs a {@code ReminderBrowserCRUDWindow}.
     *
     * @param patient the patient
     * @param context the context
     * @param help    the help context
     */
    public ReminderBrowserCRUDWindow(Party patient, Context context, HelpContext help) {
        Query<Act> query = createReminderAlertQuery(patient);
        Browser<Act> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        setBrowser(browser);

        VisitReminderCRUDWindow window = new VisitReminderCRUDWindow(patient, context, help);
        setWindow(window);
    }

    /**
     * Creates a new query, for the reminder/alert view.
     *
     * @return a new query
     */
    private Query<Act> createReminderAlertQuery(Party patient) {
        String[] shortNames = {ReminderArchetypes.REMINDER, PatientArchetypes.ALERT};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
            patient, "patient", PatientArchetypes.PATIENT_PARTICIPATION, shortNames, STATUSES);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        return query;
    }
}
