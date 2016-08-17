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

package org.openvpms.component.business.service.lookup;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link ILookupService}.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("lookup-service-appcontext.xml")
public abstract class AbstractLookupServiceTest extends AbstractArchetypeServiceTest {

    /**
     * The DAO.
     */
    @Autowired
    private IMObjectDAO dao;

    /**
     * The lookup service.
     */
    private ILookupService lookupService;

    /**
     * Default constructor
     */
    public AbstractLookupServiceTest() {
    }

    /**
     * Tests the {@link ILookupService#getLookup} method.
     */
    @Test
    public void testGetLookup() {
        Lookup lookup = createLookup("lookup.breed", "CANINE");
        String code = lookup.getCode();

        Lookup found = lookupService.getLookup("lookup.breed", code);
        assertNull(found);

        save(lookup);

        found = lookupService.getLookup("lookup.breed", code);
        assertNotNull(found);
        assertEquals(lookup.getObjectReference(), found.getObjectReference());

        // now de-activate it
        lookup.setActive(false);
        save(lookup);

        assertNull(lookupService.getLookup("lookup.breed", code));
    }

    /**
     * Tests the {@link ILookupService#getLookups} method.
     */
    @Test
    public void testGetLookups() {
        Collection<Lookup> lookups1 = lookupService.getLookups("lookup.country");

        Lookup lookup1 = createLookup("lookup.country", "AU");
        Lookup lookup2 = createLookup("lookup.country", "NZ");
        save(lookup1);
        save(lookup2);

        Collection<Lookup> lookups2 = lookupService.getLookups("lookup.country");
        assertEquals(lookups1.size() + 2, lookups2.size());
        assertTrue(lookups2.contains(lookup1));
        assertTrue(lookups2.contains(lookup2));

        lookup2.setActive(false);
        save(lookup2);

        Collection<Lookup> lookups3 = lookupService.getLookups("lookup.country");
        assertEquals(lookups1.size() + 1, lookups3.size());
        assertTrue(lookups3.contains(lookup1));
        assertFalse(lookups3.contains(lookup2));
    }

    /**
     * Tests the {@link ILookupService#getDefaultLookup} method.
     */
    @Test
    public void testGetDefaultLookups() {
        removeLookups("lookup.country");

        Lookup au = createLookup("lookup.country", "AU");
        au.setDefaultLookup(true);
        save(au);

        createLookup("lookup.country", "UK");

        Lookup lookup = lookupService.getDefaultLookup("lookup.country");
        assertEquals(au, lookup);

        // now create an inactive lookup, set as the default, and mark au as not the default
        Lookup nz = createLookup("lookup.country", "NZ");
        nz.setDefaultLookup(true);
        nz.setActive(false);
        save(nz);
        au.setDefaultLookup(false);
        save(au);

        assertNull(lookupService.getDefaultLookup("lookup.country"));

    }

    /**
     * Tests the {@link ILookupService#getSourceLookups(Lookup)} and
     * {@link ILookupService#getTargetLookups(Lookup)} method.
     */
    @Test
    public void testGetSourceAndTargetLookups() {
        removeLookups("lookup.country");
        removeLookups("lookup.state");

        Lookup au = createLookup("lookup.country", "AU");
        createLookup("lookup.country", "UK");
        Lookup vic = createLookup("lookup.state", "VIC");
        LookupUtil.addRelationship(getArchetypeService(), "lookupRelationship.countryState", au, vic);
        save(au, vic);

        assertEquals(0, lookupService.getSourceLookups(au).size());
        Collection<Lookup> targets = lookupService.getTargetLookups(au);
        assertEquals(1, targets.size());
        assertTrue(targets.contains(vic));

        assertEquals(0, lookupService.getTargetLookups(vic).size());
        Collection<Lookup> sources = lookupService.getSourceLookups(vic);
        assertEquals(1, sources.size());
        assertTrue(sources.contains(au));
    }

