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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.prefs;

import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.echo.util.WeakReferenceCallbacks;

import java.util.Collections;
import java.util.Set;

/**
 * User preferences.
 *
 * @author Tim Anderson
 */
public class UserPreferences implements Preferences {

    /**
     * The preference service.
     */
    private final PreferenceService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The underlying preferences.
     */
    private Preferences preferences;

    /**
     * The persistent preferences.
     */
    private Preferences persistent;

    /**
     * The user the preferences belong to.
     */
    private User user;

    /**
     * Used to notify updates.
     */
    private final WeakReferenceCallbacks listeners = new WeakReferenceCallbacks();


    /**
     * Constructs an {@link UserPreferences}.
     *
     * @param service         the preference service
     * @param practiceService the practice service
     */
    public UserPreferences(PreferenceService service, PracticeService practiceService) {
        this.service = service;
        this.practiceService = practiceService;
    }

    /**
     * Initialise the preferences.
     *
     * @param user the user
     */
    public void initialise(User user) {
        this.user = user;
        refresh();
    }

    /**
     * Registers a listener to be notified when the preferences are refreshed.
     * <p/>
     * The caller must hold a strong reference to the listener, to prevent it being garbage collected.
     *
     * @param listener the listener
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /**
     * Refreshes preferences.
     */
    public void refresh() {
        if (user != null) {
            preferences = service.getPreferences(user, practiceService.getPractice(), false);
            persistent = null; // persistent preferences are loaded on demand
            listeners.call();
        }
    }

    /**
     * Returns the available preference group names.
     *
     * @return the group name
     */
    @Override
    public Set<String> getGroupNames() {
        return preferences != null ? preferences.getGroupNames() : Collections.<String>emptySet();
    }

    /**
     * Returns the available preferences in a group.
     *
     * @param groupName the group name.
     * @return the preference names
     */
    @Override
    public Set<String> getNames(String groupName) {
        return preferences != null ? preferences.getNames(groupName) : Collections.<String>emptySet();
    }

    /**
     * Returns a user preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public Object getPreference(String groupName, String name, Object defaultValue) {
        return preferences != null ? preferences.getPreference(groupName, name, defaultValue) : defaultValue;
    }

    /**
     * Sets a preference.
     *
     * @param groupName the preference group name
     * @param name      the preference name
     * @param value     the preference value. May be {@code null}
     */
    @Override
    public void setPreference(String groupName, String name, Object value) {
        setPreference(groupName, name, value, false);
    }

    /**
     * Sets a preference.
     *
     * @param groupName the preference group name
     * @param name      the preference name
     * @param value     the preference value. May be {@code null}
     * @param save      if {@code true}, make the preference persistent, otherwise store it for the session
     */
    public void setPreference(String groupName, String name, Object value, boolean save) {
        if (!save) {
            setSession(groupName, name, value);
        } else if (user != null) {
            Preferences persistent = getPersistent();
            persistent.setPreference(groupName, name, value);

            // reflect it in the session preferences
            setSession(groupName, name, value);
        }
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public boolean getBoolean(String groupName, String name, boolean defaultValue) {
        return preferences != null ? preferences.getBoolean(groupName, name, defaultValue) : defaultValue;
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public int getInt(String groupName, String name, int defaultValue) {
        return preferences != null ? preferences.getInt(groupName, name, defaultValue) : defaultValue;
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public long getLong(String groupName, String name, long defaultValue) {
        return preferences != null ? preferences.getLong(groupName, name, defaultValue) : defaultValue;
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public String getString(String groupName, String name, String defaultValue) {
        return preferences != null ? preferences.getString(groupName, name, defaultValue) : defaultValue;
    }

    /**
     * Returns the reference value of a property.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public IMObjectReference getReference(String groupName, String name, IMObjectReference defaultValue) {
        return preferences != null ? preferences.getReference(groupName, name, defaultValue) : defaultValue;
    }

    /**
     * Sets a preference for the duration of the session.
     *
     * @param groupName the preference group name
     * @param name      the preference name
     * @param value     the preference value. May be {@code null}
     */
    protected void setSession(String groupName, String name, Object value) {
        if (preferences != null) {
            preferences.setPreference(groupName, name, value);
        }
    }

    /**
     * Returns persistent preferences. These don't include any changes made locally.
     *
     * @return the persistent preferences
     */
    protected Preferences getPersistent() {
        if (persistent == null) {
            persistent = service.getPreferences(user, practiceService.getPractice(), true);
        }
        return persistent;
    }

}
