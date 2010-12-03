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
 *
 *  $Id$
 */
package org.openvpms.web.app.summary;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.app.alert.AlertSummary;
import org.openvpms.web.app.alert.Alerts;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Creates summary components for a given party.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PartySummary {


    /**
     * Returns summary information for a party.
     * <p/>
     * The summary includes any alerts.
     *
     * @param party the party. May be <tt>null</tt>
     * @return a summary component, or <tt>null</tt> if there is no summary
     */
    public Component getSummary(Party party) {
        Component result = null;
        if (party != null) {
            result = createSummary(party);
        }
        return result;
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
     * @return the party's alerts, or <tt>null</tt> if the party has no alerts
     */
    public AlertSummary getAlertSummary(Party party) {
        AlertSummary result = null;
        Collection<Alerts> alerts = getAlerts(party);
        if (!alerts.isEmpty()) {
            List<Alerts> sorted = new ArrayList<Alerts>(alerts);
            Collections.sort(sorted);
            result = new AlertSummary(sorted);
        }
        return result;
    }

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected abstract Collection<Alerts> getAlerts(Party party);

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected Map<String, Alerts> queryAlerts(Party party) {
        Map<String, Alerts> map = new HashMap<String, Alerts>();
        ResultSet<Act> set = createAlertsResultSet(party, 20);
        ResultSetIterator<Act> iterator = new ResultSetIterator<Act>(set);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            IMObjectBean bean = new IMObjectBean(act);
            String code = bean.getString("alertType");
            Alerts alerts = map.get(code);
            if (alerts == null) {
                Lookup lookup = ServiceHelper.getLookupService().getLookup(act, "alertType");
                if (lookup != null) {
                    alerts = new Alerts(lookup);
                    map.put(code, alerts);
                }
            }
            if (alerts != null) {
                alerts.addAlert(act);
            }
        }
        return map;
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