    /**
     * Tests that lookup updates and removals are reflected by the service.
     */
    @Test
    public void testUpdateRemove() {
        removeLookups("lookup.country");
        removeLookups("lookup.state");

        Lookup au = createLookup("lookup.country", "AU");
        Lookup vic = createLookup("lookup.state", "VIC");
        LookupRelationship rel = LookupUtil.addRelationship(
                getArchetypeService(), "lookupRelationship.countryState", au, vic);
        save(au, vic);

        au = get(au);
        vic = get(vic);

        assertEquals(au, lookupService.getLookup("lookup.country", au.getCode()));
        assertEquals(vic, lookupService.getLookup("lookup.state", vic.getCode()));

        au.setDescription("Australia");
        save(au);

        assertEquals(au.getDescription(), lookupService.getLookup(
                "lookup.country", au.getCode()).getDescription());

        au.removeLookupRelationship(rel);
        vic.removeLookupRelationship(rel);
        save(au, vic);
        remove(vic);

        assertEquals(au, lookupService.getLookup("lookup.country", au.getCode()));
        assertNull(lookupService.getLookup("lookup.state", vic.getCode()));
    }

    /**
     * Verifies that lookups can be retrieved via wildcards, and that changes a reflected.
     */
    @Test
    public void testWildcards() {
        removeLookups("lookup.diagnosis");
        removeLookups("lookup.diagnosisVeNom");

        Lookup diagnosis1 = createLookup("lookup.diagnosis", "FIBROMA");
        Lookup diagnosis2 = createLookup("lookup.diagnosisVeNom", "VENOM");
        diagnosis2.getDetails().put("dataDictionaryId", "1234");
        save(diagnosis1, diagnosis2);

        Collection<Lookup> wildcardLookups = lookupService.getLookups("lookup.diagnosis*");
        Collection<Lookup> diagnosisLookups = lookupService.getLookups("lookup.diagnosis");
        Collection<Lookup> veNomLookups = lookupService.getLookups("lookup.diagnosisVeNom");

        assertEquals(2, wildcardLookups.size());
        assertTrue(wildcardLookups.contains(diagnosis1));
        assertTrue(wildcardLookups.contains(diagnosis2));

        assertEquals(1, diagnosisLookups.size());
        assertTrue(diagnosisLookups.contains(diagnosis1));

        assertEquals(1, veNomLookups.size());
        assertTrue(veNomLookups.contains(diagnosis2));

        // save new lookup, and verify it is reflected in wildcardLookups and diagnosisLookups
        Lookup diagnosis3 = createLookup("lookup.diagnosis", "HEART_MURMUR");
        save(diagnosis3);
        wildcardLookups = lookupService.getLookups("lookup.diagnosis*");
        diagnosisLookups = lookupService.getLookups("lookup.diagnosis");
        veNomLookups = lookupService.getLookups("lookup.diagnosisVeNom");
        assertEquals(3, wildcardLookups.size());
        assertTrue(wildcardLookups.contains(diagnosis1));
        assertTrue(wildcardLookups.contains(diagnosis2));
        assertTrue(wildcardLookups.contains(diagnosis3));

        assertEquals(2, diagnosisLookups.size());
        assertTrue(diagnosisLookups.contains(diagnosis1));
        assertTrue(diagnosisLookups.contains(diagnosis3));

        assertEquals(1, veNomLookups.size());
        assertTrue(veNomLookups.contains(diagnosis2));

        // now remove the VeNom lookup, and verify it is reflected in wildcardLookups and veNom lookups
        remove(diagnosis2);

        wildcardLookups = lookupService.getLookups("lookup.diagnosis*");
        diagnosisLookups = lookupService.getLookups("lookup.diagnosis");
        veNomLookups = lookupService.getLookups("lookup.diagnosisVeNom");
        assertEquals(2, wildcardLookups.size());
        assertTrue(wildcardLookups.contains(diagnosis1));
        assertFalse(wildcardLookups.contains(diagnosis2));
        assertTrue(wildcardLookups.contains(diagnosis3));

        assertEquals(2, diagnosisLookups.size());
        assertTrue(diagnosisLookups.contains(diagnosis1));
        assertTrue(diagnosisLookups.contains(diagnosis3));

        assertEquals(0, veNomLookups.size());
    }

