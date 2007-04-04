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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.etl.ETLValue;
import org.openvpms.etl.ETLValueDAO;
import org.openvpms.etl.ETLValueDAOTestImpl;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Tests the {@link Loader} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Tests creation of a single object with two nodes.
     */
    public void testSingle() {
        ETLValue firstName = new ETLValue("IDCUST", "party.customerperson",
                                          "ID1", "firstName", "Foo");
        ETLValue lastName = new ETLValue("IDCUST", "party.customerperson",
                                         "ID1",
                                         "lastName", "Bar");
        ETLValueDAO dao = new ETLValueDAOTestImpl();
        dao.save(firstName);
        dao.save(lastName);

        TestLoader loader = new TestLoader(dao, service);
        int count = loader.load();
        assertEquals(1, count);
        assertEquals(1, loader.getObjects().size());
        IMObject loaded = loader.getObject("IDCUST");
        assertNotNull(loaded);
        assertEquals("party.customerperson",
                     loaded.getArchetypeId().getShortName());
        IMObjectBean bean = new IMObjectBean(loaded);
        assertEquals("Foo", bean.getString("firstName"));
        assertEquals("Bar", bean.getString("lastName"));
    }

    /**
     * Tests a collection of contacts associated with a customer.
     */
    public void testCollection() {
        ETLValueDAO dao = new ETLValueDAOTestImpl();
        ETLValue firstName = new ETLValue("IDCUST", "party.customerperson",
                                          "ID1", "firstName", "Foo");
        ETLValue lastName = new ETLValue("IDCUST", "party.customerperson",
                                         "ID1",
                                         "lastName", "Bar");
        ETLValue contacts1 = new ETLValue("IDCUST", "party.customerperson",
                                          "ID1", "contacts", 0, "IDCONT1",
                                          true);
        ETLValue contacts2 = new ETLValue("IDCUST", "party.customerperson",
                                          "ID1", "contacts", 1, "IDCONT2",
                                          true);
        ETLValue phone1 = new ETLValue("IDCONT1", "contact.phoneNumber", "ID1",
                                       "telephoneNumber", "123456789");
        ETLValue phone2 = new ETLValue("IDCONT2", "contact.phoneNumber", "ID1",
                                       "telephoneNumber", "987654321");
        dao.save(firstName);
        dao.save(lastName);
        dao.save(contacts1);
        dao.save(contacts2);
        dao.save(phone1);
        dao.save(phone2);

        TestLoader loader = new TestLoader(dao, service);
        int count = loader.load();
        assertEquals(3, count);
        assertEquals(3, loader.getObjects().size());
        IMObject party = loader.getObject("IDCUST");
        IMObject contact1 = loader.getObject("IDCONT1");
        IMObject contact2 = loader.getObject("IDCONT2");
        assertTrue(TypeHelper.isA(party, "party.customerperson"));
        assertTrue(TypeHelper.isA(contact1, "contact.phoneNumber"));
        assertTrue(TypeHelper.isA(contact2, "contact.phoneNumber"));
        IMObjectBean bean = new IMObjectBean(party);
        assertEquals("Foo", bean.getString("firstName"));
        assertEquals("Bar", bean.getString("lastName"));
    }

    /**
     * Tests <em>act.customerEstimation</em> associated with an
     * <em>act.customerEstimationItem<em>, each with multiple participations.
     */
    public void testActs() {
        ETLValueDAO dao = new ETLValueDAOTestImpl();
        List<ETLValue> customerValues = createCustomer("IDCUST");
        List<ETLValue> patientValues = createPatient("IDPET");
        List<ETLValue> productValues = createProduct("IDPROD");
        List<ETLValue> estimationItemValues
                = createEstimationItem("IDITEM", "IDPET", "IDPROD");
        List<ETLValue> estimationValues = createEstimation("IDEST", "IDCUST",
                                                           "IDITEM");
        dao.save(customerValues);
        dao.save(patientValues);
        dao.save(productValues);
        dao.save(estimationValues);
        dao.save(estimationItemValues);

        TestLoader loader = new TestLoader(dao, service);
        int count = loader.load();

        assertEquals(9, count);
        assertEquals(9, loader.getObjects().size());

        IMObject customer = loader.getObject("IDCUST");
        IMObject patient = loader.getObject("IDPET");
        IMObject product = loader.getObject("IDPROD");
        IMObject estimationItem = loader.getObject("IDITEM");
        IMObject estimation = loader.getObject("IDEST");

        assertTrue(TypeHelper.isA(customer, "party.customerperson"));
        assertTrue(TypeHelper.isA(patient, "party.patientpet"));
        assertTrue(TypeHelper.isA(product, "product.medication"));
        assertTrue(TypeHelper.isA(estimationItem,
                                  "act.customerEstimationItem"));
        assertTrue(TypeHelper.isA(estimation, "act.customerEstimation"));
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

    private List<ETLValue> createEstimation(String objectId,
                                            String customerObjectId,
                                            String itemObjectId) {
        List<ETLValue> customerParticipation = createParticipation(
                "IDESTCUST", "participation.customer", objectId,
                customerObjectId);
        List<ETLValue> relationship = createActRelationship(
                "IDACTREL", "actRelationship.customerEstimationItem",
                objectId,
                itemObjectId);
        ETLValue customer = new ETLValue(objectId, "act.customerEstimation",
                                         "ID3", "customer", -1, "IDESTCUST",
                                         true);
        ETLValue items = new ETLValue(objectId, "act.customerEstimation",
                                      "ID3", "items", 0, "IDACTREL", true);
        List<ETLValue> result = new ArrayList<ETLValue>();
        result.addAll(customerParticipation);
        result.addAll(relationship);
        result.add(customer);
        result.add(items);
        return result;
    }

    private List<ETLValue> createActRelationship(String objectId,
                                                 String shortName,
                                                 String sourceObjectId,
                                                 String targetObjectId) {
        ETLValue source = new ETLValue(objectId, shortName, "ID2", "source", -1,
                                       sourceObjectId, true);
        ETLValue target = new ETLValue(objectId, shortName, "ID2", "target", -1,
                                       targetObjectId, true);
        return Arrays.asList(source, target);
    }

    private List<ETLValue> createEstimationItem(String objectId,
                                                String patientObjectId,
                                                String productObjectId) {
        List<ETLValue> patientParticipation = createParticipation(
                "IDITEMPET", "participation.patient", objectId,
                patientObjectId);
        List<ETLValue> productParticipation = createParticipation(
                "IDITEMPROD", "participation.product", objectId,
                productObjectId);
        ETLValue patient = new ETLValue(objectId, "act.customerEstimationItem",
                                        "ID3", "patient", -1, "IDITEMPET",
                                        true);
        ETLValue product = new ETLValue(objectId, "act.customerEstimationItem",
                                        "ID3", "product", -1, "IDITEMPROD",
                                        true);
        List<ETLValue> result = new ArrayList<ETLValue>();
        result.addAll(patientParticipation);
        result.addAll(productParticipation);
        result.add(product);
        result.add(patient);
        return result;
    }

    private List<ETLValue> createParticipation(String objectId,
                                               String shortName,
                                               String actObjectId,
                                               String entityObjectId) {
        ETLValue act = new ETLValue(objectId, shortName, "ID2", "act", -1,
                                    actObjectId, true);
        ETLValue entity = new ETLValue(objectId, shortName, "ID2", "entity", -1,
                                       entityObjectId, true);
        return Arrays.asList(act, entity);
    }

    private List<ETLValue> createCustomer(String objectId) {
        ETLValue firstName = new ETLValue(objectId, "party.customerperson",
                                          "ID1", "firstName", "Foo");
        ETLValue lastName = new ETLValue(objectId, "party.customerperson",
                                         "ID1", "lastName", "Bar");
        return Arrays.asList(firstName, lastName);
    }

    private List<ETLValue> createPatient(String objectId) {
        ETLValue name = new ETLValue(objectId, "party.patientpet",
                                     "ID1", "name", "XSpot");
        ETLValue species = new ETLValue(objectId, "party.patientpet",
                                        "ID1", "species", "CANINE");
        return Arrays.asList(name, species);
    }

    private List<ETLValue> createProduct(String objectId) {
        ETLValue name = new ETLValue(objectId, "product.medication",
                                     "ID1", "name", "XMedication");
        return Arrays.asList(name);
    }

    /**
     * Test loader.
     */
    private class TestLoader extends Loader {

        /**
         * The loaded objects, keyed on objectId.
         */
        private Map<String, IMObject> loaded
                = new LinkedHashMap<String, IMObject>();


        /**
         * Constructs a new <tt>TestLoader</tt>.
         *
         * @param service the archetype service
         */
        public TestLoader(ETLValueDAO dao, IArchetypeService service) {
            super(dao, service, true, true);
        }

        /**
         * Returns the loaded objects.
         *
         * @return the loaded objects
         */
        public Collection<IMObject> getObjects() {
            return loaded.values();
        }

        /**
         * Returns a loaded object, given the source object identifier.
         *
         * @param objectId the object identifier
         * @return the corresponding object, or <tt>null</tt> if none is found
         */
        public IMObject getObject(String objectId) {
            return loaded.get(objectId);
        }

        /**
         * Queues an object to be saved, flushing the batch if the batch size is
         * reached.
         *
         * @param objectId  the source object identifier
         * @param reference the object reference
         * @param target    the object to queue
         */
        @Override
        protected void queue(String objectId, IMObjectReference reference,
                             IMObject target) {
            super.queue(objectId, reference, target);
            loaded.put(objectId, target);
        }
    }
}
