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

import nextapp.echo2.app.*;
import org.openvpms.component.business.domain.im.act.*;
import org.openvpms.component.business.domain.im.lookup.*;
import org.openvpms.component.business.domain.im.party.*;
import org.openvpms.web.app.alert.*;
import org.openvpms.web.component.im.query.*;
import org.openvpms.web.system.*;

import java.util.*;


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
        List<Alert> alerts = getAlerts(party);
        if (!alerts.isEmpty()) {
            Collections.sort(alerts);
            result = new AlertSummary(alerts);
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
