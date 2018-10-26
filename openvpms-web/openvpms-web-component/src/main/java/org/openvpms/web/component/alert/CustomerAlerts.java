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
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.prefs.UserPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer alerts.
 *
 * @author Tim Anderson
 */
class CustomerAlerts extends Alerts {

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * Customer account type alerts.
     */
    private final Map<Long, State> accountTypeAlerts;

    /**
     * Customer alerts user preference name.
     */
    private static final String CUSTOMER_ALERTS = "customerAlerts";

    /**
     * Account type alerts user preference name.
     */
    private static final String ACCOUNT_TYPE_ALERTS = "accountTypeAlerts";

    /**
     * Constructs a {@link CustomerAlerts}.
     *
     * @param preferences   the user preferences
     * @param lookups       the lookup service
     * @param customerRules the customer rules
     */
    public CustomerAlerts(UserPreferences preferences, ILookupService lookups, CustomerRules customerRules) {
        super(preferences, CUSTOMER_ALERTS);
        this.lookups = lookups;
        this.customerRules = customerRules;
        accountTypeAlerts = parseAccountTypeAlerts();
    }

    /**
     * Determines if an alert has been acknowledged in the last 24 hours.
     *
     * @param alert the alert
     * @return {@code true} if the alert has been acknowledged in the last 24 hours
     */
    @Override
    public boolean isAcknowledged(Alert alert) {
        boolean result;
        if (alert instanceof AccountTypeAlert) {
            State state = accountTypeAlerts.get(((AccountTypeAlert) alert).getCustomer().getId());
            result = state != null && !isExpired(state.time);
        } else {
            result = super.isAcknowledged(alert);
        }
        return result;
    }

    /**
     * Acknowledge an alert. The acknowledgment expires in 24 hours.
     *
     * @param alert the alert to acknowledge
     */
    @Override
    public void acknowledge(Alert alert) {
        if (alert instanceof AccountTypeAlert) {
            if (alert.isMandatory()) {
                State state = new State(alert.getAlertType().getId(), nowPlus24Hours());
                long customerId = ((AccountTypeAlert) alert).getCustomer().getId();
                accountTypeAlerts.put(customerId, state);
                saveAccountTypeAlerts();
            }
        } else {
            super.acknowledge(alert);
        }
    }

    /**
     * Returns the alerts for a party.
     * <p>
     * Customers can have an alert associated with their account type, so the returned alerts may contain an alert
     * that has no {@link Act}.
     *
     * @param customer the party
     * @return the party's alerts
     */
    public List<Alert> getAlerts(Party customer) {
        List<Alert> result = new ArrayList<>();
        ArchetypeQuery query = new ArchetypeQuery(CustomerArchetypes.ALERT);
        query.add(Constraints.join("customer").add(Constraints.eq("entity", customer)));
        query.add(QueryHelper.createDateRangeConstraint(new Date()));
        query.add(Constraints.eq("status", ActStatus.IN_PROGRESS));

        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            Lookup lookup = lookups.getLookup((IMObject) act, "alertType");
            if (lookup != null) {
                result.add(new Alert(lookup, act));
            }
        }
        AccountType accountType = customerRules.getAccountType(customer);
        if (accountType != null) {
            Lookup alertLookup = accountType.getAlert();
            if (alertLookup != null) {
                result.add(new AccountTypeAlert(customer, accountType, alertLookup));
            }
        }

        if (result.size() > 1) {
            Collections.sort(result);
        }
        return result;
    }

    /**
     * Parses the acknowledged account type alerts from preferences.
     * <p>
     * These are stored as a comma separated list of {@code <customerId>,<lookupId>,
     * <timestamp>,<customerId>,<lookupId>,<timestamp>...}<br/>
     * Timestamps are stored in minutes to reduce space requirements.
     *
     * @return the alerts
     */
    protected Map<Long, State> parseAccountTypeAlerts() {
        String value = getPreference(ACCOUNT_TYPE_ALERTS);
        String[] values = (value != null) ? value.split(",") : new String[0];
        Map<Long, State> map = new HashMap<>();
        for (int i = 0; i < values.length - 2; i += 3) {
            long customerId;
            long lookupId;
            long time;
            try {
                customerId = Long.valueOf(values[i]);
            } catch (NumberFormatException exception) {
                break;
            }
            try {
                lookupId = Long.valueOf(values[i + 1]);
            } catch (NumberFormatException exception) {
                break;
            }
            try {
                time = Long.valueOf(values[i + 2]);
            } catch (NumberFormatException exception) {
                break;
            }
            map.put(customerId, new State(lookupId, time));
        }
        return map;
    }

    private void saveAccountTypeAlerts() {
        // remove any acknowledgements that have expired
        long minutes = nowMinutes();
        accountTypeAlerts.entrySet().removeIf(entry -> entry.getValue().time <= minutes);

        List<Map.Entry<Long, State>> entries = new ArrayList<>(accountTypeAlerts.entrySet());
        // sort entries on most recent acknowledgement first. This is because there is limited space to store the data
        // so some acknowledgements may not be stored
        Collections.sort(entries, (o1, o2) -> -Long.compare(o1.getValue().time, o2.getValue().time));

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long, State> entry : accountTypeAlerts.entrySet()) {
            String customerId = Long.toString(entry.getKey());
            String lookupId = Long.toString(entry.getValue().lookupId);
            String time = Long.toString(entry.getValue().time);
            int length = customerId.length() + lookupId.length() + time.length() + 2; // +2 for separator
            if (builder.length() > 0) {
                length++;
            }
            if (builder.length() + length > 5000) {
                break;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(customerId).append(',').append(lookupId).append(',').append(time);
        }
        savePreference(ACCOUNT_TYPE_ALERTS, builder.toString());
    }

    private static class State {

        private final long lookupId;

        private final long time;

        public State(long lookupId, long time) {
            this.lookupId = lookupId;
            this.time = time;
        }
    }

}
