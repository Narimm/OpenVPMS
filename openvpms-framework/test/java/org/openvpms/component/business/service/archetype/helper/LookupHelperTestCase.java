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
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.List;


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
        String code = "CANINE-" + System.currentTimeMillis();
        String name = "Canine";

        createLookup("lookup.species", code, name);

        IMObject pet = service.create("animal.pet");
        IMObjectBean bean = new IMObjectBean(pet, service);
        bean.setValue("species", code);

        NodeDescriptor species = bean.getDescriptor("species");
        assertNotNull(species);
        assertEquals(name, LookupHelper.getName(service, species, pet));
    }

    /**
     * Tests the {@link LookupHelper#getName} method for a target lookup.
     */
    public void testGetNameTargetLookup() {
        String speciesCode = "CANINE-" + System.currentTimeMillis();
        String speciesName = "Canine";

        String breedCode = "KELPIE-" + System.currentTimeMillis();
        String breedName = "Kelpie";

        Lookup speciesLookup = createLookup("lookup.species", speciesCode,
                                            speciesName);
        Lookup breedLookup = createLookup("lookup.breed", breedCode, breedName);

        IMObject pet = service.create("animal.pet");
        IMObjectBean bean = new IMObjectBean(pet, service);
        bean.setValue("species", speciesCode);
        bean.setValue("breed", breedCode);

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

        assertEquals(breedName, LookupHelper.getName(service, breed, pet));
    }

    /**
     * Tests the {@link LookupHelper#getName} method for a local lookup.
     */
    public void testGetNameLocalLookup() {
        IMObject pet = service.create("animal.pet");
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
        String shortName = "lookup.colour";

        // create two new colour lookups. Make RED the default.
        Lookup red = (Lookup) create(shortName);
        red.setCode("RED");
        red.setDefaultLookup(true);

        Lookup blue = (Lookup) create(shortName);
        blue.setCode("BLUE");
        blue.setDefaultLookup(false);

        service.save(red);
        service.save(blue);

        // verify the correct lookup is returned
        Lookup lookup = LookupHelper.getDefaultLookup(service, shortName);
        assertNotNull(lookup);
        assertEquals("RED", lookup.getCode());
    }

    @Override
    protected void onSetUp() throws Exception {
        // remove existing colour lookup
        service = ArchetypeServiceHelper.getArchetypeService();
        String shortName = "lookup.colour";
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> lookups = service.get(query).getResults();
        for (IMObject lookup : lookups) {
            service.remove(lookup);
        }
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
     * @param archetype the lookup archetype short name
     * @param code the lookup code
     * @param name the lookup name
     * @return a new lookup
     */
    private Lookup createLookup(String archetype, String code, String name) {
        Lookup lookup = (Lookup) create(archetype);
        lookup.setCode(code);
        lookup.setName(name);
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
