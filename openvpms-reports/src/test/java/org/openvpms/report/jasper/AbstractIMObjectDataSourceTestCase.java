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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignField;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.AbstractReportTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Base class for {@link AbstractIMObjectDataSource} test cases.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectDataSourceTestCase extends AbstractReportTest {

    /**
     * The document handlers.
     */
    @Autowired
    protected DocumentHandlers handlers;

    /**
     * Tests the {@link AbstractIMObjectDataSource#getExpressionDataSource(String)} method.
     *
     * @param dataSource     the data source
     * @param expectedFields fields expected to be available to the data source
     * @throws Exception for any error
     */
    protected void checkExpressionDataSource(AbstractIMObjectDataSource dataSource, PropertySet expectedFields)
            throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        Lookup lookup1 = TestHelper.getLookup("lookup.state", "VIC");
        Lookup lookup2 = TestHelper.getLookup("lookup.state", "QLD");
        properties.put("xtest", new TestFunctions(Arrays.<IMObject>asList(lookup1, lookup2)));
        new JXPathHelper(properties);

        JRField code = createField("code", String.class);

        // test expressions returning Iterable
        JRDataSource expressionDataSource1 = dataSource.getExpressionDataSource("xtest:getIterable()");
        assertTrue(expressionDataSource1.next());
        assertEquals(lookup1.getCode(), expressionDataSource1.getFieldValue(code));
        assertTrue(expressionDataSource1.next());
        assertEquals(lookup2.getCode(), expressionDataSource1.getFieldValue(code));
        checkFields(expressionDataSource1, expectedFields);
        assertFalse(expressionDataSource1.next());

        // test expressions returning Iterator
        JRDataSource expressionDataSource2 = dataSource.getExpressionDataSource("xtest:getIterator()");
        assertTrue(expressionDataSource2.next());
        assertEquals(lookup1.getCode(), expressionDataSource2.getFieldValue(code));
        assertTrue(expressionDataSource2.next());
        assertEquals(lookup2.getCode(), expressionDataSource2.getFieldValue(code));
        checkFields(expressionDataSource2, expectedFields);
        assertFalse(expressionDataSource2.next());

        // test expressions returning neither Iterable nor Iterator
        try {
            dataSource.getExpressionDataSource("xtest:invalid()");
            fail("Expected getExpressionDataSource() to fail");
        } catch (JRException expected) {
            // do nothing
        }
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

    /**
     * Helper class for testing {@link AbstractIMObjectDataSource#getExpressionDataSource(String)}.
     */
    public static class TestFunctions {

        private List<IMObject> objects;

        public TestFunctions(List<IMObject> objects) {
            this.objects = objects;
        }

        public Iterable<IMObject> getIterable() {
            return objects;
        }

        public Iterator<IMObject> getIterator() {
            return objects.iterator();
        }

        public void invalid() {
        }
    }
}
