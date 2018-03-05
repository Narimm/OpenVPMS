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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;
import org.apache.commons.jxpath.Functions;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.AbstractReportTest;
import org.openvpms.report.Parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Base class for {@link AbstractDataSource} test cases.
 *
 * @author Tim Anderson
 */
public abstract class AbstractDataSourceTest<T> extends AbstractReportTest {

    /**
     * Helper class for testing {@link AbstractDataSource#getExpressionDataSource(String)}.
     */
    public static class TestFunctions {

        private List<IMObject> objects;

        public TestFunctions(List<IMObject> objects) {
            this.objects = objects;
        }

        public Iterable<IMObject> getIterable() {
            return objects;
        }

        public void invalid() {
        }
    }

    /**
     * Tests the {@link DataSource#getExpressionDataSource(String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExpressionDataSource() throws Exception {
        Party customer = TestHelper.createCustomer(false);
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName("Fido");
        List<T> objects = createCollection(customer);
        Map<String, Object> fields = new HashMap<>();
        fields.put("Globals.A", "A");
        fields.put("Globals.1", 1);
        fields.put("OpenVPMS.patient", patient);
        PropertySet f = new ResolvingPropertySet(fields, getArchetypeService(), getLookupService());
        DataSource source = createDataSource(objects, new Parameters(null), f);
        assertTrue(source.next());
        checkExpressionDataSource(source, f);
    }

    /**
     * Creates a new data source.
     *
     * @param objects    the objects
     * @param parameters the parameters
     * @param fields     the fields
     * @return a new data source
     */
    protected DataSource createDataSource(List<T> objects, Parameters parameters, PropertySet fields) {
        Functions functions = applicationContext.getBean(Functions.class);
        DocumentHandlers handlers = applicationContext.getBean(DocumentHandlers.class);
        return createDataSource(objects, parameters, fields, handlers, functions);
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
    protected abstract DataSource createDataSource(List<T> objects, Parameters parameters, PropertySet fields,
                                                   DocumentHandlers handlers, Functions functions);

    /**
     * Creates a collection of customers to pass to the data source.
     *
     * @param customers the customers
     * @return a collection to pass to the data source
     */
    protected abstract List<T> createCollection(Party... customers);

    /**
     * Tests the {@link AbstractDataSource#getExpressionDataSource(String)} method.
     *
     * @param dataSource     the data source
     * @param expectedFields fields expected to be available to the data source
     * @throws Exception for any error
     */
    protected void checkExpressionDataSource(DataSource dataSource, PropertySet expectedFields)
            throws Exception {
        Map<String, Object> properties = new HashMap<>();
        Lookup lookup1 = TestHelper.getLookup("lookup.state", "VIC");
        Lookup lookup2 = TestHelper.getLookup("lookup.state", "QLD");
        properties.put("xtest", new TestFunctions(Arrays.<IMObject>asList(lookup1, lookup2)));
        new JXPathHelper(properties);

        JRField code = createField("code", String.class);

        // test expressions returning Iterable
        JRRewindableDataSource expressionDataSource1 = dataSource.getExpressionDataSource("xtest:getIterable()");
        assertTrue(expressionDataSource1.next());
        assertEquals(lookup1.getCode(), expressionDataSource1.getFieldValue(code));
        assertTrue(expressionDataSource1.next());
        assertEquals(lookup2.getCode(), expressionDataSource1.getFieldValue(code));
        checkFields(expressionDataSource1, expectedFields);
        assertFalse(expressionDataSource1.next());

        // reset the source and check we can rewind
        expressionDataSource1.moveFirst();
        assertTrue(expressionDataSource1.next());
        assertEquals(lookup1.getCode(), expressionDataSource1.getFieldValue(code));

        // test expressions not returning Iterable
        try {
            dataSource.getExpressionDataSource("xtest:invalid()");
            fail("Expected getExpressionDataSource() to fail");
        } catch (JRException expected) {
            // do nothing
        }

        // test expression accessing field
        JRRewindableDataSource expressionDataSource2 = dataSource.getExpressionDataSource("$OpenVPMS.patient");
        assertTrue(expressionDataSource2.next());
        JRField name = createField("name", String.class);
        assertEquals("Fido", expressionDataSource2.getFieldValue(name));
        assertFalse(expressionDataSource2.next());
    }

    /**
     * Helper to create a new field.
     *
     * @param name       the field name
     * @param valueClass the field value class
     * @return a new field
     */
    protected JRField createField(String name, Class valueClass) {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(valueClass);
        return field;
    }

    /**
     * Verifies that the expected fields are accessible via the data source.
     *
     * @param dataSource     the data source
     * @param expectedFields the expected fields
     * @throws JRException for any JasperReports error
     */
    private void checkFields(JRDataSource dataSource, PropertySet expectedFields) throws JRException {
        for (String name : expectedFields.getNames()) {
            Object expected = expectedFields.get(name);
            Object value = dataSource.getFieldValue(createField(name, expected.getClass()));
            assertEquals(expected, value);
        }
    }
}
