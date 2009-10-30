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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Map;


/**
 * Tests the {@link LookupHelper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class LookupHelperTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests the {@link LookupHelper#getName} method.
     */
    public void testGetName() {
        Lookup lookup = createLookup("lookup.species", "CANINE", "Canine");

        IMObject pet = service.create("party.animalpet");
        IMObjectBean bean = new IMObjectBean(pet, service);
        bean.setValue("species", lookup.getCode());

        NodeDescriptor species = bean.getDescriptor("species");
        assertNotNull(species);
        assertEquals(lookup.getName(),
                     LookupHelper.getName(service, species, pet));
    }

    /**
     * Tests the {@link LookupHelper#getNames(IArchetypeService, String, String)} method.
     */
    public void testGetNames() {
        Lookup canine = createLookup("lookup.species", "CANINE", "Canine");
        Lookup feline = createLookup("lookup.species", "FELINE", "Feline");

        Map<String, String> names
                = LookupHelper.getNames(service, "party.animalpet", "species");
        assertEquals(2, names.size());
        assertEquals("Canine", names.get(canine.getCode()));
        assertEquals("Feline", names.get(feline.getCode()));
    }

    /**
     * Tests the {@link LookupHelper#getName} method for a target lookup.
     */
    public void testGetNameTargetLookup() {
        Lookup speciesLookup = createLookup("lookup.species", "CANINE",
                                            "Canine");
        Lookup breedLookup = createLookup("lookup.breed", "KELPIE", "Kelpie");

        IMObject pet = service.create("party.animalpet");
        IMObjectBean bean = new IMObjectBean(pet, service);
        bean.setValue("species", speciesLookup.getCode());
        bean.setValue("breed", breedLookup.getCode());

        NodeDescriptor breed = bean.getDescriptor("breed");
        assertNotNull(breed);

        // the breed won't be available until there is a relationship between
        // species and breed
        assertNull(LookupHelper.getName(service, breed, pet));

        // now add the relationship
        LookupRelationship relationship = (LookupRelationship) create(
                "lookupRelationship.speciesBreed");
        relationship.setSource(speciesLookup.getObjectReference());
        relationship.setTarget(breedLookup.getObjectReference());
        service.save(relationship);

        assertEquals(breedLookup.getName(),
                     LookupHelper.getName(service, breed, pet));
    }

    /**
     * Tests the {@link LookupHelper#getName} method for a local lookup.
     */
    public void testGetNameLocalLookup() {
        IMObject pet = service.create("party.animalpet");
        IMObjectBean bean = new IMObjectBean(pet, service);

        bean.setValue("sex", "MALE");
        NodeDescriptor sex = bean.getDescriptor("sex");
        assertNotNull(sex);
        assertEquals("male", LookupHelper.getName(service, sex, pet));
    }

    /**
     * Tests the {@link LookupHelper#getDefaultLookup} methods.
     */
    public void testDefaultLookup() {
        // create two new colour lookups. Make RED the default.
        Lookup red = LookupUtil.createLookup(service, "lookup.colour", "RED");
        red.setDefaultLookup(true);

        Lookup blue = LookupUtil.createLookup(service, "lookup.colour", "BLUE");
        blue.setDefaultLookup(false);

        service.save(red);
        service.save(blue);

        // verify the correct lookup is returned
        Lookup lookup = LookupServiceHelper.getLookupService().getDefaultLookup(
                "lookup.colour");
        assertNotNull(lookup);
        assertEquals(red.getCode(), lookup.getCode());
    }

    @Override
    protected void onSetUp() throws Exception {
        // remove existing lookups
        service = ArchetypeServiceHelper.getArchetypeService();
        LookupUtil.removeAll(service, "lookup.colour");
        LookupUtil.removeAll(service, "lookup.species");
    }

    /**
     * (non-Javadoc)
     *
     * @see AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
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
        Lookup lookup = LookupUtil.createLookup(service, shortName, code, name);
        service.save(lookup);
        return lookup;
    }

    /**
     * Helper to create an object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObject create(String shortName) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }


}
