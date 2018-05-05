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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.PreferenceMonitor;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.CRUDWindow;


/**
 * Customer financial workspace.
 * <p/>
 * This monitors preferences for updates to the {@link PreferenceArchetypes#CHARGE} group, and refreshes when
 * they change.
 *
 * @author Tim Anderson
 */
public abstract class CustomerFinancialWorkspace<T extends Act> extends CustomerActWorkspace<T> {

    /**
     * The preference monitor.
     */
    private final PreferenceMonitor monitor;

    /**
     * Constructs a {@link CustomerFinancialWorkspace}.
     *
     * @param id          the workspace id
     * @param context     the context
     * @param preferences the user preferences
     */
    public CustomerFinancialWorkspace(String id, Context context, Preferences preferences) {
        super(id, context, preferences);
        monitor = new PreferenceMonitor(preferences);
        monitor.add(PreferenceArchetypes.CHARGE);
    }

    /**
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        super.show();
        checkPreferences();
    }

    /**
     * Invoked when user preferences have changed.
     * <p/>
     * This is only invoked when the workspace is being shown.
     */
    @Override
    public void preferencesChanged() {
        checkPreferences();
    }

    /**
     * Checks preferences. If they have changed, forces a refresh on the CRUD window to pick up the latest values.
     */
    protected void checkPreferences() {
        if (monitor.changed()) {
            CRUDWindow<T> window = getCRUDWindow();
            if (window != null) {
                T object = window.getObject();
                window.setObject(object);
            }
        }
    }

}
