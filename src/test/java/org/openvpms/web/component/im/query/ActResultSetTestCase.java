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
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.test.TestHelper;

import java.util.Date;


/**
 * {@link ActResultSet} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActResultSetTestCase extends AbstractAppTest {

    /**
     * The customer.
     */
    private Party _customer;

    /**
     * The acts created during {@link #onSetUp}.
     */
    private Act[] _acts;


    /**
     * Tests behaviour of iterating using an empty result set.
     */
    public void testEmpty() {
        Party party = TestHelper.createCustomer();
        SaveHelper.save(party);

        BaseArchetypeConstraint archetypes = new ShortNameConstraint(
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
        try {
            set.getPages();
            fail("Expected getPages() to throw IllegalStateException");
        } catch (IllegalStateException expected) {
            // no-op
        }
        assertNull(set.getPage(0));
        assertNull(set.getPage(1));
        assertEquals(rowsPerPage, set.getPageSize());

        try {
            set.getResults();
            fail("Expected getResults() to throw IllegalStateException");
        } catch (IllegalStateException expected) {
            // no-op
        }

        assertTrue(set.isSortedAscending());
        assertTrue(set.getSortConstraints() != null
                && set.getSortConstraints().length == 0);
    }

    /**
     * Tests iteration.
     */
    public void testIteration() {
        final int rowsPerPage = 5;
        final int total = _acts.length;
        int expectedPages = (total / rowsPerPage);
        if (total % rowsPerPage > 0) {
            ++expectedPages;
        }

        BaseArchetypeConstraint archetypes = new ShortNameConstraint(
                "act.customerEstimation", true, true);
        Date from = null;       // query all dates
        Date to = null;
        String[] statuses = {}; // query all statuses
        SortConstraint[] sort = null;
        ParticipantConstraint participant = new ParticipantConstraint(
                "customer", "participation.customer",
                _customer.getObjectReference());
        ActResultSet<Act> set = new ActResultSet<Act>(archetypes, participant,
                                                      from, to, statuses,
                                                      rowsPerPage, sort);

        assertFalse(set.hasPrevious());
        for (int i = 0; i < expectedPages; ++i) {
            assertTrue(set.hasNext());
            IPage<Act> page = set.next();
            checkPage(set, page, i, rowsPerPage, total);
        }
        assertFalse(set.hasNext());

        for (int i = expectedPages - 1; i >= 0; --i) {
            assertTrue(set.hasPrevious());
            IPage<Act> page = set.previous();
            checkPage(set, page, i, rowsPerPage, total);
        }
        assertFalse(set.hasPrevious());
    }

    /**
     * Tests random access.
     */
    public void testRandomAccess() {
        final int rowsPerPage = 2;
        final int total = _acts.length;
        int expectedPages = getPages(rowsPerPage, total);

        BaseArchetypeConstraint archetypes = new ShortNameConstraint(
                "act.customerEstimation", true, true);
        Date from = null;       // query all dates
        Date to = null;
        String[] statuses = {}; // query all statuses
        SortConstraint[] sort = null;
        ParticipantConstraint participant = new ParticipantConstraint(
                "customer", "participation.customer",
                _customer.getObjectReference());
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
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        _customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();
        SaveHelper.save(_customer);
        SaveHelper.save(patient);
        SaveHelper.save(product);

        final int count = 12;

        _acts = new Act[count];
        for (int i = 0; i < count; ++i) {
            _acts[i] = createEstimation(_customer, patient, product);
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
     * Helper to create a new <code>act.customerEstimation</code>, and save it.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param product  the product
     * @return a new act
     */
    protected Act createEstimation(Party customer, Party patient,
                                   Product product) {
        Act act = createAct("act.customerEstimation");
        act.setStatus(FinancialActStatus.IN_PROGRESS);
        addParticipation(act, customer, "participation.customer");

        Act child = createAct("act.customerEstimationItem");
        child.setStatus(FinancialActStatus.IN_PROGRESS);

        addParticipation(child, patient, "participation.patient");
        addParticipation(child, product, "participation.product");
        addActRelationship(act, child,
                           "actRelationship.customerEstimationItem");
        assertTrue(SaveHelper.save(act));
        assertTrue(SaveHelper.save(child));
        return act;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected Act createAct(String shortName) {
        Act act = (Act) TestHelper.create(shortName);
        assertNotNull(act);
        return act;
    }

    /**
     * Adds a participation.
     *
     * @param act           the act to add to
     * @param entity        the participation entity
     * @param participation the participation short name
     */
    protected void addParticipation(Act act, Entity entity,
                                    String participation) {
        Participation p = (Participation) TestHelper.create(participation);
        assertNotNull(p);
        p.setAct(act.getObjectReference());
        p.setEntity(entity.getObjectReference());
        act.addParticipation(p);
    }

    /**
     * Helper to add a relationship between two acts.
     *
     * @param source    the source act
     * @param target    the target act
     * @param shortName the act relationship short name
     */
    protected void addActRelationship(Act source, Act target,
                                      String shortName) {
        ActRelationship relationship
                = (ActRelationship) TestHelper.create(shortName);
        assertNotNull(relationship);
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addActRelationship(relationship);
        target.addActRelationship(relationship);
    }

}
