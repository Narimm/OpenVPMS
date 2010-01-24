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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;


/**
 * Tests the {@link LookupHelper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class LookupHelperTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link LookupHelper#getName} method.
     */
    @Test
    public void testGetName() {
        Lookup lookup = createLookup("lookup.species", "CANINE", "Canine");

        IMObject pet = create("party.animalpet");
        IMObjectBean bean = new IMObjectBean(pet);
        bean.setValue("species", lookup.getCode());

        NodeDescriptor species = bean.getDescriptor("species");
        assertNotNull(species);
        assertEquals(lookup.getName(), LookupHelper.getName(getArchetypeService(), species, pet));
    }

    /**
     * Tests the {@link LookupHelper#getNames(IArchetypeService, String, String)} method.
     */
    @Test
    public void testGetNames() {
        IArchetypeService service = getArchetypeService();
        Map<String, String> current = LookupHelper.getNames(service, "party.animalpet", "species");

        Lookup canine = createLookup("lookup.species", "CANINE", "Canine");
        Lookup feline = createLookup("lookup.species", "FELINE", "Feline");

        Map<String, String> names = LookupHelper.getNames(service, "party.animalpet", "species");
        assertEquals(current.size() + 2, names.size());
        assertEquals("Canine", names.get(canine.getCode()));
        assertEquals("Feline", names.get(feline.getCode()));
    }

    /**
     * Tests the {@link LookupHelper#getName} method for a target lookup.
     */
    @Test
    public void testGetNameTargetLookup() {
        IArchetypeService service = getArchetypeService();
        Lookup speciesLookup = createLookup("lookup.species", "CANINE", "Canine");
        Lookup breedLookup = createLookup("lookup.breed", "KELPIE", "Kelpie");

        IMObject pet = create("party.animalpet");
        IMObjectBean bean = new IMObjectBean(pet);
        bean.setValue("species", speciesLookup.getCode());
        bean.setValue("breed", breedLookup.getCode());

        NodeDescriptor breed = bean.getDescriptor("breed");
        assertNotNull(breed);

        // the breed won't be available until there is a relationship between
        // species and breed
        assertNull(LookupHelper.getName(service, breed, pet));

        // now add the relationship
        LookupRelationship relationship = (LookupRelationship) create("lookupRelationship.speciesBreed");
        relationship.setSource(speciesLookup.getObjectReference());
        relationship.setTarget(breedLookup.getObjectReference());
        service.save(relationship);

        assertEquals(breedLookup.getName(), LookupHelper.getName(service, breed, pet));
    }

    /**
     * Tests the {@link LookupHelper#getName} method for a local lookup.
     */
    @Test
    public void testGetNameLocalLookup() {
        IArchetypeService service = getArchetypeService();
        IMObject pet = create("party.animalpet");
        IMObjectBean bean = new IMObjectBean(pet);

        bean.setValue("sex", "MALE");
        NodeDescriptor sex = bean.getDescriptor("sex");
        assertNotNull(sex);
        assertEquals("male", LookupHelper.getName(service, sex, pet));
    }

    /**
     * Tests the {@link LookupHelper#getDefaultLookup} methods.
     */
    @Test
    public void testDefaultLookup() {
        IArchetypeService service = getArchetypeService();
        LookupUtil.removeAll(service, "lookup.colour");

        // create two new colour lookups. Make RED the default.

        Lookup red = LookupUtil.createLookup(service, "lookup.colour", "RED");
        red.setDefaultLookup(true);

        Lookup blue = LookupUtil.createLookup(service, "lookup.colour", "BLUE");
        blue.setDefaultLookup(false);

        service.save(red);
        service.save(blue);

        // verify the correct lookup is returned
        Lookup lookup = LookupServiceHelper.getLookupService().getDefaultLookup("lookup.colour");
        assertNotNull(lookup);
        assertEquals(red.getCode(), lookup.getCode());
    }

    /**
     * Helper to create a lookup.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @param name      the lookup name
     * @return a new lookup
     */
    private Lookup createLookup(String shortName, String code, String name) {
        Lookup lookup = LookupUtil.createLookup(getArchetypeService(), shortName, code, name);
        save(lookup);
        return lookup;
    }


}
