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

package org.openvpms.archetype.rules.practice;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link PracticeRules} class, in conjunction with the
 * <em>archetypeService.save.party.organisationPractice.before</em>
 * rule.
 *
 * @author Tim Anderson
 */
public class PracticeRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private PracticeRules rules;


    /**
     * Tests the {@link PracticeRules#getLocations(Party)} method.
     */
    @Test
    public void testGetLocations() {
        Party practice = createPractice();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        IMObjectBean bean = getBean(practice);

        bean.addTarget("locations", location1);
        bean.addTarget("locations", location2);

        List<Party> locations = rules.getLocations(practice);
        assertEquals(2, locations.size());
        assertTrue(locations.contains(location1));
        assertTrue(locations.contains(location2));
    }

    /**
     * Tests the {@link PracticeRules#getDefaultLocation(Party)} method.
     */
    @Test
    public void testGetDefaultLocation() {
        Party practice = createPractice();

        assertNull(rules.getDefaultLocation(practice));

        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        IMObjectBean bean = getBean(practice);

        bean.addTarget("locations", location1);
        EntityRelationship rel2 = (EntityRelationship) bean.addTarget("locations", location2);

        Party defaultLocation = rules.getDefaultLocation(practice);
        assertNotNull(defaultLocation);

        // location can be one of location1, or location2, as default not
        // specified
        assertTrue(defaultLocation.equals(location1) || defaultLocation.equals(location2));

        // mark rel2 as the default
        EntityRelationshipHelper.setDefault(practice, "locations", rel2,
                                            getArchetypeService());
        assertEquals(location2, rules.getDefaultLocation(practice));
    }

    /**
     * Tests the {@link PracticeRules#isActivePractice(Party)} method.
     */
    @Test
    public void testIsActivePractice() {
        Party practice = TestHelper.getPractice();

        assertTrue(rules.isActivePractice(practice));
        save(practice); // save should succeed

        Party newPractice = createPractice();

        // try and save the new practice. Should fail
        try {
            save(newPractice);
            fail("Expected save of another active practice to fail");
        } catch (Exception expected) {
            // do nothing
        }

        // mark the new practice inactive and save again. Should succeed.
        newPractice.setActive(false);
        save(newPractice);

        assertFalse(rules.isActivePractice(newPractice));

        // verify original practice still active
        assertTrue(rules.isActivePractice(practice));
    }

    /**
     * Tests the {@link PracticeRules#getEstimateExpiryDate(Date, Party)} method.
     */
    @Test
    public void testEstimateExpiryDate() {
        Party practice = createPractice();
        IMObjectBean bean = getBean(practice);
        bean.setValue("estimateExpiryUnits", null);

        Date startDate = TestHelper.getDate("2018-08-04");
        assertNull(rules.getEstimateExpiryDate(startDate, practice));

        bean.setValue("estimateExpiryPeriod", 1);
        bean.setValue("estimateExpiryUnits", "YEARS");
        assertEquals(TestHelper.getDate("2019-08-04"), rules.getEstimateExpiryDate(startDate, practice));

        bean.setValue("estimateExpiryPeriod", 6);
        bean.setValue("estimateExpiryUnits", "MONTHS");
        assertEquals(TestHelper.getDate("2019-02-04"), rules.getEstimateExpiryDate(startDate, practice));
    }

    /**
     * Tests the {@link PracticeRules#getPrescriptionExpiryDate(Date, Party)} method.
     */
    @Test
    public void testPrescriptionExpiryDate() {
        Party practice = createPractice();
        IMObjectBean bean = getBean(practice);
        bean.setValue("prescriptionExpiryUnits", null);

        Date startDate = TestHelper.getDate("2013-07-01");
        assertEquals(startDate, rules.getPrescriptionExpiryDate(startDate, practice));

        bean.setValue("prescriptionExpiryPeriod", 1);
        bean.setValue("prescriptionExpiryUnits", "YEARS");
        assertEquals(TestHelper.getDate("2014-07-01"), rules.getPrescriptionExpiryDate(startDate, practice));

        bean.setValue("prescriptionExpiryPeriod", 6);
        bean.setValue("prescriptionExpiryUnits", "MONTHS");
        assertEquals(TestHelper.getDate("2014-01-01"), rules.getPrescriptionExpiryDate(startDate, practice));
    }

    /**
     * Tests the {@link PracticeRules#getServiceUser(Party)} method.
     */
    @Test
    public void testGetServiceUser() {
        Party practice = createPractice();
        assertNull(rules.getServiceUser(practice));

        User user = TestHelper.createUser();
        IMObjectBean bean = getBean(practice);
        bean.addTarget("serviceUser", user);

        assertEquals(user, rules.getServiceUser(practice));
    }

    /**
     * Tests the {@link PracticeRules#getCurrency(Party)} method.
     */
    @Test
    public void testGetCurrency() {
        Party practice = createPractice();
        Currency currency = rules.getCurrency(practice);
        assertEquals("AUD", currency.getCode());
        assertEquals(2, currency.getDefaultFractionDigits());
    }

    /**
     * Tests the {@link PracticeRules#useLocationProducts(Party)} method.
     */
    @Test
    public void testUseLocationProducts() {
        Party practice = createPractice();

        assertFalse(rules.useLocationProducts(practice));

        IMObjectBean bean = getBean(practice);
        bean.setValue("useLocationProducts", true);
        assertTrue(rules.useLocationProducts(practice));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeService service = getArchetypeService();
        rules = new PracticeRules(service, new Currencies(service, getLookupService()));
    }

    /**
     * Helper to create a new practice.
     *
     * @return a new practice
     */
    private Party createPractice() {
        Party party = (Party) create(PracticeArchetypes.PRACTICE);
        party.setName("XPractice2");
        IMObjectBean bean = getBean(party);
        Lookup currency = TestHelper.getCurrency("AUD");
        bean.setValue("currency", currency.getCode());

        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        party.addContact(contact);
        return party;
    }
}
