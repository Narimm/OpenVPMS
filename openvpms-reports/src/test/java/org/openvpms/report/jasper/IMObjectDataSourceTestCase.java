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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.commons.jxpath.Functions;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link IMObjectDataSource} class.
 *
 * @author Tim Anderson
 */
public class IMObjectDataSourceTestCase extends AbstractDataSourceTest<IMObject> {

    /**
     * Tests basic data source functionality.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDataSource() throws Exception {
        IMObject object = createCustomer("Foo", "Bar");
        IMObjectDataSource ds = createDataSource(object);
        JRField firstName = createField("firstName", String.class);
        JRField lastName = createField("lastName", String.class);

        assertTrue(ds.next());
        assertEquals("Foo", ds.getFieldValue(firstName));
        assertEquals("Bar", ds.getFieldValue(lastName));
        assertFalse(ds.next());
    }

    /**
     * Tests handling of documents.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDocument() throws Exception {
        // create a new act and associate an image with it
        ActBean act = createAct("act.patientDocumentImage");
        File file = new File("src/test/images/openvpms.gif");
        Document doc = DocumentHelper.create(file, "image/gif", applicationContext.getBean(DocumentHandlers.class));
        getArchetypeService().save(doc);
        act.setValue("document", doc.getObjectReference());
        IMObjectDataSource ds = createDataSource(act.getAct());

        // get the image field
        JRField document = createField("document", InputStream.class);
        InputStream stream = (InputStream) ds.getFieldValue(document);

        // verify the image contents match that expected
        FileInputStream expectedStream = new FileInputStream(file);
        int expected;
        while ((expected = expectedStream.read()) != -1) {
            assertEquals(expected, stream.read());
        }
        assertEquals(-1, stream.read());
        expectedStream.close();
        stream.close();
    }

    /**
     * Tests fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testFields() throws Exception {
        IMObject object = createCustomer("Foo", "Bar");
        Map<String, Object> fields = new HashMap<>();
        fields.put("fieldA", "A");
        fields.put("field.B", "B");
        fields.put("field1", 1);
        fields.put("OpenVPMS.customer", object);
        fields.put("title", "OVERRIDE");

        IMObjectDataSource ds = createDataSource(object, fields);
        JRField firstName = createField("firstName", String.class);
        JRField lastName = createField("lastName", String.class);
        JRField fieldA = createField("fieldA", String.class);
        JRField fieldB = createField("field.B", String.class);
        JRField field1 = createField("field1", Integer.class);
        JRField customerFirstName = createField("OpenVPMS.customer.firstName", String.class);
        JRField title = createField("title", String.class); // overrides the default field

        // verify that fields can also be accessed as variables.
        JRField exprCustomerFirstName = createField("[openvpms:get($OpenVPMS.customer, 'firstName')]", String.class);
        JRField exprCustomerLastName = createField("[openvpms:get($OpenVPMS.customer, 'lastName')]", String.class);

        assertTrue(ds.next());
        assertEquals("Foo", ds.getFieldValue(firstName));
        assertEquals("Bar", ds.getFieldValue(lastName));
        assertEquals("A", ds.getFieldValue(fieldA));
        assertEquals("B", ds.getFieldValue(fieldB));
        assertEquals(1, ds.getFieldValue(field1));
        assertEquals("Foo", ds.getFieldValue(customerFirstName));
        assertEquals("OVERRIDE", ds.getFieldValue(title));

        assertEquals("Foo", ds.getFieldValue(exprCustomerFirstName));
        assertEquals("Bar", ds.getFieldValue(exprCustomerLastName));

        assertFalse(ds.next());
    }

    /**
     * Tests the {@link IMObjectDataSource#getDataSource(String)} method.
     */
    @Test
    public void testGetDataSource() throws JRException {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        patient.setName("Fido");
        save(patient);

        IMObjectDataSource ds = createDataSource(customer, new HashMap<String, Object>());
        DataSource patients = (DataSource) ds.getDataSource("patients");
        assertTrue(patients.next());

        assertEquals("Fido", patients.getFieldValue("target.name"));
        assertFalse(patients.next());
    }


    /**
     * Creates a new data source.
     *
     * @param objects    the objects
     * @param parameters the parameters
     * @param fields     the fields
     * @param handlers   the document handlers
     * @param functions  the functions
     * @return a new data source
     */
    @Override
    protected DataSource createDataSource(List<IMObject> objects, Parameters parameters, PropertySet fields,
                                          DocumentHandlers handlers, Functions functions) {
        if (objects.size() != 1) {
            throw new IllegalArgumentException("Argument 'objects' must contain a single element");
        }
        return new IMObjectDataSource(objects.get(0), parameters, fields, getArchetypeService(), getLookupService(),
                                      handlers, functions);
    }

    /**
     * Creates a collection of customers to pass to the data source.
     *
     * @param customers the customers
     * @return a collection to pass to the data source
     */
    @Override
    protected List<IMObject> createCollection(Party... customers) {
        return Arrays.<IMObject>asList(customers);
    }

    /**
     * Helper to create a new data source.
     *
     * @param object the object
     * @return a new data source
     */
    private IMObjectDataSource createDataSource(IMObject object) {
        return createDataSource(object, null);
    }

    /**
     * Helper to create a new data source.
     *
     * @param object the object
     * @param fields a map of additional field names and their values, to pass to the report. May be {@code null}
     * @return a new data source
     */
    private IMObjectDataSource createDataSource(IMObject object, Map<String, Object> fields) {
        Functions functions = applicationContext.getBean(Functions.class);
        DocumentHandlers handlers = applicationContext.getBean(DocumentHandlers.class);
        return new IMObjectDataSource(object, null, fields, getArchetypeService(), getLookupService(), handlers,
                                      functions);
    }

}
