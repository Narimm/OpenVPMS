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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.etl.ETLValue;
import org.openvpms.etl.ETLValueDAO;
import org.openvpms.etl.ETLValueDAOTestImpl;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.ArrayList;
import java.util.List;


/**
 * Tests the {@link LookupLoader}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupLoaderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests loading of lookups with <em>type="lookup"<em>.
     */
    public void testLoadLookup() {
        ETLValue mr = new ETLValue("IDCUST1", "party.customerperson",
                                   "ID1", "title", "MR");
        ETLValue ms = new ETLValue("IDCUST2", "party.customerperson",
                                   "ID2", "title", "MS");
        ETLValueDAO dao = new ETLValueDAOTestImpl();
        dao.save(mr);
        dao.save(ms);

        TestDefaultLookupLoaderHandler listener = new TestDefaultLookupLoaderHandler(
                service);
        LookupLoader loader = new LookupLoader(dao, service, listener);
        loader.load();
        List<IMObject> objects = listener.getObjects();
        assertEquals(2, objects.size());
        checkLookup(objects, "lookup.personTitle", "MR");
        checkLookup(objects, "lookup.personTitle", "MS");
    }

    /**
     * Tests loading of lookups with <em>type="targetLookup"<em>.
     */
    public void testLoadTargetLookup() {
        ETLValue species1 = new ETLValue("IDPET1", "party.patientpet",
                                         "ID1", "species", "CANINE");
        ETLValue breed1 = new ETLValue("IDPET1", "party.patientpet",
                                       "ID1", "breed", "BEAGLE");
        ETLValue species2 = new ETLValue("IDPET2", "party.patientpet",
                                         "ID1", "species", "FELINE");
        ETLValue breed2 = new ETLValue("IDPET2", "party.patientpet",
                                       "ID1", "breed", "BURMESE");
        ETLValueDAO dao = new ETLValueDAOTestImpl();
        dao.save(species1);
        dao.save(breed1);
        dao.save(species2);
        dao.save(breed2);

        TestDefaultLookupLoaderHandler listener = new TestDefaultLookupLoaderHandler(
                service);
        LookupLoader loader = new LookupLoader(dao, service, listener);
        loader.load();
        List<IMObject> objects = listener.getObjects();
        assertEquals(6, objects.size());
        checkLookup(objects, "lookup.species", "CANINE");
        checkLookup(objects, "lookup.breed", "BEAGLE");
        checkRelationship(objects, "lookupRelationship.speciesBreed",
                          "CANINE", "BEAGLE");
        checkLookup(objects, "lookup.species", "FELINE");
        checkLookup(objects, "lookup.breed", "BURMESE");
        checkRelationship(objects, "lookupRelationship.speciesBreed",
                          "FELINE", "BURMESE");
    }

    /**
     * Verifies that lookups with <em>type="lookup.local"</em> aren't loaded.
     */
    public void testLookupLocal() {
        ETLValue male = new ETLValue("IDPET1", "party.patientpet",
                                         "ID1", "sex", "MALE");
        ETLValue female = new ETLValue("IDPET2", "party.patientpet",
                                       "ID1", "sex", "FEMALE");
        ETLValueDAO dao = new ETLValueDAOTestImpl();
        dao.save(male);
        dao.save(female);

        TestDefaultLookupLoaderHandler listener = new TestDefaultLookupLoaderHandler(
                service);
        LookupLoader loader = new LookupLoader(dao, service, listener);
        loader.load();
        assertTrue(listener.getObjects().isEmpty());
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
     * Verifies that a list contains the expected lookup.
     *
     * @param objects   the objects to check
     * @param shortName the expected lookup short name
     * @param code      the expected lookup code
     */
    private void checkLookup(List<IMObject> objects, String shortName,
                             String code) {
        boolean found = false;
        for (IMObject object : objects) {
            if (object instanceof Lookup && TypeHelper.isA(object, shortName)) {
                Lookup lookup = (Lookup) object;
                if (code.equals(lookup.getCode())) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
        ArchetypeQuery query = new ArchetypeQuery(shortName, true, true);
        query.add(new NodeConstraint("code", code));
        List<IMObject> match = service.get(query).getResults();
        assertEquals(1, match.size());
    }

    /**
     * Verifies that a list constains the expected lookup relationship.
     *
     * @param objects    the object to check
     * @param shortName  the expected lookup short name
     * @param sourceCode the expected source lookup code
     * @param targetCode the expected target lookup code
     */
    private void checkRelationship(List<IMObject> objects, String shortName,
                                   String sourceCode, String targetCode) {
        boolean found = false;
        Lookup source = null;
        Lookup target = null;
        for (IMObject object : objects) {
            if (object instanceof LookupRelationship
                    && TypeHelper.isA(object, shortName)) {
                LookupRelationship r = (LookupRelationship) object;
                source = (Lookup) ArchetypeQueryHelper.getByObjectReference(
                        service, r.getSource());
                target = (Lookup) ArchetypeQueryHelper.getByObjectReference(
                        service, r.getTarget());
                if (source != null && sourceCode.equals(source.getCode())
                        && target != null && targetCode.equals(
                        target.getCode())) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
        ArchetypeQuery query = new ArchetypeQuery(shortName, true, true);
        query.add(new ObjectRefNodeConstraint("source", source.getObjectReference()));
        query.add(new ObjectRefNodeConstraint("target", target.getObjectReference()));
        List<IMObject> results = service.get(query).getResults();
        assertEquals(1, results.size());
    }

    /**
     * Test lookup loader listener.
     */
    private class TestDefaultLookupLoaderHandler extends DefaultLookupLoaderHandler {

        /**
         * The loaded objects.
         */
        private List<IMObject> loaded = new ArrayList<IMObject>();


        /**
         * Constructs a new <tt>TestLookupLoaderListener</tt>.
         *
         * @param service the archetype service
         */
        public TestDefaultLookupLoaderHandler(IArchetypeService service) {
            super(service, true);
        }

        /**
         * Returns the loaded lookups.
         *
         * @return the loaded lookups
         */
        public List<IMObject> getObjects() {
            return loaded;
        }

        @Override
        public void add(IMObject object, Context context) {
            super.add(object, context);
            loaded.add(object);
        }
    }
}
