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

package org.openvpms.web.component.im.lookup;

import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.test.AbstractAppTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link NodeLookupQuery} class.
 *
 * @author Tim Anderson
 */
public class NodeLookupQueryTestCase extends AbstractAppTest {

    /**
     * Tests the {@link NodeLookupQuery#getLookups()} method when constructed using the
     * {@link NodeLookupQuery#NodeLookupQuery(String, String)} constructor.
     */
    @Test
    public void testGetLookupsForNodeName() {
        NodeLookupQuery query = new NodeLookupQuery(CustomerArchetypes.PERSON, "title");
        checkTitles(query);
    }

    /**
     * Tests the {@link NodeLookupQuery#getLookups()} method when constructed using the
     * {@link NodeLookupQuery#NodeLookupQuery(IMObject, NodeDescriptor)} constructor.
     */
    @Test
    public void testGetLookupsForDescriptor() {
        Party customer = TestHelper.createCustomer(false);
        ArchetypeDescriptor archetype = getArchetypeService().getArchetypeDescriptor(customer.getArchetypeId());
        NodeDescriptor descriptor = archetype.getNodeDescriptor("title");
        assertNotNull(descriptor);
        NodeLookupQuery query = new NodeLookupQuery(customer, descriptor);
        checkTitles(query);
    }

    /**
     * Verifies that inactive lookups are returned if they are referred to by an object.
     */
    @Test
    public void testInactiveLookups() {
        Party patient = TestHelper.createPatient(false);
        IMObjectBean bean = new IMObjectBean(patient);
        Lookup canine = TestHelper.getLookup(PatientArchetypes.SPECIES, "CANINE");
        Lookup canineBreed = TestHelper.getLookup(PatientArchetypes.BREED, "BLOODHOUND", canine,
                                                  "lookupRelationship.speciesBreed");
        Lookup feline = TestHelper.getLookup(PatientArchetypes.SPECIES, "FELINE");
        Lookup felineBreed = TestHelper.getLookup(PatientArchetypes.BREED, "ABYSSINIAN", feline,
                                                  "lookupRelationship.speciesBreed");
        bean.setValue("species", canine.getCode());
        bean.setValue("breed", canineBreed.getCode());

        NodeLookupQuery speciesQuery = new NodeLookupQuery(patient, bean.getDescriptor("species"));
        checkLookups(speciesQuery, true, canine, feline);

        NodeLookupQuery breedQuery = new NodeLookupQuery(patient, bean.getDescriptor("breed"));
        checkLookups(breedQuery, true, canineBreed);
        checkLookups(breedQuery, false, felineBreed);

        canine.setActive(false);
        save(canine);
        checkLookups(speciesQuery, true, canine, feline);
        checkLookups(breedQuery, true, canineBreed);
        checkLookups(breedQuery, false, felineBreed);

        canineBreed.setActive(false);
        save(canineBreed);
        checkLookups(speciesQuery, true, canine, feline);
        checkLookups(breedQuery, true, canineBreed);
        checkLookups(breedQuery, false, felineBreed);

        // now change to the active codes, and verify the inactive codes aren't returned
        bean.setValue("species", feline.getCode());
        bean.setValue("breed", felineBreed.getCode());
        checkLookups(speciesQuery, true, feline);
        checkLookups(speciesQuery, false, canine);
        checkLookups(breedQuery, true, felineBreed);
        checkLookups(breedQuery, false, canineBreed);
    }

    /**
     * Tests the {@link NodeLookupQuery#getLookups()} method against lookup.personTitle lookups.
     *
     * @param query the query to check
     */
    private void checkTitles(NodeLookupQuery query) {
        Collection<Lookup> titles = getLookupService().getLookups("lookup.personTitle");
        List<Lookup> expected = new ArrayList<>();
        for (Lookup title : titles) {
            if (title.isActive()) {
                expected.add(title);
            }
        }

        List<Lookup> actual = query.getLookups();

        assertEquals(expected.size(), actual.size());
        for (Lookup l : expected) {
            assertTrue(actual.contains(l));
        }
    }

    /**
     * Tests the {@link NodeLookupQuery#getLookups()}.
     *
     * @param query   the query
     * @param exists  determines if the lookups must exist
     * @param lookups the lookups to check
     */
    private void checkLookups(NodeLookupQuery query, boolean exists, Lookup... lookups) {
        List<Lookup> actual = query.getLookups();
        for (Lookup lookup : lookups) {
            assertEquals(exists, actual.contains(lookup));
        }
    }

}
