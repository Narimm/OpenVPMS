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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.summary;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.alert.Alert;
import org.openvpms.web.app.alert.AlertSummary;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Creates summary components for a given party.
 *
 * @author Tim Anderson
 */
public abstract class PartySummary {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@code PartySummary}.
     *
     * @param context the context
     * @param help    the help context
     */
    public PartySummary(Context context, HelpContext help) {
        this.context = context;
        this.help = help;
    }

    /**
     * Returns summary information for a party.
     * <p/>
     * The summary includes any alerts.
     *
     * @param party the party. May be {@code null}
     * @return a summary component, or {@code null} if there is no summary
     */
    public Component getSummary(Party party) {
        Component result = null;
        if (party != null) {
            result = createSummary(party);
        }
        return result;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Returns the help context.
     *
     * @return the context
     */
    protected HelpContext getHelpContext() {
        return help;
    }

    /**
     * Returns summary information for a party.
     * <p/>
     * The summary includes any alerts.
     *
     * @param party the party
     * @return a summary component
     */
    protected abstract Component createSummary(Party party);

    /**
     * Creates an alert summary for the specified party.
     *
     * @param party the party
     * @return the party's alerts, or {@code null} if the party has no alerts
     */
    public AlertSummary getAlertSummary(Party party) {
        AlertSummary result = null;
        List<Alert> alerts = getAlerts(party);
        if (!alerts.isEmpty()) {
            Collections.sort(alerts);
            result = new AlertSummary(alerts, context, help);
        }
        return result;
    }

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected abstract List<Alert> getAlerts(Party party);

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected List<Alert> queryAlerts(Party party) {
        List<Alert> result = new ArrayList<Alert>();
        ResultSet<Act> set = createAlertsResultSet(party, 20);
        ResultSetIterator<Act> iterator = new ResultSetIterator<Act>(set);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            Lookup lookup = ServiceHelper.getLookupService().getLookup(act, "alertType");
            if (lookup != null) {
                result.add(new Alert(lookup, act));
            }
        }
        return result;
    }

    /**
     * Returns outstanding alerts for a party.
     *
     * @param party    the party
     * @param pageSize the no. of alerts to return per page
     * @return the set of outstanding alerts for the party
     */
    protected abstract ResultSet<Act> createAlertsResultSet(Party party, int pageSize);
}
