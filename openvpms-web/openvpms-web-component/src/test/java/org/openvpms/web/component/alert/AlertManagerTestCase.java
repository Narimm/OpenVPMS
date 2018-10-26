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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.archetype.rules.prefs.PreferenceServiceImpl;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.web.component.prefs.UserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link AlertManager}.
 *
 * @author Tim Anderson
 */
public class AlertManagerTestCase extends ArchetypeServiceTest {

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * User preferences.
     */
    private UserPreferences preferences;

    /**
     * Customer rules.
     */
    private CustomerRules customerRules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        PreferenceService preferenceService = new PreferenceServiceImpl(getArchetypeService(), transactionManager);
        User user = TestHelper.createUser();
        customerRules = new CustomerRules(getArchetypeService(), getLookupService());
        PracticeService practiceService = new PracticeService(getArchetypeService(), practiceRules, null);
        preferences = new UserPreferences(preferenceService, practiceService);
        preferences.initialise(user);
    }

    /**
     * Tests customer alerts.
     */
    @Test
    public void testCustomerAlerts() {
        AlertManager manager1 = createAlertManager();

        Lookup alertType1 = createCustomerAlertType("VIP", true);
        Lookup alertType2 = createCustomerAlertType("NOTE", false);
        Lookup alertType3 = createCustomerAlertType("ACCOUNT", true);
        Lookup accountType = createCustomerAccountType("BAD_DEBT", alertType3); // an account type that has an alert

        Party customer = TestHelper.createCustomer();
        assertEquals(0, manager1.getAlerts(customer).size());
        assertEquals(0, manager1.getMandatoryAlerts(customer).size());

        customer.addClassification(accountType);
        save(customer);
        Act act1 = createCustomerAlert(customer, "VIP");
        Act act2 = createCustomerAlert(customer, "NOTE");

        List<Alert> alerts = manager1.getAlerts(customer);
        assertEquals(3, alerts.size());
        Alert alert1 = checkAlert(alerts, act1, alertType1, true);
        Alert alert2 = checkAlert(alerts, act2, alertType2, false);
        Alert alert3 = checkAlert(alerts, null, alertType3, true); // alert from an account type

        List<Alert> mandatoryAlerts = manager1.getMandatoryAlerts(customer);
        assertEquals(2, mandatoryAlerts.size());
        checkAlert(mandatoryAlerts, act1, alertType1, true);
        checkAlert(mandatoryAlerts, null, alertType3, true);

        assertFalse(manager1.isAcknowledged(alert1));
        assertFalse(manager1.isAcknowledged(alert2));
        assertFalse(manager1.isAcknowledged(alert3));

        manager1.acknowledge(alert1);
        assertTrue(manager1.isAcknowledged(alert1));

        manager1.acknowledge(alert2);
        assertFalse(manager1.isAcknowledged(alert2));  // can't acknowledge non-mandatory alerts

        manager1.acknowledge(alert3);
        assertTrue(manager1.isAcknowledged(alert3));

        assertEquals(3, manager1.getAlerts(customer).size());
        assertEquals(0, manager1.getMandatoryAlerts(customer).size()); // alerts acknowledged, so not returned.

        // simulate expiration of the alert1 acknowledgement. Need to recreate the manager as it caches acknowledgements
        long nowMinutes = Alerts.nowMinutes();
        preferences.setPreference(PreferenceArchetypes.GENERAL, "customerAlerts", act1.getId() + "," + nowMinutes);

        AlertManager manager2 = createAlertManager();
        assertFalse(manager2.isAcknowledged(alert1));
        assertTrue(manager2.isAcknowledged(alert3)); // uses accountTypeAlerts preference
    }

    /**
     * Tests patient alerts.
     */
    @Test
    public void testPatientAlerts() {
        AlertManager manager1 = createAlertManager();

        Entity alertType1 = createPatientAlertType("Allergy", true);
        Entity alertType2 = createPatientAlertType("Note", false);

        Party patient = TestHelper.createPatient();
        assertEquals(0, manager1.getAlerts(patient).size());
        assertEquals(0, manager1.getMandatoryAlerts(patient).size());

        Act act1 = createPatientAlert(patient, alertType1);
        Act act2 = createPatientAlert(patient, alertType2);

        List<Alert> alerts = manager1.getAlerts(patient);
        assertEquals(2, alerts.size());
        Alert alert1 = checkAlert(alerts, act1, alertType1, true);
        Alert alert2 = checkAlert(alerts, act2, alertType2, false);

        List<Alert> mandatoryAlerts = manager1.getMandatoryAlerts(patient);
        assertEquals(1, mandatoryAlerts.size());
        checkAlert(mandatoryAlerts, act1, alertType1, true);

        assertFalse(manager1.isAcknowledged(alert1));
        assertFalse(manager1.isAcknowledged(alert2));

        manager1.acknowledge(alert1);
        assertTrue(manager1.isAcknowledged(alert1));

        manager1.acknowledge(alert2);
        assertFalse(manager1.isAcknowledged(alert2));  // can't acknowledge non-mandatory alerts

        assertEquals(2, manager1.getAlerts(patient).size());
        assertEquals(0, manager1.getMandatoryAlerts(patient).size()); // alerts acknowledged, so not returned.

        // simulate expiration of the alert1 acknowledgement. Need to recreate the manager as it caches acknowledgements
        long nowMinutes = Alerts.nowMinutes();
        preferences.setPreference(PreferenceArchetypes.GENERAL, "patientAlerts", act1.getId() + "," + nowMinutes);

        AlertManager manager2 = createAlertManager();
        assertFalse(manager2.isAcknowledged(alert1));
    }

    /**
     * Creates an alert manager.
     *
     * @return a new alert manager
     */
    protected AlertManager createAlertManager() {
        return new AlertManager(preferences, getLookupService(), customerRules);
    }

    /**
     * Creates a customer account type with a an alert type.
     *
     * @param code the account type code
     * @param alertType the alert type
     * @return a new customer account type
     */
    private Lookup createCustomerAccountType(String code, Lookup alertType) {
        IMObjectBean bean = getBean(TestHelper.getLookup(CustomerArchetypes.ACCOUNT_TYPE, code));
        bean.setTarget("alert", alertType);
        bean.save();
        return (Lookup) bean.getObject();
    }

    /**
     * Verifies an alert matches that expected.
     *
     * @param alerts    the alerts
     * @param act       the expected act
     * @param alertType the expected alert type
     * @param mandatory the expected mandatory flag
     * @return the alert
     */
    private Alert checkAlert(List<Alert> alerts, Act act, IMObject alertType, boolean mandatory) {
        Alert alert = getAlert(alerts, alertType);
        assertNotNull(alert);
        assertEquals(act, alert.getAlert());
        assertEquals(alert.isMandatory(), mandatory);
        return alert;
    }

    /**
     * Returns an alert with matching alert type.
     *
     * @param alerts the alerts
     * @param alertType the alert type
     * @return the first alert with the alert type, or {@code null} if none is found
     */
    private Alert getAlert(List<Alert> alerts, IMObject alertType) {
        return alerts.stream().filter(alert -> alert.getAlertType().equals(alertType)).findFirst().orElse(null);
    }

    /**
     * Creates a new customer alert.
     *
     * @param customer  the customer
     * @param alertType the alert type code
     * @return a new customer alert
     */
    private Act createCustomerAlert(Party customer, String alertType) {
        IMObjectBean alert = getBean(create(CustomerArchetypes.ALERT));
        alert.setTarget("customer", customer);
        alert.setValue("alertType", alertType);
        alert.save();
        return (Act) alert.getObject();
    }

    /**
     * Creates a new customer alert type.
     *
     * @param code           the alert type code
     * @param mandatoryAlert if {@code true}, the alert must be acknowledged
     * @return a new customer alert type
     */
    private Lookup createCustomerAlertType(String code, boolean mandatoryAlert) {
        IMObjectBean alertType = getBean(TestHelper.getLookup(CustomerArchetypes.ALERT_TYPE, code));
        alertType.setValue("mandatoryAlert", mandatoryAlert);
        alertType.save();
        return (Lookup) alertType.getObject();
    }

    /**
     * Creates a new patient alert.
     *
     * @param patient   the patient
     * @param alertType the alert type
     * @return a new patient alert
     */
    private Act createPatientAlert(Party patient, Entity alertType) {
        IMObjectBean alert = getBean(create(PatientArchetypes.ALERT));
        alert.setTarget("patient", patient);
        alert.setTarget("alertType", alertType);
        alert.save();
        return (Act) alert.getObject();
    }

    /**
     * Creates a new patient alert type.
     *
     * @param name           the alert type name
     * @param mandatoryAlert if {@code true}, the alert must be acknowledged
     * @return a new patient alert type
     */
    private Entity createPatientAlertType(String name, boolean mandatoryAlert) {
        IMObjectBean alertType = getBean(create(PatientArchetypes.ALERT_TYPE));
        alertType.setValue("name", name);
        alertType.setValue("mandatoryAlert", mandatoryAlert);
        alertType.save();
        return (Entity) alertType.getObject();
    }

}
