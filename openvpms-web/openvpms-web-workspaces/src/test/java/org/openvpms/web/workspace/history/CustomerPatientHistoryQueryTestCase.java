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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.workspace.history;

import org.apache.commons.collections.ComparatorUtils;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.app.SelectionHistory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link CustomerPatientHistoryQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerPatientHistoryQueryTestCase extends AbstractAppTest {

    /**
     * Tests the query.
     *
     * @throws Exception for any error
     */
    @Test
    public void testQuery() throws Exception {
        int count = 10;
        LocalContext context = new LocalContext();
        SelectionHistory patients = new SelectionHistory(context);
        SelectionHistory customers = new SelectionHistory(context);
        for (int i = 0; i < count; ++i) {
            Party patient = TestHelper.createPatient(true);
            patients.add(patient);

            Thread.sleep(100); // to try and guarantee the history is ordered on time meaningfully
            Party customer = TestHelper.createCustomer(true);
            customers.add(customer);

            if (i >= 5) {
                addOwnerRelationship(customer, patient); // add a patient-owner relationship
            }
        }

        // now create a query for the customer and patient selection history, and verify that it returns
        // CustomerPatient instances in the correct order.
        CustomerPatientHistoryQuery query = new CustomerPatientHistoryQuery(customers, patients, context);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        ResultSet<CustomerPatient> results = query.query();
        IPage<CustomerPatient> page = results.getPage(0);
        List<CustomerPatient> list = page.getResults();
        assertEquals(15, list.size());
        List<SelectionHistory.Selection> customerHistory = customers.getSelections();
        List<SelectionHistory.Selection> patientHistory = patients.getSelections();

        // verify the first 5 selections contain both a customer and patient (as these were the last selected,
        // and have a patient-owner relationship).
        for (int i = 0; i < 5; ++i) {
            checkCustomerPatient(list.get(i), customerHistory.get(i), patientHistory.get(i));
        }

        // verify the next 10 selections contain either a customer and patient, but not both (these have no
        // patient-owner relationship).
        for (int i = 5; i < list.size(); ) {
            for (int j = 5; j < 10; ++j) {
                checkCustomer(list.get(i), customerHistory.get(j));
                ++i;
                checkPatient(list.get(i), patientHistory.get(j));
                ++i;
            }
        }
    }

    /**
     * Tests sorting on customer and patient name.
     */
    @Test
    public void testSort() {
        Context context = new LocalContext();
        SelectionHistory patients = new SelectionHistory(context);
        SelectionHistory customers = new SelectionHistory(context);
        Party customer1 = TestHelper.createCustomer("", "XC", true);
        Party customer2 = TestHelper.createCustomer("", "XA", true);
        Party customer3 = TestHelper.createCustomer("", "XB", true);
        Party patient1 = TestHelper.createPatient();
        patient1.setName("XPatient-XB");
        Party patient2 = TestHelper.createPatient();
        patient2.setName("XPatient-XA");
        save(patient1, patient2);
        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);
        patients.add(patient1);
        patients.add(patient2);

        // now create a query for the customer and patient selection history, and verify that it returns
        // CustomerPatient instances in the correct order.
        CustomerPatientHistoryQuery query = new CustomerPatientHistoryQuery(customers, patients, context);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);

        // check ascending sort on customer. The nulls indicate no corresponding customer for a patient
        checkSortCustomer(query, true, null, null, customer2, customer3, customer1);

        // check ascending sort on patient
        checkSortPatient(query, true, null, null, null, patient2, patient1);

        // check descending sort on customer
        checkSortCustomer(query, false, customer1, customer3, customer2, null, null);

        // check descending sort on patient
        checkSortPatient(query, false, patient1, patient2, null, null, null);
    }

    /**
     * Tests filtering.
     *
     * @throws Exception for any error
     */
    @Test
    public void testFilter() throws Exception {
        Context context = new LocalContext();
        SelectionHistory patients = new SelectionHistory(context);
        SelectionHistory customers = new SelectionHistory(context);
        Party customer1 = TestHelper.createCustomer("", "XC", true);
        Party customer2 = TestHelper.createCustomer("", "XA", true);
        Party customer3 = TestHelper.createCustomer("", "XB", true);
        Party patient1 = TestHelper.createPatient();
        patient1.setName("XPatient-XA");
        Party patient2 = TestHelper.createPatient();
        patient2.setName("XPatient-XB");
        save(patient1, patient2);
        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);
        Thread.sleep(100); // to try and guarantee the history is ordered on time meaningfully
        patients.add(patient1);
        patients.add(patient2);

        // now create a query for the customer and patient selection history
        CustomerPatientHistoryQuery query = new CustomerPatientHistoryQuery(customers, patients, context);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);

        // filter results containing "XA" and verify customer2 and patient1 are returned
        query.setValue("XA");
        List<CustomerPatient> list = query.query().getPage(0).getResults();
        assertEquals(2, list.size());
        assertNull(list.get(0).getCustomer());
        assertEquals(patient1, list.get(0).getPatient());

        assertNull(list.get(1).getPatient());
        assertEquals(customer2, list.get(1).getCustomer());
    }

    /**
     * Verifies that a <tt>CustomerPatient</tt> instance contains both a customer and patient.
     *
     * @param pair     the customer/patient pair
     * @param customer the expected customer
     * @param patient  the expected patient
     */
    private void checkCustomerPatient(CustomerPatient pair, SelectionHistory.Selection customer,
                                      SelectionHistory.Selection patient) {
        assertEquals(customer.getObject(), pair.getCustomer());
        assertEquals(patient.getObject(), pair.getPatient());
        Date expected = (Date) ComparatorUtils.max(customer.getTime(), patient.getTime(), null);
        assertEquals(expected, pair.getSelected());
    }

    /**
     * Verifies that a <tt>CustomerPatient</tt> instance contains only a customer.
     *
     * @param pair     the customer/patient
     * @param customer the expected customer
     */
    private void checkCustomer(CustomerPatient pair, SelectionHistory.Selection customer) {
        assertEquals(customer.getObject(), pair.getCustomer());
        assertNull(pair.getPatient());
        assertEquals(customer.getTime(), pair.getSelected());
    }

    /**
     * Verifies that a <tt>CustomerPatient</tt> instance contains only a patient.
     *
     * @param pair    the customer/patient
     * @param patient the expected patient
     */
    private void checkPatient(CustomerPatient pair, SelectionHistory.Selection patient) {
        assertEquals(patient.getObject(), pair.getPatient());
        assertNull(pair.getCustomer());
        assertEquals(patient.getTime(), pair.getSelected());
    }

    /**
     * Sorts the query on customer, and verifies the results matches that expected.
     *
     * @param query     the query
     * @param ascending if <tt>true</tt> sort ascending, otherwise sort descending
     * @param expected  the expected results
     */
    private void checkSortCustomer(CustomerPatientHistoryQuery query, boolean ascending, Party... expected) {
        List<CustomerPatient> list = query(query, "customer", ascending);
        assertEquals(expected.length, list.size());
        for (int i = 0; i < list.size(); ++i) {
            assertEquals(expected[i], list.get(i).getCustomer());
        }
    }

    /**
     * Sorts the query on patient, and verifies the results matches that expected.
     *
     * @param query     the query
     * @param ascending if <tt>true</tt> sort ascending, otherwise sort descending
     * @param expected  the expected results
     */
    private void checkSortPatient(CustomerPatientHistoryQuery query, boolean ascending, Party... expected) {
        List<CustomerPatient> list = query(query, "patient", ascending);
        assertEquals(expected.length, list.size());
        for (int i = 0; i < list.size(); ++i) {
            assertEquals(expected[i], list.get(i).getPatient());
        }
    }

    /**
     * Executes the query, sorted on the specified node.
     *
     * @param query     the query
     * @param node      the sort node
     * @param ascending if <tt>true</tt> sort ascending, otherwise sort descending
     * @return the query results
     */
    private List<CustomerPatient> query(CustomerPatientHistoryQuery query, String node, boolean ascending) {
        SortConstraint[] sort = new SortConstraint[]{new NodeSortConstraint(node, ascending)};
        ResultSet<CustomerPatient> results = query.query(sort);
        IPage<CustomerPatient> page = results.getPage(0);
        return page.getResults();
    }

    /**
     * Helper to add a patient owner relationship between a customer and patient.
     *
     * @param customer the customer
     * @param patient  the patient
     */
    private void addOwnerRelationship(Party customer, Party patient) {
        EntityBean bean = new EntityBean(customer);
        bean.addRelationship("entityRelationship.patientOwner", patient);
        save(customer, patient);
    }

}