    /**
     * Verifies that the {@link ILookupService#getLookup(IMObject, String)}
     * and {@link ILookupService#getName(IMObject, String)} methods can return inactive lookups for remote and
     * target lookups.
     */
    @Test
    public void testInactiveLookups() {
        Lookup canine = createLookup("lookup.species", "CANINE");
        Lookup canineBreed = createLookup("lookup.breed", "BLOODHOUND");
        LookupUtil.addRelationship(getArchetypeService(), "lookupRelationship.speciesBreed", canine, canineBreed);
        Lookup feline = createLookup("lookup.species", "FELINE");
        Lookup felineBreed = createLookup("lookup.breed", "ABYSSINIAN");
        LookupUtil.addRelationship(getArchetypeService(), "lookupRelationship.speciesBreed", feline, felineBreed);
        save(canine, canineBreed, feline, felineBreed);

        IMObjectBean bean = createBean("party.patientpet");
        bean.setValue("species", canine.getCode());
        bean.setValue("breed", canineBreed.getCode());

        IMObject patient = bean.getObject();
        Lookup species1 = lookupService.getLookup(patient, "species");
        assertEquals(canine, species1);

        Collection<Lookup> list1 = lookupService.getLookups(patient, "species");
        checkLookups(list1, true, canine, feline);

        Lookup breed1 = lookupService.getLookup(patient, "breed");
        assertEquals(canineBreed, breed1);

        Collection<Lookup> list2 = lookupService.getLookups(patient, "breed");
        checkLookups(list2, true, canineBreed);
        checkLookups(list2, false, felineBreed);

        canine.setActive(false);
        save(canine);

        Lookup species2 = lookupService.getLookup(patient, "species");
        assertEquals(canine, species2);

        Collection<Lookup> list3 = lookupService.getLookups(patient, "species");
        checkLookups(list3, true, canine, feline);

        Lookup breed2 = lookupService.getLookup(patient, "breed");
        assertEquals(canineBreed, breed2);

        Collection<Lookup> list4 = lookupService.getLookups(patient, "breed");
        checkLookups(list4, true, canineBreed);
        checkLookups(list4, false, felineBreed);

        canineBreed.setActive(false);
        save(canineBreed);

        Lookup breed3 = lookupService.getLookup(patient, "breed");
        assertEquals(canineBreed, breed3);

        Collection<Lookup> list5 = lookupService.getLookups(patient, "breed");
        checkLookups(list5, true, canineBreed);
        checkLookups(list5, false, felineBreed);

        // verify the getName() method also handles inactive lookups
        assertNotNull(canine.getName());
        assertNotNull(canineBreed.getName());
        assertEquals(canine.getName(), lookupService.getName(patient, "species"));
        assertEquals(canineBreed.getName(), lookupService.getName(patient, "breed"));

        // now swap the species. The getLookups() methods should no longer return the CANINE species
        bean.setValue("species", feline.getCode());
        bean.setValue("breed", felineBreed.getCode());

        Collection<Lookup> list6 = lookupService.getLookups(patient, "species");
        checkLookups(list6, true, feline);
        checkLookups(list6, false, canine);

        Collection<Lookup> list7 = lookupService.getLookups(patient, "breed");
        checkLookups(list7, true, felineBreed);
        checkLookups(list7, false, canineBreed);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public abstract void setUp() throws Exception;

    /**
     * Helper to create and save a lookup.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     * @return a new lookup
     */
    protected Lookup createLookup(String shortName, String code) {
        return LookupUtil.createLookup(getArchetypeService(), shortName, code);
    }

    /**
     * Registers the lookup service.
     *
     * @param service the lookup service
     */
    protected void setLookupService(ILookupService service) {
        lookupService = service;
        new LookupServiceHelper(service);
    }

    /**
     * Returns the DAO.
     *
     * @return the DAO
     */
    protected IMObjectDAO getDAO() {
        return dao;
    }

    /**
     * Helper to remove all lookups for the specified archetype short name.
     *
     * @param shortName the archetype short name
     */
    protected void removeLookups(String shortName) {
        Collection<Lookup> lookups = lookupService.getLookups(shortName);
        for (Lookup lookup : lookups) {
            lookup = (Lookup) get(lookup.getObjectReference());
            remove(lookup);
        }

    }

    private void checkLookups(Collection<Lookup> lookups, boolean exists, Lookup... expected) {
        for (Lookup lookup : expected) {
            assertEquals(exists, lookups.contains(lookup));
        }
    }
}
