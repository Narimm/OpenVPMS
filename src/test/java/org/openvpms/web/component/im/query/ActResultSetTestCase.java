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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * {@link ActResultSet} test case.
 *
 * @author Tim Anderson
 */
public class ActResultSetTestCase extends AbstractResultSetTest {

    /**
     * The acts created during {@link #setUp}.
     */
    private Act[] acts;

    /**
     * The customer.
     */
    private Party customer;


    /**
     * Tests behaviour of iterating using an empty result set.
     */
    @Test
    public void testEmpty() {
        Party party = TestHelper.createCustomer(true);

        ShortNameConstraint archetypes = new ShortNameConstraint(
                "act.customerEstimation", true, true);
        Date from = null;       // query all dates
        Date to = null;
        String[] statuses = {}; // query all statuses
        int rowsPerPage = 20;
        SortConstraint[] sort = null;
        ParticipantConstraint participant = new ParticipantConstraint(
                "customer", "participation.customer",
                party.getObjectReference());
        ActResultSet set = new ActResultSet(archetypes, participant, from, to,
                                            statuses, rowsPerPage, sort);

        assertFalse(set.hasNext());
        assertEquals(0, set.getPages());
        assertEquals(0, set.getResults());
        assertNull(set.getPage(0));
        assertNull(set.getPage(1));
        assertEquals(rowsPerPage, set.getPageSize());
        assertTrue(set.isSortedAscending());
        assertTrue(set.getSortConstraints() != null && set.getSortConstraints().length == 0);
    }

    /**
     * Tests iteration.
     */
    @Test
    public void testIteration() {
        int rowsPerPage = 5;
        int total = acts.length;
        int expectedPages = (total / rowsPerPage);
        if (total % rowsPerPage > 0) {
            ++expectedPages;
        }
        checkIteration(expectedPages, rowsPerPage, total);
    }

    /**
     * Verifies that iteration works when all results are returned.
     */
    @Test
    public void testIterateAll() {
        final int rowsPerPage = ArchetypeQuery.ALL_RESULTS;
        checkIteration(1, rowsPerPage, acts.length);
    }

    /**
     * Tests random access.
     */
    @Test
    public void testRandomAccess() {
        final int rowsPerPage = 2;
        final int total = acts.length;
        int expectedPages = getPages(rowsPerPage, total);

        ShortNameConstraint archetypes = new ShortNameConstraint(
                "act.customerEstimation", true, true);
        Date from = null;       // query all dates
        Date to = null;
        String[] statuses = {}; // query all statuses
        SortConstraint[] sort = null;
        ParticipantConstraint participant = new ParticipantConstraint(
                "customer", "participation.customer",
                customer.getObjectReference());
        ResultSet<Act> set = new ActResultSet<Act>(archetypes, participant,
                                                   from, to, statuses,
                                                   rowsPerPage, sort);

        for (int j = 0; j < 2; ++j) {
            for (int i = 0; i < expectedPages; ++i) {
                IPage<Act> page = set.getPage(i);
                checkPage(set, page, i, rowsPerPage, total);
            }
        }
    }

    /**
     * Verifies that non-primary archetypes can be specified.
     */
    @Test
    public void testNonPrimary() {
        Party patient = TestHelper.createPatient(true);
        String[] shortNames = {"act.customerAccountInvoiceItem",
                "act.customerAccountCreditItem"};
        ShortNameConstraint archetypes = new ShortNameConstraint(shortNames,
                                                                 false, true);
        ParticipantConstraint participant = new ParticipantConstraint(
                "patient", "participation.patient",
                patient.getObjectReference());
        Date from = null;       // query all dates
        Date to = null;
        String[] statuses = {}; // query all statuses
        int rowsPerPage = 20;
        SortConstraint[] sort = null;
        ActResultSet set = new ActResultSet(archetypes, participant, from, to,
                                            statuses, rowsPerPage, sort);
        assertFalse(set.hasNext());
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();

        customer = TestHelper.createCustomer(true);
        Party patient = TestHelper.createPatient(true);
        Product product = TestHelper.createProduct();

        final int count = 12;
        acts = new Act[count];
        for (int i = 0; i < count; ++i) {
            acts[i] = createEstimation(customer, patient, product);
        }
    }

    /**
     * Verifies the set and page match that expected.
     *
     * @param set         the result set
     * @param page        the page
     * @param pageIndex   the current page index
     * @param rowsPerPage the no. of rows per page
     * @param total       the total no. of rows
     */
    protected void checkPage(ResultSet set, IPage<Act> page,
                             int pageIndex, int rowsPerPage, int total) {
        assertNotNull(page);
        assertNotNull(page.getResults());
        int expected = getRows(pageIndex, rowsPerPage, total);
        assertEquals(expected, page.getResults().size());

        int pages = getPages(rowsPerPage, total);
        assertEquals(pages, set.getPages());
        assertEquals(total, set.getResults());
    }

    /**
     * Helper to return the expected no. of pages.
     *
     * @param rowsPerPage the no. of rows per page
     * @param total       the total no. of rows
     * @return the no. of pages
     */
    protected int getPages(int rowsPerPage, int total) {
        int result = (total / rowsPerPage);
        if (total % rowsPerPage > 0) {
            result++;
        }
        return result;
    }

    /**
     * Helper to return the expected no. of rows for a page.
     *
     * @param page        the current page index
     * @param rowsPerPage the no. of rows per page
     * @param total       the total no. of rows
     * @return the no. of rows
     */
    protected int getRows(int page, int rowsPerPage, int total) {
        int count;
        int pages = total / rowsPerPage;
        boolean remainder = false;
        if (total % rowsPerPage > 0) {
            ++pages;
            remainder = true;
        }
        if (page == pages - 1) {
            count = (remainder) ? total % rowsPerPage : rowsPerPage;
        } else {
            count = rowsPerPage;
        }
        return count;
    }

    /**
     * Tests iteration.
     *
     * @param expectedPages the expected no. of pages
     * @param rowsPerPage   the no. of rows per page
     * @param total         the total no. of rows
     */
    private void checkIteration(int expectedPages, int rowsPerPage, int total) {
        ShortNameConstraint archetypes = new ShortNameConstraint(
                "act.customerEstimation", true, true);
        Date from = null;       // query all dates
        Date to = null;
        String[] statuses = {}; // query all statuses
        SortConstraint[] sort = null;
        ParticipantConstraint participant = new ParticipantConstraint(
                "customer", "participation.customer",
                customer.getObjectReference());
        ActResultSet<Act> set = new ActResultSet<Act>(archetypes, participant,
                                                      from, to, statuses,
                                                      rowsPerPage, sort);

        assertFalse(set.hasPrevious());
        int rows = (rowsPerPage == ArchetypeQuery.ALL_RESULTS) ? total : rowsPerPage;
        for (int i = 0; i < expectedPages; ++i) {
            assertTrue(set.hasNext());
            IPage<Act> page = set.next();
            checkPage(set, page, i, rows, total);
        }
        assertFalse(set.hasNext());

        for (int i = expectedPages - 1; i >= 0; --i) {
            assertTrue(set.hasPrevious());
            IPage<Act> page = set.previous();
            checkPage(set, page, i, rows, total);
        }
        assertFalse(set.hasPrevious());
    }

}
