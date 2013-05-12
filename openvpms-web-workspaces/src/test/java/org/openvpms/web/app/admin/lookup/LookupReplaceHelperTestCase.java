/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.admin.lookup;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link LookupReplaceHelper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class LookupReplaceHelperTestCase extends AbstractAppTest {

    /**
     * The lookup replacer.
     */
    private LookupReplaceHelper replacer;

    /**
     * The pet to test with.
     */
    private Party pet;

    /**
     * Verifies that an entity that has a node that refers to a lookup by its code can have the lookup code replaced.
     * The source lookup is not removed.
     */
    @Test
    public void testSpeciesReplace() {
        IMObjectBean bean = new IMObjectBean(pet);
        Lookup speciesA = createLookup("lookup.species", "A");
        Lookup breedA1 = createLookup("lookup.breed", "A1");
        Lookup breedA2 = createLookup("lookup.breed", "A2");
        addRelationship(speciesA, breedA1);
        addRelationship(speciesA, breedA2);

        Lookup speciesB = createLookup("lookup.species", "B");
        Lookup breedB1 = createLookup("lookup.breed", "B1");
        Lookup breedB2 = createLookup("lookup.breed", "B2");
        addRelationship(speciesB, breedB1);
        addRelationship(speciesB, breedB2);

        bean.setValue("species", speciesA.getCode());
        bean.setValue("breed", breedA1.getCode());
        bean.save();

        replacer.replace(speciesA, speciesB, false);
        pet = get(pet);
        bean = new IMObjectBean(pet);
        speciesA = get(speciesA);
        speciesB = get(speciesB);
        breedA1 = get(breedA1);
        breedA2 = get(breedA2);
        breedB1 = get(breedB1);
        breedB2 = get(breedB2);
        save(breedA1);
        assertEquals(speciesB.getCode(), bean.getString("species"));
        assertEquals(breedA1.getCode(), bean.getString("breed"));
        assertEquals(0, speciesA.getSourceLookupRelationships().size());
        assertEquals(4, speciesB.getSourceLookupRelationships().size());
        checkTarget(speciesB, breedA1);
        checkTarget(speciesB, breedA2);
        checkTarget(speciesB, breedB1);
        checkTarget(speciesB, breedB2);
    }

    /**
     * Verifies that an entity that has a node that refers to a lookup by its code can have the lookup code replaced.
     * The source lookup is removed.
     */
    @Test
    public void testSpeciesReplaceWithDelete() {
        IMObjectBean bean = new IMObjectBean(pet);
        Lookup speciesA = createLookup("lookup.species", "A");
        Lookup breedA1 = createLookup("lookup.breed", "A1");
        Lookup breedA2 = createLookup("lookup.breed", "A2");
        addRelationship(speciesA, breedA1);
        addRelationship(speciesA, breedA2);

        Lookup speciesB = createLookup("lookup.species", "B");
        Lookup breedB1 = createLookup("lookup.breed", "B1");
        Lookup breedB2 = createLookup("lookup.breed", "B2");
        addRelationship(speciesB, breedB1);
        addRelationship(speciesB, breedB2);

        bean.setValue("species", speciesA.getCode());
        bean.setValue("breed", breedA1.getCode());
        bean.save();

        replacer.replace(speciesA, speciesB, true);
        pet = get(pet);
        bean = new IMObjectBean(pet);
        speciesA = get(speciesA);
        speciesB = get(speciesB);
        assertNull(speciesA);
        breedA1 = get(breedA1);
        breedA2 = get(breedA2);
        breedB1 = get(breedB1);
        breedB2 = get(breedB2);
        save(breedA1);
        assertEquals(speciesB.getCode(), bean.getString("species"));
        assertEquals(breedA1.getCode(), bean.getString("breed"));
        assertEquals(4, speciesB.getSourceLookupRelationships().size());
        checkTarget(speciesB, breedA1);
        checkTarget(speciesB, breedA2);
        checkTarget(speciesB, breedB1);
        checkTarget(speciesB, breedB2);
    }

    /**
     * Verify that when a target lookup is replaced, the source lookup has relationships to both the original and new
     * target.
     */
    @Test
    public void testBreedReplacementForSameSpecies() {
        IMObjectBean bean = new IMObjectBean(pet);
        Lookup species = createLookup("lookup.species", "A");
        Lookup breed1 = createLookup("lookup.breed", "A1");
        Lookup breed2 = createLookup("lookup.breed", "A2");
        addRelationship(species, breed1);
        addRelationship(species, breed2);

        bean.setValue("species", species.getCode());
        bean.setValue("breed", breed1.getCode());
        bean.save();

        replacer.replace(breed1, breed2, false);
        pet = get(pet);
        bean = new IMObjectBean(pet);
        breed1 = get(breed1);
        breed2 = get(breed2);

        validate(breed1);
        validate(breed2);

        assertEquals(species.getCode(), bean.getString("species"));
        assertEquals(breed2.getCode(), bean.getString("breed"));
        assertEquals(2, species.getSourceLookupRelationships().size());
        checkTarget(species, breed1);
        checkTarget(species, breed2);
    }

    /**
     * Verify that a target lookup can be replaced and deleted.
     */
    @Test
    public void testBreedReplacementForSameSpeciesWithDelete() {
        IMObjectBean bean = new IMObjectBean(pet);
        Lookup species = createLookup("lookup.species", "A");
        Lookup breed1 = createLookup("lookup.breed", "A1");
        Lookup breed2 = createLookup("lookup.breed", "A2");
        addRelationship(species, breed1);
        addRelationship(species, breed2);

        bean.setValue("species", species.getCode());
        bean.setValue("breed", breed1.getCode());
        bean.save();

        replacer.replace(breed1, breed2, true);
        pet = get(pet);
        bean = new IMObjectBean(pet);
        assertNull(get(breed1));
        species = get(species);
        breed2 = get(breed2);
        validate(breed2);

        assertEquals(species.getCode(), bean.getString("species"));
        assertEquals(breed2.getCode(), bean.getString("breed"));
        assertEquals(1, species.getSourceLookupRelationships().size());
        checkTarget(species, breed2);
    }

    /**
     * Verifies that a breed lookup cannot be replaced with another that is associated with a different species,
     * due to a validation error.
     */
    @Test
    public void testBreedReplacementForDifferentSpecies() {
        IMObjectBean bean = new IMObjectBean(pet);
        Lookup speciesA = createLookup("lookup.species", "A");
        Lookup breedA = createLookup("lookup.breed", "A1");
        addRelationship(speciesA, breedA);

        Lookup speciesB = createLookup("lookup.species", "B");
        Lookup breedB = createLookup("lookup.breed", "B1");
        addRelationship(speciesB, breedB);

        bean.setValue("species", speciesA.getCode());
        bean.setValue("breed", breedA.getCode());
        bean.save();

        try {
            replacer.replace(breedA, breedB, false);
            fail("Expected ValidationException");
        } catch (ValidationException expected) {
            // do nothing
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        replacer = new LookupReplaceHelper();
        pet = TestHelper.createPatient();
    }

    /**
     * Verifies that a relationship exists between the source and target lookups.
     *
     * @param source the source lookup
     * @param target the target lookup
     */
    private void checkTarget(Lookup source, Lookup target) {
        boolean found = false;
        for (LookupRelationship relationship : source.getSourceLookupRelationships()) {
            if (ObjectUtils.equals(target.getObjectReference(), relationship.getTarget())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Adds a species-breed relationship between two lookups.
     *
     * @param species the species
     * @param breed   the breed
     */
    private void addRelationship(Lookup species, Lookup breed) {
        LookupRelationship relationship = (LookupRelationship) create("lookupRelationship.speciesBreed");
        relationship.setSource(species.getObjectReference());
        relationship.setTarget(breed.getObjectReference());
        species.addLookupRelationship(relationship);
        breed.addLookupRelationship(relationship);
        save(Arrays.asList(species, breed));
    }

    /**
     * Checks object validity (i.e cardinality constraints etc).
     *
     * @param object the object to check
     */
    private void validate(IMObject object) {
        IArchetypeService service = getArchetypeService();
        service.validateObject(object);
    }

    /**
     * Helper to create a lookup with unique code.
     *
     * @param shortName the lookup archetype short name
     * @param code      the code prefix
     * @return a new lookup
     */
    private Lookup createLookup(String shortName, String code) {
        return TestHelper.getLookup(shortName, code + System.currentTimeMillis());
    }

}
