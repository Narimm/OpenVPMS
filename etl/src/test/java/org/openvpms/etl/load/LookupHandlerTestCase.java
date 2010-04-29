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

package org.openvpms.etl.load;

import junit.framework.AssertionFailedError;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Test the {@link LookupHandler}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("/applicationContext.xml")
public class LookupHandlerTestCase extends AbstractJUnit4SpringContextTests {

    /**
     * The archetype service.
     */
    @Autowired
    private IArchetypeService service;


    /**
     * Verifies that lookup codes are generated correctly.
     */
    @Test
    public void testCodeGeneration() {
        Mappings mappings = new Mappings();
        Mapping breedMap = createMapping("BREED", "<party.patientpet>breed");
        Mapping speciesMap = createMapping("SPECIES",
                                           "<party.patientpet>species");
        mappings.addMapping(breedMap);
        mappings.addMapping(speciesMap);
        TestLookupHandler handler = new TestLookupHandler(mappings);

        assertEquals("KELPIE", handler.getCode("Kelpie"));
        assertEquals("SMALL_DOG", handler.getCode("Small Dog"));
        assertEquals("WIERD_BREED_", handler.getCode("Wierd breed?"));
    }

    /**
     * Verifies that lookup and lookup relationships are generated.
     */
    @Test
    public void testLookupGeneration() {
        Mappings mappings = new Mappings();
        Mapping breedMap = createMapping("BREED", "<party.patientpet>breed");
        Mapping speciesMap = createMapping("SPECIES",
                                           "<party.patientpet>species");
        mappings.addMapping(breedMap);
        mappings.addMapping(speciesMap);

        TestLookupHandler handler = new TestLookupHandler(mappings);
        ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                "party.patientpet");
        assertNotNull(archetype);
        NodeDescriptor breedDesc = archetype.getNodeDescriptor("breed");
        assertNotNull(breedDesc);
        NodeDescriptor speciesDesc = archetype.getNodeDescriptor("species");
        assertNotNull(speciesDesc);

        String breedCode = "KELPIE" + System.currentTimeMillis();
        String speciesCode = "CANINE" + System.currentTimeMillis();
        CodeName breed = new CodeName(breedCode, "Kelpie");
        CodeName species = new CodeName(speciesCode, "Canine");
        Map<NodeDescriptor, CodeName> lookups
                = new HashMap<NodeDescriptor, CodeName>();
        lookups.put(breedDesc, breed);
        lookups.put(speciesDesc, species);
        handler.add(lookups);
        handler.commit();

        List<IMObject> objects = handler.getObjects();
        assertEquals(3, objects.size());
        Lookup speciesLookup = getLookup(objects, "lookup.species");
        Lookup breedLookup = getLookup(objects, "lookup.breed");
        LookupRelationship relationship = getRelationship(
                objects, "lookupRelationship.speciesBreed");

        assertEquals(breedCode, breedLookup.getCode());
        assertEquals(speciesCode, speciesLookup.getCode());

        assertEquals(speciesLookup.getObjectReference(),
                     relationship.getSource());
        assertEquals(breedLookup.getObjectReference(),
                     relationship.getTarget());
    }

    /**
     * Returns the lookup of the specified type.
     *
     * @param objects   the objects to search through
     * @param shortName the short name
     * @return the first lookup of the specified type
     * @throws AssertionFailedError if the object doesn't exist
     */
    private Lookup getLookup(List<IMObject> objects, String shortName) {
        return (Lookup) getObject(objects, shortName);
    }

    /**
     * Returns the relationship of the specified type.
     *
     * @param objects   the objects to search through
     * @param shortName the short name
     * @return the first relationship of the specified type
     * @throws AssertionFailedError if the object doesn't exist
     */
    private LookupRelationship getRelationship(List<IMObject> objects,
                                               String shortName) {
        return (LookupRelationship) getObject(objects, shortName);
    }

    /**
     * Returns an object of the specified type.
     *
     * @param objects   the objects
     * @param shortName the short name
     * @return the first object of the specified type
     * @throws AssertionFailedError if the object doesn't exist
     */
    private IMObject getObject(List<IMObject> objects, String shortName) {
        for (IMObject object : objects) {
            if (TypeHelper.isA(object, shortName)) {
                return object;
            }
        }
        fail("No object of type " + shortName);
        return null;
    }

    /**
     * Helper to create a new mapping.
     *
     * @param source the source to map
     * @param target the target to map to
     * @return a new mapping
     */
    private Mapping createMapping(String source, String target) {
        Mapping mapping = new Mapping();
        mapping.setSource(source);
        mapping.setTarget(target);
        return mapping;
    }

    class TestLookupHandler extends LookupHandler {

        private List<IMObject> objects = new ArrayList<IMObject>();

        public TestLookupHandler(Mappings mappings) {
            super(mappings, service);
        }

        /**
         * Returns the saved objects.
         *
         * @return the saved objects
         */
        public List<IMObject> getObjects() {
            return objects;
        }

        /**
         * Saves a collection of objects.
         *
         * @param objects the objects to save
         */
        @Override
        protected void save(Collection<IMObject> objects) {
            this.objects.addAll(objects);
        }

        /**
         * Determines if a lookup exists.
         *
         * @param archetype the lookup archetype short name
         * @param code      the lookup code
         * @return <tt>false</tt> to always generate lookups
         */
        @Override
        protected boolean exists(String archetype, String code) {
            return false;
        }

        /**
         * Determines if a lookup relationship exists.
         *
         * @param archetype the relationship archetype short name
         * @param source    the source lookup
         * @param target    the target lookup
         * @return <tt>false</tt> to always generate lookup relationships
         */
        @Override
        protected boolean exists(String archetype, Lookup source,
                                 Lookup target) {
            return false;
        }

    }

}
