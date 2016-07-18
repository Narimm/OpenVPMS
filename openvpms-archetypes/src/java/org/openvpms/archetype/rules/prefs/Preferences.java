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

import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Set;

/**
 * User preferences.
 *
 * @author Tim Anderson
 */
public interface Preferences {

    /**
     * Returns the available preference group names.
     *
     * @return the group name
     */
    Set<String> getGroupNames();

    /**
     * Returns the available preferences in a group.
     *
     * @param groupName the group name.
     * @return the preference names
     */
    Set<String> getNames(String groupName);

    /**
     * Returns a user preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    Object getPreference(String groupName, String name, Object defaultValue);

    /**
     * Sets a preference.
     *
     * @param groupName the preference group name
     * @param name      the preference name
     * @param value     the preference value. May be {@code null}
     */
    void setPreference(String groupName, String name, Object value);

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    boolean getBoolean(String groupName, String name, boolean defaultValue);

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    int getInt(String groupName, String name, int defaultValue);

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    long getLong(String groupName, String name, long defaultValue);

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    String getString(String groupName, String name, String defaultValue);

    /**
     * Returns the reference value of a property.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    IMObjectReference getReference(String groupName, String name, IMObjectReference defaultValue);

}
