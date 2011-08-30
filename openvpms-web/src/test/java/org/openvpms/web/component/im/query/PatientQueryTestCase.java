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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.LocalContext;

import java.util.List;


/**
 * Tests the {@link PatientQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientQueryTestCase extends AbstractEntityQueryTest<Party> {

    /**
     * Patient archetype short names.
     */
    private static final String[] SHORT_NAMES = new String[]{PatientArchetypes.PATIENT};


    /**
     * Tests restricting patients to those owned by a customer.
     */
    @Test
    public void testQueryByCustomer() {
        Party customer = TestHelper.createCustomer(false);
        Party pet1 = TestHelper.createPatient(customer, false);
        Party pet2 = TestHelper.createPatient(customer, false);
        Party pet3 = createObject(false);
        save(customer, pet1, pet2, pet3);

        PatientQuery query = new PatientQuery(SHORT_NAMES, customer);

        List<IMObjectReference> matches = getObjectRefs(query);
        assertEquals(2, matches.size());
        checkExists(pet1, query, matches, true);
        checkExists(pet2, query, matches, true);
        checkExists(pet3, query, matches, false);
    }

    /**
     * Tests restricting patients to those owned by a customer, with a particular name.
     */
    @Test
    public void testQueryByCustomerAndName() {
        Party customer = TestHelper.createCustomer(false);
        Party pet1 = TestHelper.createPatient(customer, false);
        pet1.setName(getUniqueValue());
        Party pet2 = TestHelper.createPatient(customer, false);
        save(customer, pet1, pet2);

        PatientQuery query = new PatientQuery(SHORT_NAMES, customer);
        query.setValue(pet1.getName());

        List<IMObjectReference> matches = getObjectRefs(query);
        assertEquals(1, matches.size());
        checkExists(pet1, query, matches, true);
        checkExists(pet2, query, matches, false);
    }

    /**
     * Tests restricting patients to those owned by a customer, with a particular id.
     */
    @Test
    public void testQueryByCustomerAndId() {
        Party customer = TestHelper.createCustomer(false);
        Party pet1 = TestHelper.createPatient(customer, false);
        Party pet2 = TestHelper.createPatient(customer, false);
        save(customer, pet1, pet2);

        PatientQuery query = new PatientQuery(SHORT_NAMES, customer);
        query.setValue(Long.toString(pet2.getId()));

        List<IMObjectReference> matches = getObjectRefs(query);
        assertEquals(1, matches.size());
        checkExists(pet1, query, matches, false);
        checkExists(pet2, query, matches, true);
    }

    /**
     * Tests the behaviour of {@link PatientQuery#setQueryAllPatients}
     */
    @Test
    public void testQueryAllPatients() {
        Party customer = TestHelper.createCustomer(false);
        Party pet1 = TestHelper.createPatient(customer, false);
        Party pet2 = TestHelper.createPatient(customer, false);
        Party pet3 = createObject(false);
        save(customer, pet1, pet2, pet3);

        PatientQuery query = new PatientQuery(SHORT_NAMES, customer);

        // verify that when a customer is present, only its patients are returned
        List<IMObjectReference> matches = getObjectRefs(query);
        assertFalse(query.isQueryAllPatients());

        assertEquals(2, matches.size());
        checkExists(pet1, query, matches, true);
        checkExists(pet2, query, matches, true);
        checkExists(pet3, query, matches, false);

        // verify that when queryAllPatients=true. all patients are returned
        query.setQueryAllPatients(true);
        assertTrue(query.isQueryAllPatients());
        assertTrue(query.selects(pet1));
        assertTrue(query.selects(pet2));
        assertTrue(query.selects(pet3));

        // verify that when queryAllPatients=false, only the customer's patients are returned
        query.setQueryAllPatients(false);
        assertFalse(query.isQueryAllPatients());
        assertTrue(query.selects(pet1));
        assertTrue(query.selects(pet2));
        assertFalse(query.selects(pet3));
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected Query<Party> createQuery() {
        LocalContext context = new LocalContext(null);
        return new PatientQuery(SHORT_NAMES, context);
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected Party createObject(String value, boolean save) {
        Party pet = TestHelper.createPatient(false);
        pet.setName(value);
        if (save) {
            save(pet);
        }
        return pet;
    }

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    protected String getUniqueValue() {
        return getUniqueValue("ZPet");
    }
}
