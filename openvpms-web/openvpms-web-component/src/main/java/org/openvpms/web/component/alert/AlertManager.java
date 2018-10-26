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

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.prefs.UserPreferences;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages mandatory alerts for a user session.
 * <p>
 * The identifiers of the acknowledged alerts are stored in user preferences along with the time when they were
 * acknowledged.
 * <p>
 * This allows mandatory alerts to be only displayed once per 24 hour period, per user.
 *
 * @author Tim Anderson
 */
public class AlertManager {
    /**
     * Customer alerts.
     */
    private final Alerts customerAlerts;

    /**
     * Patient alerts.
     */
    private final Alerts patientAlerts;

    /**
     * Constructs an {@link AlertManager}.
     *
     * @param preferences   user preferences
     * @param lookups       the lookup service
     * @param customerRules the customer rules
     */
    public AlertManager(UserPreferences preferences, ILookupService lookups, CustomerRules customerRules) {
        customerAlerts = new CustomerAlerts(preferences, lookups, customerRules);
        patientAlerts = new PatientAlerts(preferences);
    }

    /**
     * Returns alerts for a customer or patient.
     *
     * @param party the customer or patient
     * @return the alerts for the customer or patient
     */
    public List<Alert> getAlerts(Party party) {
        List<Alert> result;
        if (party.isA(CustomerArchetypes.PERSON)) {
            result = customerAlerts.getAlerts(party);
        } else if (party.isA(PatientArchetypes.PATIENT)) {
            result = patientAlerts.getAlerts(party);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns the mandatory alerts.
     * <p>
     * These are alerts that need to be acknowledged by the user.
     *
     * @return the mandatory alerts
     */
    public List<Alert> getMandatoryAlerts(Party party) {
        List<Alert> alerts = getAlerts(party);
        return alerts.stream().filter(alert -> alert.isMandatory() && !isAcknowledged(alert))
                .collect(Collectors.toList());
    }

    /**
     * Acknowledge an alert.
     * <p>
     * Once acknowledged by a user, the alert will no longer be returned by {@link #getMandatoryAlerts(Party)} for
     * another 24 hours.
     *
     * @param alert the alert to acknowledge
     */
    public void acknowledge(Alert alert) {
        getAlerts(alert).acknowledge(alert);
    }

    /**
     * Determines if an alert has been acknowledged in the last 24 hours.
     *
     * @param alert the alert
     * @return {@code true} if the alert has been acknowledged in the last 24 hours
     */
    public boolean isAcknowledged(Alert alert) {
        return getAlerts(alert).isAcknowledged(alert);
    }

    /**
     * Returns the {@link Alerts} for an alert.
     *
     * @param alert the alert
     * @return the corresponding {@link Alerts}
     */
    private Alerts getAlerts(Alert alert) {
        return isPatientAlert(alert) ? patientAlerts : customerAlerts;
    }

    /**
     * Determines if an alert is a patient alert.
     *
     * @param alert the alert
     * @return {@code true} if the alert is a patient alert, or {@code false} if it is a customer alert
     */
    private boolean isPatientAlert(Alert alert) {
        return alert.getAlertType().isA(PatientArchetypes.ALERT_TYPE);
    }

}
