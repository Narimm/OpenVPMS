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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Test the {@link LookupHandler}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class LookupHandlerTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Verifies that lookup codes are generated correctly.
     */
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

        CodeName breed = new CodeName("KELPIE", "Kelpie");
        CodeName species = new CodeName("CANINE", "Canine");
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

        assertEquals("CANINE", speciesLookup.getCode());
        assertEquals("KELPIE", breedLookup.getCode());

        assertEquals(speciesLookup.getObjectReference(),
                     relationship.getSource());
        assertEquals(breedLookup.getObjectReference(),
                     relationship.getTarget());
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
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
         * Returns a list of existing lookup codes for a lookup.
         *
         * @param descriptor the lookup descriptor
         * @return the set of existing lookup codes
         */
        @Override
        protected Set<String> getCodes(LookupDescriptor descriptor) {
            return Collections.emptySet();
        }

        /**
         * Determines if a lookup relationship is a duplicate.
         *
         * @param relationship the relationship
         * @return <tt>true</tt> if it is a duplicate, otherwise <tt>false</tt>
         */
        @Override
        protected boolean isDuplicate(LookupRelationship relationship) {
            return false;
        }

    }

}
