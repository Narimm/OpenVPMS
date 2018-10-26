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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.alert;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.prefs.UserPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Patient alerts.
 *
 * @author Tim Anderson
 */
class PatientAlerts extends Alerts {

    /**
     * Constructs a {@link PatientAlerts}.
     *
     * @param preferences the preferences
     */
    public PatientAlerts(UserPreferences preferences) {
        super(preferences, "patientAlerts");
    }

    /**
     * Returns alerts for a party.
     *
     * @param party the party
     * @return the alerts for a party
     */
    @Override
    public List<Alert> getAlerts(Party party) {
        List<Alert> result = new ArrayList<>();
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.ALERT);
        query.add(Constraints.join("patient").add(Constraints.eq("entity", party)));
        query.add(QueryHelper.createDateRangeConstraint(new Date()));
        query.add(Constraints.eq("status", ActStatus.IN_PROGRESS));

        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            Entity alertType = new IMObjectBean(act).getTarget("alertType", Entity.class);
            if (alertType != null) {
                result.add(new Alert(alertType, act));
            }
        }
        if (result.size() > 1) {
            Collections.sort(result);
        }
        return result;
    }

}
