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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitors changes in user preferences.
 *
 * @author Tim Anderson
 */
public class PreferenceMonitor {

    /**
     * The preferences.
     */
    private final Preferences preferences;

    /**
     * A map of preferences names to their corresponding values.
     */
    private Map<Id, Object> values = new HashMap<>();

    /**
     * Helper to associate a preference group and name.
     */
    private static class Id {

        /**
         * The preference group.
         */
        public final String group;

        /**
         * The preference name.
         */
        public final String name;

        public Id(String group, String name) {
            this.group = group;
            this.name = name;
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            } else if (object instanceof Id) {
                return group.equals(((Id) object).group) && name.equals(((Id) object).name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(group).append(name).toHashCode();
        }

        @Override
        public String toString() {
            return group + "/" + name;
        }
    }

    /**
     * Constructs a {@link PreferenceMonitor}.
     *
     * @param preferences the preferences
     */
    public PreferenceMonitor(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Adds a group to monitor changes to.
     *
     * @param group the preference group
     */
    public void add(String group) {
        for (String name : preferences.getNames(group)) {
            add(group, name);
        }
    }

    /**
     * Adds a preference to monitor changes to.
     *
     * @param group the preference group
     * @param name the preference name
     */
    public void add(String group, String name) {
        Id id = new Id(group, name);
        values.put(id, getValue(id));
    }

    /**
     * Determines if any of the preferences have changed since the last invocation.
     *
     * @return {@code true} if any of the values have changed
     */
    public boolean changed() {
        boolean result = false;
        for (Map.Entry<Id, Object> entry : values.entrySet()) {
            Id id = entry.getKey();
            Object current = getValue(id);
            if (!ObjectUtils.equals(entry.getValue(), current)) {
                entry.setValue(current);
                result = true;
                // need to continue loop to pick up all updated values
            }
        }
        return result;
    }

    /**
     * Returns the current value of a preference.
     *
     * @param id the preference id
     * @return the corresponding value. May be {@code null}
     */
    protected Object getValue(Id id) {
        return preferences.getPreference(id.group, id.name, null);
    }

}
