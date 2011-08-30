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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.List;


/**
 * Tests the {@link CustomerQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerQueryTestCase extends AbstractEntityQueryTest<Party> {

    /**
     * Customer archetype short names.
     */
    private static final String[] SHORT_NAMES = new String[]{CustomerArchetypes.PERSON, CustomerArchetypes.OTC};

    /**
     * Tests querying customers by contact details.
     */
    @Test
    public void testQueryByContact() {
        Party customer = createObject(true);
        String address = getUniqueValue("ZAddress");

        CustomerQuery query = (CustomerQuery) createQuery();
        query.setContact(address);
        ResultSet<Party> results = query.query();
        assertNotNull(results);
        assertEquals(0, results.getResults());
        checkSelects(false, query, customer);

        Contact location = TestHelper.createLocationContact(address, "VIC", "MELBOURNE", "3001");
        customer.addContact(location);
        save(customer);

        results = query.query();
        List<Party> list = results.getPage(0).getResults();
        assertEquals(1, list.size());
        assertEquals(customer, list.get(0));
        checkSelects(true, query, customer);
    }

    /**
     * Tests querying customers by patient name.
     */
    @Test
    public void testQueryByPatientName() {
        String patientName = getUniqueValue("ZPatient");
        Party customer = createObject(true);

        CustomerQuery query = (CustomerQuery) createQuery();
        query.setPatient(patientName);
        ResultSet<Party> results = query.query();
        assertNotNull(results);
        assertEquals(0, results.getResults());
        checkSelects(false, query, customer);

        // create a patient owned by the customer
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName(patientName);
        save(customer, patient);

        List<Party> list = results.getPage(0).getResults();
        assertEquals(1, list.size());
        assertEquals(customer, list.get(0));
        checkSelects(true, query, customer);
    }

    /**
     * Tests querying customers by patient id.
     */
    @Test
    public void testQueryByPatientId() {
        // create a customer and patient
        Party customer = createObject(true);
        Party patient = TestHelper.createPatient(customer);

        CustomerQuery query = (CustomerQuery) createQuery();
        query.setPatient(Long.toString(patient.getId()));
        ResultSet<Party> results = query.query();
        assertNotNull(results);
        List<Party> list = results.getPage(0).getResults();
        assertEquals(1, list.size());
        assertEquals(customer, list.get(0));
        checkSelects(true, query, customer);
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected Query<Party> createQuery() {
        return new CustomerQuery(SHORT_NAMES);
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected Party createObject(String value, boolean save) {
        return TestHelper.createCustomer("foo", value, save);
    }

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    protected String getUniqueValue() {
        return getUniqueValue("ZCustomer");
    }
}
