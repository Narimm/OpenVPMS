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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.wip;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.QueryTestHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link IncompleteChargesQuery}.
 *
 * @author Tim Anderson
 */
public class IncompleteChargesQueryTestCase extends AbstractAppTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The product.
     */
    private Product product;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        product = TestHelper.createProduct();
    }

    /**
     * Verifies that if there are no charges for a user's location, an empty result set is returned.
     */
    @Test
    public void testEmptyQuery() {
        Context context = new LocalContext();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party location3 = TestHelper.createLocation();
        User user = TestHelper.createUser();
        EntityBean bean = new EntityBean(user);

        bean.addNodeRelationship("locations", location1);
        context.setUser(user);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        IncompleteChargesQuery query = new IncompleteChargesQuery(layout);
        query.setLocation(location1);
        QueryTestHelper.checkEmpty(query);

        Act invoice = createInvoice(location2, ActStatus.IN_PROGRESS);
        Act counter = createCounter(location2, ActStatus.IN_PROGRESS);
        Act credit = createCredit(location3, ActStatus.IN_PROGRESS);

        save(invoice);
        save(counter);
        save(credit);
        QueryTestHelper.checkEmpty(query);
    }

    /**
     * Tests querying charges by practice location.
     */
    @Test
    public void testQueryByLocation() {
        Context context = new LocalContext();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party location3 = TestHelper.createLocation();
        User user = TestHelper.createUser();
        EntityBean bean = new EntityBean(user);

        bean.addNodeRelationship("locations", location1);
        bean.addNodeRelationship("locations", location2);
        context.setUser(user);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        IncompleteChargesQuery query = new IncompleteChargesQuery(layout);

        query.setLocation(null);  // all locations
        checkNoLocationInvoices(query, IncompleteChargesQuery.SHORT_NAMES);

        Act invoice = createInvoice(location1, ActStatus.IN_PROGRESS);
        Act counter = createCounter(location2, ActStatus.IN_PROGRESS);
        Act credit = createCredit(location3, ActStatus.IN_PROGRESS);

        List<IMObjectReference> refs = QueryTestHelper.getObjectRefs(query);
        assertTrue(refs.contains(invoice.getObjectReference()));
        assertTrue(refs.contains(counter.getObjectReference()));
        assertFalse(refs.contains(credit.getObjectReference()));

        query.setLocation(location1);
        refs = QueryTestHelper.getObjectRefs(query);
        assertTrue(refs.contains(invoice.getObjectReference()));
        assertFalse(refs.contains(counter.getObjectReference()));
        assertFalse(refs.contains(credit.getObjectReference()));
    }

    /**
     * Verifies that the user inherits the practice's locations.
     */
    @Test
    public void testQueryByLocationForUserWithNoLocations() {
        Context context = new LocalContext();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party location3 = TestHelper.createLocation();
        User user = TestHelper.createUser();
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);

        EntityBean bean = new EntityBean(practice);
        bean.addNodeRelationship("locations", location1);
        bean.addNodeRelationship("locations", location2);
        context.setUser(user);
        context.setPractice(practice);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        IncompleteChargesQuery query = new IncompleteChargesQuery(layout);

        query.setLocation(null);  // all locations
        checkNoLocationInvoices(query, IncompleteChargesQuery.SHORT_NAMES);

        Act invoice = createInvoice(location1, ActStatus.IN_PROGRESS);
        Act counter = createCounter(location2, ActStatus.IN_PROGRESS);
        Act credit = createCredit(location3, ActStatus.IN_PROGRESS);

        List<IMObjectReference> refs = QueryTestHelper.getObjectRefs(query);
        assertTrue(refs.contains(invoice.getObjectReference()));
        assertTrue(refs.contains(counter.getObjectReference()));
        assertFalse(refs.contains(credit.getObjectReference()));

        query.setLocation(location1);
        refs = QueryTestHelper.getObjectRefs(query);
        assertTrue(refs.contains(invoice.getObjectReference()));
        assertFalse(refs.contains(counter.getObjectReference()));
        assertFalse(refs.contains(credit.getObjectReference()));
    }

    /**
     * Tests querying charges by status.
     */
    @Test
    public void testQueryByStatus() {
        Context context = new LocalContext();
        Party location1 = TestHelper.createLocation();
        User user = TestHelper.createUser();
        EntityBean bean = new EntityBean(user);

        bean.addNodeRelationship("locations", location1);
        context.setUser(user);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        IncompleteChargesQuery query = new IncompleteChargesQuery(layout);

        Act inProgress = createInvoice(location1, ActStatus.IN_PROGRESS);
        Act completed = createInvoice(location1, ActStatus.COMPLETED);
        Act onHold = createInvoice(location1, FinancialActStatus.ON_HOLD);
        Act posted = createInvoice(location1, FinancialActStatus.POSTED);

        List<IMObjectReference> refs = QueryTestHelper.getObjectRefs(query);
        assertTrue(refs.contains(inProgress.getObjectReference()));
        assertTrue(refs.contains(completed.getObjectReference()));
        assertTrue(refs.contains(onHold.getObjectReference()));
        assertFalse(refs.contains(posted.getObjectReference()));

        query.setStatus(ActStatus.IN_PROGRESS);
        refs = QueryTestHelper.getObjectRefs(query);
        assertTrue(refs.contains(inProgress.getObjectReference()));
        assertFalse(refs.contains(completed.getObjectReference()));
        assertFalse(refs.contains(onHold.getObjectReference()));
        assertFalse(refs.contains(posted.getObjectReference()));
    }

    /**
     * Verifies that only charges with no location are returned by the query.
     *
     * @param query      the query
     * @param shortNames the expected short names
     */
    private void checkNoLocationInvoices(IncompleteChargesQuery query, String... shortNames) {
        ResultSet<Act> set = query.query();
        ResultSetIterator<Act> iterator = new ResultSetIterator<Act>(set);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            ActBean bean = new ActBean(act);
            assertTrue(bean.isA(shortNames));
            assertNull(bean.getNodeParticipantRef("location"));
        }
    }

    /**
     * Creates an invoice.
     *
     * @param location the practice location
     * @param status   the invoice status
     * @return the invoice
     */
    private Act createInvoice(Party location, String status) {
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(Money.TEN, customer, patient, product,
                                                                              status);
        addLocation(invoice, location);
        save(invoice);
        return invoice.get(0);
    }

    /**
     * Creates a credit.
     *
     * @param location the practice location
     * @param status   the credit status
     * @return the credit
     */
    private Act createCredit(Party location, String status) {
        List<FinancialAct> credit = FinancialTestHelper.createChargesCredit(Money.TEN, customer, patient, product,
                                                                            status);
        addLocation(credit, location);
        save(credit);
        return credit.get(0);
    }

    /**
     * Creates a counter sale.
     *
     * @param location the practice location
     * @param status   the counter sale status
     * @return the counter sale
     */
    private Act createCounter(Party location, String status) {
        List<FinancialAct> counter = FinancialTestHelper.createChargesCounter(Money.TEN, customer, product, status);
        addLocation(counter, location);
        save(counter);
        return counter.get(0);
    }

    /**
     * Adds a location to a charge.
     *
     * @param charge   the charge acts
     * @param location the location
     */
    private void addLocation(List<FinancialAct> charge, Party location) {
        ActBean bean = new ActBean(charge.get(0));
        bean.addNodeParticipation("location", location);
    }
}
