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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.practice;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.practice.PracticeArchetypes.PRACTICE_LOCATION_RELATIONSHIP;


/**
 * Tests the {@link PracticeRules} class, in conjunction with the
 * <em>archetypeService.save.party.organisationPractice.before</em>
 * rule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
        EntityBean bean = new EntityBean(practice);

        bean.addRelationship(PRACTICE_LOCATION_RELATIONSHIP, location1);
        bean.addRelationship(PRACTICE_LOCATION_RELATIONSHIP, location2);

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
        EntityBean bean = new EntityBean(practice);

        bean.addRelationship(PRACTICE_LOCATION_RELATIONSHIP, location1);
        EntityRelationship rel2
                = bean.addRelationship(PRACTICE_LOCATION_RELATIONSHIP,
                                       location2);

        Party defaultLocation = rules.getDefaultLocation(practice);
        assertNotNull(defaultLocation);

        // location can be one of location1, or location2, as default not
        // specified
        assertTrue(defaultLocation.equals(location1)
                   || defaultLocation.equals(location2));

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
        }

        // mark the new practice inactive and save again. Should succeed.
        newPractice.setActive(false);
        save(newPractice);

        assertFalse(rules.isActivePractice(newPractice));

        // verify original practice still active
        assertTrue(rules.isActivePractice(practice));
    }

    /**
     * Tests the {@link PracticeRules#getPrescriptionExpiryDate(Date, Party)} method.
     */
    @Test
    public void testPrescriptionExpiryDate() {
        Party practice = createPractice();
        IMObjectBean bean = new IMObjectBean(practice);
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
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new PracticeRules(getArchetypeService());
    }

    /**
     * Helper to create a new practice.
     *
     * @return a new practice
     */
    private Party createPractice() {
        Party party = (Party) create(PracticeArchetypes.PRACTICE);
        party.setName("XPractice2");
        IMObjectBean bean = new IMObjectBean(party);
        Lookup currency = TestHelper.getCurrency("AUD");
        bean.setValue("currency", currency.getCode());

        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        party.addContact(contact);
        return party;
    }
}
