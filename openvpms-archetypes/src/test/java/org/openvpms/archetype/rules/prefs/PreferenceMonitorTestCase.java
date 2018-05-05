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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PreferenceMonitor}.
 *
 * @author Tim Anderson
 */
public class PreferenceMonitorTestCase extends ArchetypeServiceTest {
    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * The preferences.
     */
    private Preferences preferences;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        PreferenceService service = new PreferenceServiceImpl(getArchetypeService(), transactionManager);
        User user = TestHelper.createUser();
        preferences = service.getPreferences(user, null, true);
    }

    /**
     * Tests monitoring a single preference.
     */
    @Test
    public void testMonitorPreference() {
        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showCustomerAccount", true);
        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "ACTIVE");

        PreferenceMonitor monitor = new PreferenceMonitor(preferences);
        monitor.add(PreferenceArchetypes.SUMMARY, "showCustomerAccount");
        assertFalse(monitor.changed());

        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showCustomerAccount", false);
        assertTrue(monitor.changed());
        assertFalse(monitor.changed());

        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "NEVER");
        assertFalse(monitor.changed());
    }

    /**
     * Tests the monitoring a preference group.
     */
    @Test
    public void testMonitorGroup() {
        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showCustomerAccount", true);
        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "ACTIVE");

        PreferenceMonitor monitor = new PreferenceMonitor(preferences);
        monitor.add(PreferenceArchetypes.SUMMARY);
        assertFalse(monitor.changed());

        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showCustomerAccount", false);
        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "ALWAYS");
        assertTrue(monitor.changed());
        assertFalse(monitor.changed());

        preferences.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "NEVER");
        assertTrue(monitor.changed());
        assertFalse(monitor.changed());
    }

}
