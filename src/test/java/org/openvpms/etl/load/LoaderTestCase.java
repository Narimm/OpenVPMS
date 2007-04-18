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

import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Tests the {@link Loader}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class LoaderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The DAO.
     */
    private ETLLogDAO dao;


    /**
     * Tests a single object.
     *
     * @throws Exception for any error
     */
    public void testObject() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);

        String loaderName = "CUSTLOAD";
        Loader loader = createLoader(loaderName, mappings);
        String legacyId = "ID1";
        ETLRow row = new ETLRow(legacyId);
        row.add("FIRST_NAME", "Foo");
        row.add("LAST_NAME", "Bar");

        List<IMObject> objects = loader.load(row);
        loader.close();
        assertEquals(1, objects.size());
        IMObject object = objects.get(0);
        IMObjectBean bean = new IMObjectBean(object, service);
        assertEquals("Foo", bean.getString("firstName"));
        assertEquals("Bar", bean.getString("lastName"));

        List<ETLLog> logs = dao.get(loaderName, legacyId, null);
        assertEquals(1, logs.size());
        checkLog(logs, loaderName, legacyId, "party.customerperson", -1);
    }

    /**
     * Tests a collection node.
     *
     * @throws Exception for any error
     */
    public void testCollection() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping addressMap = createMapping(
                "ADDRESS",
                "<party.customerperson>contacts[0]<contact.location>address");
        Mapping suburbMap = createMapping(
                "SUBURB",
                "<party.customerperson>contacts[0]<contact.location>suburb");
        Mapping phoneMap = createMapping("PHONE",
                                         "<party.customerperson>contacts[1]<contact.phoneNumber>telephoneNumber");
        mappings.addMapping(addressMap);
        mappings.addMapping(suburbMap);
        mappings.addMapping(phoneMap);

        String loaderName = "CUSTLOAD";
        Loader loader = createLoader(loaderName, mappings);
        String legacyId = "ID1";
        ETLRow row = new ETLRow(legacyId);
        row.add("ADDRESS", "49 Foo St Bar");
        row.add("SUBURB", "Coburg");
        row.add("PHONE", "123456789");

        List<IMObject> objects = loader.load(row);
        loader.close();
        assertEquals(3, objects.size());
        IMObjectBean customer = new IMObjectBean(objects.get(0));
        IMObjectBean location = new IMObjectBean(objects.get(1));
        IMObjectBean phone = new IMObjectBean(objects.get(2));
        assertTrue(customer.isA("party.customerperson"));
        assertTrue(location.isA("contact.location"));
        assertTrue(phone.isA("contact.phoneNumber"));

        List<ETLLog> logs = dao.get(loaderName, legacyId, null);
        assertEquals(3, logs.size());
        checkLog(logs, loaderName, legacyId, "party.customerperson", -1);
        checkLog(logs, loaderName, legacyId, "contact.location", 0);
        checkLog(logs, loaderName, legacyId, "contact.phoneNumber", 1);
    }

    /**
     * Tests a collection heirarchy.
     *
     * @throws Exception for any error
     */
    public void testCollectionHeirarchy() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        getClassification("lookup.contactPurpose", "MAILING");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        Mapping suburbMap = createMapping(
                "ADDRESS",
                "<party.customerperson>contacts[0]<contact.location>purposes[1]");
        String ref = "<lookup.contactPurpose>code=MAILING";
        suburbMap.setValue(ref);
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);
        mappings.addMapping(suburbMap);

        Loader loader = createLoader("CUSTLOAD", mappings);
        ETLRow row = new ETLRow("ID1");
        row.add("FIRST_NAME", "Foo");
        row.add("LAST_NAME", "Bar");
        row.add("LEGACY_ID", "ID1");
        row.add("ADDRESS", "49 Foo St Bar");
        List<IMObject> objects = loader.load(row);
        loader.close();

        assertEquals(2, objects.size());

        IMObjectBean customer = new IMObjectBean(objects.get(0));
        IMObjectBean location = new IMObjectBean(objects.get(1));
        assertTrue(customer.isA("party.customerperson"));
        assertTrue(location.isA("contact.location"));
    }

    /**
     * Verifies that the string <em>$value</em> is expanded with the input
     * value.
     */
    public void testValueExpansion() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("INVOICEID");

        Mapping mapping = createMapping(
                "INVOICEID",
                "<act.customerAccountChargesInvoice>items[0]<actRelationship.customerAccountInvoiceItem>source");
        mapping.setValue("<act.customerAccountChargesInvoice>$value");
        mappings.addMapping(mapping);

        Loader loader = createLoader("ACTLOAD", mappings);
        ETLRow row = new ETLRow("INVOICE1");
        row.add("INVOICEID", "INVOICE1");
        List<IMObject> objects = loader.load(row);
        loader.close();

        assertEquals(2, objects.size());
        IMObjectBean act = new IMObjectBean(objects.get(0));
        IMObjectBean rel = new IMObjectBean(objects.get(1));
        assertTrue(act.isA("act.customerAccountChargesInvoice"));
        assertTrue(rel.isA("actRelationship.customerAccountInvoiceItem"));
        List<ActRelationship> items
                = act.getValues("items", ActRelationship.class);
        assertEquals(items.size(), 1);
        assertEquals(rel.getObject(), items.get(0));
        assertEquals(act.getObject().getObjectReference(),
                     rel.getValue("source"));
    }

    /**
     * Verifies that an object isn't created if 'excludeNull' is specified
     * and the input field is null.
     */
    public void testExcludeNull() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("CUSTID");

        Mapping mapping = createMapping(
                "FAXNUMBER",
                "<party.customerperson>contacts[0]<contact.faxNumber>faxNumber");
        mapping.setExcludeNull(true);
        mappings.addMapping(mapping);

        Loader loader = createLoader("CUSTLOAD", mappings);
        ETLRow row = new ETLRow("CUSTID");
        row.add("FAXNUMBER", null);
        List<IMObject> objects = loader.load(row);
        loader.close();

        assertEquals(0, objects.size());
    }

    /**
     * Verifies that wildcards can be specified for archetype/legacyId
     * references.
     */
    public void testReferenceWildcards() {
        // create a product
        Product product = (Product) service.create("product.medication");
        assertNotNull(product);
        product.setName("XLoaderTestCaseProduct" + System.currentTimeMillis());
        service.save(product);

        // add a log for the product, so it can be referenced
        ETLLog productLog = new ETLLog("PRODLOADER", "PROD1",
                                       "product.medication");
        productLog.setLinkId(product.getLinkId());
        dao.save(productLog);

        // create a product participation mapping
        Mappings mappings = new Mappings();
        mappings.setIdColumn("INVOICEID");
        Mapping productMap = createMapping(
                "PRODUCTID",
                "<act.customerAccountInvoiceItem>product[0]<participation.product>entity",
                "<product.*>$value");
        mappings.addMapping(productMap);

        // load a (partial) invoice item
        Loader loader = createLoader("INVLOAD", mappings);
        ETLRow row = new ETLRow("INVOICEID");
        row.add("INVOICEID", "INV1");
        row.add("PRODUCTID", "PROD1");
        List<IMObject> objects = loader.load(row);
        loader.close();

        // verify the invoice and participation objects were created and
        // that they reference each other
        assertEquals(2, objects.size());
        IMObjectBean act = new IMObjectBean(objects.get(0));
        IMObjectBean participation = new IMObjectBean(objects.get(1));
        assertTrue(act.isA("act.customerAccountInvoiceItem"));
        assertTrue(participation.isA("participation.product"));

        List<Participation> participations
                = act.getValues("product", Participation.class);
        assertEquals(1, participations.size());
        assertEquals(participations.get(0), participation.getObject());
        assertEquals(product.getObjectReference(),
                     participation.getValue("entity"));
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
        dao = new ETLLogDAOTestImpl();
    }

    private void checkLog(List<ETLLog> logs, String loaderName,
                          String legacyId, String archetype, int index) {
        boolean found = false;
        for (ETLLog log : logs) {
            if (log.getLoader().equals(loaderName)
                    && log.getRowId().equals(legacyId)
                    && log.getArchetype().equals(archetype)
                    && log.getIndex() == index) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Helper to create a new mapping.
     *
     * @param source the source to map
     * @param target the target to map to
     * @return a new mapping
     */
    private Mapping createMapping(String source, String target) {
        return createMapping(source, target, null);
    }

    /**
     * Helper to create a new mapping.
     *
     * @param source the source to map
     * @param target the target to map to
     * @param value  the value. May be <tt>null</tt>
     * @return a new mapping
     */
    private Mapping createMapping(String source, String target, String value) {
        Mapping mapping = new Mapping();
        mapping.setSource(source);
        mapping.setTarget(target);
        mapping.setValue(value);
        return mapping;
    }

    /**
     * Gets a classification lookup, creating it if it doesn't exist.
     *
     * @param shortName the clasification short name
     * @param code      the classification code
     * @return the classification
     */
    private Lookup getClassification(String shortName, String code) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.add(new NodeConstraint("code", code));
        query.setMaxResults(1);
        QueryIterator<Lookup> iter = new IMObjectQueryIterator<Lookup>(query);
        if (iter.hasNext()) {
            return iter.next();
        }
        Lookup classification = (Lookup) service.create(shortName);
        classification.setCode(code);
        service.save(classification);
        return classification;
    }

    private Loader createLoader(String loaderName, Mappings mappings) {
        return new Loader(loaderName, mappings, dao, service,
                          new TestObjectHandler(loaderName, mappings, dao,
                                                service));
    }


    private class TestObjectHandler extends DefaultObjectHandler {

        public TestObjectHandler(String loaderName, Mappings mappings,
                                 ETLLogDAO dao, IArchetypeService service) {
            super(loaderName, mappings, dao, service);
        }

        /**
         * Saves a set of mapped objects.
         *
         * @param objects the objects to save
         */
        @Override
        protected void save(Collection<IMObject> objects,
                            Map<IMObject, ETLLog> logs,
                            Collection<ETLLog> errorLogs) {
            dao.save(logs.values());
            for (ETLLog errorLog : errorLogs) {
                dao.remove(errorLog.getLoader(), errorLog.getRowId());
            }
            dao.save(errorLogs);
        }
    }
}
