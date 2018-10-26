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

package org.openvpms.archetype.rules.prefs;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.model.bean.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link PreferenceServiceImpl}.
 *
 * @author Tim Anderson
 */
public class PreferenceServiceImplTestCase extends ArchetypeServiceTest {

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * The preference service.
     */
    private PreferenceService service;

    /**
     * Test user.
     */
    private User user;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        service = new PreferenceServiceImpl(getArchetypeService(), transactionManager);
        user = TestHelper.createUser();
    }

    /**
     * Verifies preferences can be reset.
     */
    @Test
    public void testReset() {
        service.reset(user, null);  // no-op

        Preferences prefs = service.getPreferences(user, null, true);

        prefs.getPreference(PreferenceArchetypes.SUMMARY, "showReferral", "ALWAYS"); // will create group

        Entity entity = service.getEntity(user, null);
        assertNotNull(entity);
        List<Entity> groups = getGroups(entity);
        assertEquals(1, groups.size());

        // reset preferences and verify the associated entities are removed
        service.reset(user, null);
        assertNull(get(entity));
        assertNull(get(groups.get(0)));
    }

    /**
     * Verifies that a user can be assigned default preferences, and that they are copies of the source.
     */
    @Test
    public void testCreateWithDefaults() {
        Party practice = TestHelper.getPractice();
        service.reset(practice, null);
        Preferences defaultPrefs = service.getPreferences(practice, null, true);
        defaultPrefs.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "ALWAYS");
        defaultPrefs.setPreference(PreferenceArchetypes.SUMMARY, "showCustomerAccount", false);
        Entity defaultPrefEntity = service.getEntity(practice, null);
        assertNotNull(defaultPrefEntity);
        List<Entity> defaultGroups = getGroups(defaultPrefEntity);
        assertEquals(1, defaultGroups.size());

        // verify the user gets the practice defaults
        Preferences prefs = service.getPreferences(user, practice, true);
        assertEquals("ALWAYS", prefs.getString(PreferenceArchetypes.SUMMARY, "showReferral", "ACTIVE"));
        assertFalse(prefs.getBoolean(PreferenceArchetypes.SUMMARY, "showCustomerAccount", true));

        // verify the entities are different
        Entity entity = service.getEntity(user, practice);
        assertNotNull(entity);
        assertNotEquals(entity.getId(), defaultPrefEntity.getId());

        List<Entity> groups = getGroups(entity);
        assertEquals(1, groups.size());
        assertNotEquals(defaultGroups.get(0).getId(), groups.get(0).getId());
    }

    /**
     * Verifies preferences can be reset to default values specified by the practice.
     */
    @Test
    public void testResetToPracticeDefaults() {
        Party practice = TestHelper.getPractice();
        service.reset(practice, null);
        Preferences defaultPrefs = service.getPreferences(practice, null, true);
        defaultPrefs.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "ALWAYS");

        Preferences prefs = service.getPreferences(user, null, true); // will get archetype defaults
        assertEquals("ACTIVE", prefs.getString(PreferenceArchetypes.SUMMARY, "showReferral", "ACTIVE"));
        prefs.setPreference(PreferenceArchetypes.SUMMARY, "showReferral", "NEVER");

        service.reset(user, practice);
        prefs = service.getPreferences(user, null, true);
        assertEquals("ALWAYS", prefs.getString(PreferenceArchetypes.SUMMARY, "showReferral", "ACTIVE"));
    }

    /**
     * Verifies that preference updates aren't rolled back if an outer transaction rolls back.
     */
    @Test
    public void testTransactionIsolation() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        Preferences preferences = service.getPreferences(user, null, true);
        assertEquals("customer.information", preferences.getString(PreferenceArchetypes.GENERAL, "homePage", null));

        Party customer = TestHelper.createCustomer();
        long version = customer.getVersion();
        String name = customer.getName();
        try {
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    IMObjectBean bean = getBean(customer);
                    bean.setValue("lastName", "Foo");
                    bean.save();
                    preferences.setPreference(PreferenceArchetypes.GENERAL, "homePage", "workflow.scheduling");
                    throw new IllegalStateException("Forcing rollback of outer transaction");
                }
            });
        } catch (IllegalStateException expected) {
            // do nothing
        }

        // verify the preference was updated
        Preferences preferences2 = service.getPreferences(user, null, true);
        assertEquals("workflow.scheduling", preferences2.getString(PreferenceArchetypes.GENERAL, "homePage", null));

        // verify the customer was not updated
        Party customer2 = get(customer);
        assertEquals(version, customer2.getVersion());
        assertEquals(name, customer2.getName());
    }

    /**
     * Verifies that if a preference update fails, the outer transaction doesn't roll back.
     */
    @Test
    public void testPreferenceRollbackDoesntAffectOuterTransaction() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);

        // get 2 copies of the same preferences
        Preferences preferences1 = service.getPreferences(user, null, true);
        Preferences preferences2 = service.getPreferences(user, null, true);
        assertEquals("customer.information", preferences1.getString(PreferenceArchetypes.GENERAL, "homePage", null));
        assertEquals("customer.information", preferences2.getString(PreferenceArchetypes.GENERAL, "homePage", null));

        // update preferences1, and verify the update is not reflected in preferences2
        preferences1.setPreference(PreferenceArchetypes.GENERAL, "homePage", "customer.communication");
        assertEquals("customer.information", preferences2.getString(PreferenceArchetypes.GENERAL, "homePage", null));

        // update the customer and preferences within a transaction. The preferences2 update should roll back,
        // but the customer should update
        Party customer = TestHelper.createCustomer();
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                IMObjectBean bean = getBean(customer);
                bean.setValue("lastName", "Foo");
                bean.save();
                preferences2.setPreference(PreferenceArchetypes.GENERAL, "homePage", "workflow.scheduling");
            }
        });

        // verify the preference3 reflects the value set by preference1
        Preferences preferences3 = service.getPreferences(user, null, true);
        assertEquals("customer.communication", preferences3.getString(PreferenceArchetypes.GENERAL, "homePage", null));

        // verify the customer was updated
        Party customer2 = get(customer);
        assertEquals(1, customer2.getVersion());
        assertEquals("Foo", getBean(customer2).getString("lastName"));
    }

    /**
     * Returns the preference groups.
     *
     * @param prefs the preferences
     * @return the associated groups
     */
    private List<Entity> getGroups(Entity prefs) {
        IMObjectBean bean = getBean(prefs);
        return bean.getTargets("groups", Entity.class);
    }

}
