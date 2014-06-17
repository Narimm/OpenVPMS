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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper.sort;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link IMObjectSorter} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../../archetype-service-appcontext.xml")
public class IMObjectSorterTestCase extends AbstractArchetypeServiceTest {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookups;

    /**
     * Tests the {@link IMObjectSorter#sort(List, String, boolean)} method.
     */
    @Test
    public void testSort() {
        Party pet1 = createPet("C");
        Party pet2 = createPet("A");
        Party pet3 = createPet("B");

        IMObjectSorter sorter = new IMObjectSorter(getArchetypeService(), lookups);
        List<Party> pets = Arrays.asList(pet1, pet2, pet3);

        sorter.sort(pets, "name", true);
        checkOrder(pets, pet2, pet3, pet1);

        sorter.sort(pets, "name", false);
        checkOrder(pets, pet1, pet3, pet2);

        sorter.sort(pets, "id", true);
        checkOrder(pets, pet1, pet2, pet3);

        sorter.sort(pets, "id", false);
        checkOrder(pets, pet3, pet2, pet1);
    }

    /**
     * Helper to create a new patient.
     *
     * @param name the patient name
     * @return a new patient
     */
    private Party createPet(String name) {
        Party patient = (Party) create("party.patientpet");
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("name", name);
        Lookup canine = LookupUtil.getLookup(getArchetypeService(), "lookup.species", "CANINE");
        bean.setValue("species", canine.getCode());
        bean.save();
        return patient;
    }

    /**
     * Verifies the order of objects matches that the expected.
     *
     * @param actual   the sorted order
     * @param expected the expected order
     */
    private <T extends IMObject> void checkOrder(List<T> actual, T... expected) {
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], actual.get(i));
        }
    }
}
