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

package org.openvpms.web.component.prefs;

import org.junit.Test;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.archetype.rules.prefs.PreferenceServiceImpl;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link UserPreferences}.
 *
 * @author Tim Anderson
 */
public class UserPreferencesTestCase extends ArchetypeServiceTest {

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * Verifies that a new user inherits the practice defaults until changed.
     */
    @Test
    public void testInheritPracticeDefaults() {
        User user = TestHelper.createUser();
        PreferenceService preferenceService = new PreferenceServiceImpl(getArchetypeService(), transactionManager);
        Preferences defaultPrefs = preferenceService.getPreferences(TestHelper.getPractice(), null, true);
        defaultPrefs.setPreference(PreferenceArchetypes.GENERAL, "homePage", "workflow.scheduling");
        defaultPrefs.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "ACTIVE");

        PracticeRules rules = new PracticeRules(getArchetypeService(), applicationContext.getBean(Currencies.class));
        PracticeService practiceService = new PracticeService(getArchetypeService(), rules, null);
        UserPreferences userPreferences = new UserPreferences(preferenceService, practiceService);
        userPreferences.initialise(user);

        assertEquals("workflow.scheduling", userPreferences.getPreference(PreferenceArchetypes.GENERAL, "homePage",
                                                                          "foo"));
        assertEquals("ACTIVE", userPreferences.getString(PreferenceArchetypes.SUMMARY, "showReferral", "foo"));

        userPreferences.setPreference(PreferenceArchetypes.GENERAL, "homePage", "customer.information");
        userPreferences.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "NEVER");
        assertEquals("customer.information", userPreferences.getPreference(PreferenceArchetypes.GENERAL, "homePage",
                                                                           "foo"));
        assertEquals("NEVER", userPreferences.getString(PreferenceArchetypes.SUMMARY, "showReferral", "foo"));

        // make sure defaults haven't changed.
        assertEquals("workflow.scheduling", defaultPrefs.getPreference(PreferenceArchetypes.GENERAL, "homePage",
                                                                       "foo"));
        assertEquals("ACTIVE", defaultPrefs.getPreference(PreferenceArchetypes.SUMMARY, "showReferral", "foo"));
    }
}
