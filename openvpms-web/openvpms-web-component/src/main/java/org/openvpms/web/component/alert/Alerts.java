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

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.prefs.UserPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages alert retrieval and acknowledgement of mandatory alerts.
 * <p>
 * The identifiers and times of acknowledged alerts are stored in user preferences, to ensure acknowledged alerts
 * are only displayed once per user in a 24 hour period.
 *
 * @author Tim Anderson
 */
abstract class Alerts {

    /**
     * User preferences.
     */
    private final UserPreferences preferences;

    /**
     * The user preference name.
     */
    private final String name;

    /**
     * Acknowledged alerts.
     */
    private final Map<Long, Long> alerts;

    /**
     * Constructs an {@link Alerts}.
     *
     * @param preferences the preferences
     * @param name        the user preference name
     */
    public Alerts(UserPreferences preferences, String name) {
        this.preferences = preferences;
        this.name = name;
        alerts = parseAlerts(name);
    }

    /**
     * Returns alerts for a party.
     *
     * @param party the party
     * @return the alerts for a party
     */
    public abstract List<Alert> getAlerts(Party party);

    /**
     * Determines if an alert has been acknowledged in the last 24 hours.
     *
     * @param alert the alert
     * @return {@code true} if the alert has been acknowledged in the last 24 hours
     */
    public boolean isAcknowledged(Alert alert) {
        boolean result = false;
        Act act = alert.getAlert();
        if (act != null) {
            result = isAcknowledged(act.getId(), alerts);
        }
        return result;
    }

    /**
     * Acknowledge an alert. The acknowledgment expires in 24 hours.
     *
     * @param alert the alert to acknowledge
     */
    public void acknowledge(Alert alert) {
        if (alert.isMandatory()) {
            Act act = alert.getAlert();
            if (act != null) {
                acknowledge(act.getId(), name, alerts);
            }
        }
    }

    /**
     * Determines if an object has been acknowledged.
     *
     * @param id     the identifier of the object to check
     * @param alerts the alerts
     */
    protected boolean isAcknowledged(long id, Map<Long, Long> alerts) {
        boolean result = false;
        Long time = alerts.get(id);
        if (time != null) {
            if (isExpired(time)) {
                alerts.remove(id);
            } else {
                result = true;
            }
        }
        return result;
    }

    /**
     * Determines if a time has expired.
     *
     * @param time the time, in minutes since epoch
     * @return {@code true} if the time is before now
     */
    protected boolean isExpired(long time) {
        return (time <= nowMinutes());
    }

    /**
     * Acknowledge an alert. The acknowledgment expires in 24 hours.<br/>
     * This saves the acknowledged alert to user preferences.
     *
     * @param id     the identifier of the alert
     * @param name   the preference name
     * @param alerts the alerts
     */
    protected void acknowledge(long id, String name, Map<Long, Long> alerts) {
        alerts.put(id, nowPlus24Hours());
        saveAlerts(name, alerts);
    }

    /**
     * Parses the acknowledged alerts from preferences.
     * <p>
     * These are stored as a comma separated list of {@code <id>,<timestamp in mins>,<id>,<timestamp in mins>...}<br/>
     * Minutes are stored to reduce space requirements.
     *
     * @param name the preference name
     * @return the alerts
     */
    protected Map<Long, Long> parseAlerts(String name) {
        String value = getPreference(name);
        String[] values = (value != null) ? value.split(",") : new String[0];
        Map<Long, Long> map = new HashMap<>();
        for (int i = 0; i < values.length - 1; i += 2) {
            long id;
            long time;
            try {
                id = Long.valueOf(values[i]);
            } catch (NumberFormatException exception) {
                break;
            }
            try {
                time = Long.valueOf(values[i + 1]);
            } catch (NumberFormatException exception) {
                break;
            }
            map.put(id, time);
        }
        return map;
    }

    /**
     * Writes the acknowledged alerts to preferences.
     *
     * @param name   the preference name
     * @param alerts the alerts to save
     */
    protected void saveAlerts(String name, Map<Long, Long> alerts) {
        expire(alerts);
        List<Map.Entry<Long, Long>> entries = new ArrayList<>(alerts.entrySet());
        // sort entries on most recent acknowledgement first. This is because there is limited space to store the data
        // so some acknowledgements may not be stored
        Collections.sort(entries, (o1, o2) -> -o1.getValue().compareTo(o2.getValue()));

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long, Long> entry : alerts.entrySet()) {
            String id = Long.toString(entry.getKey());
            String time = Long.toString(entry.getValue());
            int length = id.length() + time.length() + 1; // +1 for separator
            if (builder.length() > 0) {
                length++;
            }
            if (builder.length() + length > 5000) {
                break;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id).append(',').append(time);
        }
        savePreference(name, builder.toString());
    }

    /**
     * Returns a preference.
     *
     * @param name the preference name
     * @return the preference value. May be {@code null}
     */
    protected String getPreference(String name) {
        return preferences.getString(PreferenceArchetypes.GENERAL, name, null);
    }

    /**
     * Saves a preference.
     *
     * @param name  the preference name
     * @param value the preference value. May be {@code null}
     */
    protected void savePreference(String name, String value) {
        preferences.setPreference(PreferenceArchetypes.GENERAL, name, value, true);
    }

    /**
     * Returns the current time, in minutes since epoch.
     *
     * @return the current time, in minutes
     */
    protected static long nowMinutes() {
        return toMinutes(System.currentTimeMillis());
    }

    /**
     * Returns {@code now + 24 hours}, in minutes.
     *
     * @return {@code now + 24 hours}, in minutes
     */
    protected static long nowPlus24Hours() {
        Date date = DateRules.getDate(new Date(), 1, DateUnits.DAYS);
        return toMinutes(date.getTime());
    }

    /**
     * Expires alert acknowledgements that have expired.
     *
     * @param alerts the alerts
     */
    private void expire(Map<Long, Long> alerts) {
        long minutes = nowMinutes();
        alerts.entrySet().removeIf(entry -> entry.getValue() <= minutes);
    }

    /**
     * Converts milliseconds to minutes.
     *
     * @param ms the milliseconds value to convert
     * @return the converted value
     */
    private static long toMinutes(long ms) {
        return ms / DateUtils.MILLIS_PER_MINUTE;
    }

}
