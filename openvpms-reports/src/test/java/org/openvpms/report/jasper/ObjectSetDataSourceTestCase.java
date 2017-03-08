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
import org.apache.commons.jxpath.Functions;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.Parameters;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ObjectSetDataSource} class.
 *
 * @author Tim Anderson
 */
public class ObjectSetDataSourceTestCase extends AbstractDataSourceTest<ObjectSet> {

    /**
     * Tests the data source.
     *
     * @throws JRException for any error
     */
    @Test
    public void testDataSource() throws JRException {
        Party customer1 = TestHelper.createCustomer("F", "Bourke", true);
        Party customer2 = TestHelper.createCustomer("J", "Smith", true);
        List<ObjectSet> objects = createCollection(customer1, customer2);
        DataSource source = createDataSource(objects, new Parameters(null), null);
        assertTrue(source.next());
        assertEquals("Bourke", source.getFieldValue("customer.lastName"));
        assertTrue(source.next());
        assertEquals("Smith", source.getFieldValue("customer.lastName"));
        assertFalse(source.next());
    }


    /**
     * Tests the {@link IMObjectDataSource#getDataSource(String)} method.
     */
    @Test
    public void testGetDataSource() throws JRException {
        Party customer = TestHelper.createCustomer("Foo", "Bar", true);
        Party patient = TestHelper.createPatient(customer);
        patient.setName("Fido");
        save(patient);

        DataSource source = createDataSource(createCollection(customer), new Parameters(null), null);
        assertTrue(source.next());

        DataSource cust = (DataSource) source.getDataSource("customer");
        assertTrue(cust.next());
        assertEquals(cust.getFieldValue("lastName"), "Bar");

        DataSource patients = (DataSource) cust.getDataSource("patients");
        assertTrue(patients.next());

        assertEquals("Fido", patients.getFieldValue("target.name"));
        assertFalse(patients.next());
    }

    /**
     * Creates a collection of objects to pass to the data source.
     *
     * @param customers the objects
     * @return a collection to pass to the data source
     */
    @Override
    protected List<ObjectSet> createCollection(Party... customers) {
        List<ObjectSet> result = new ArrayList<>();
        for (Party customer : customers) {
            ObjectSet set = new ObjectSet();
            set.set("customer", customer);
            result.add(set);
        }
        return result;
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
    protected DataSource createDataSource(List<ObjectSet> objects, Parameters parameters, PropertySet fields,
                                          DocumentHandlers handlers, Functions functions) {
        return new ObjectSetDataSource(objects, parameters, fields, getArchetypeService(), getLookupService(), handlers,
                                       functions);
    }
}
