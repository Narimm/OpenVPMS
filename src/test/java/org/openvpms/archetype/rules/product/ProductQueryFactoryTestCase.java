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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.product;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.QueryIterator;


/**
 * Tests the {@link ProductQueryFactory} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductQueryFactoryTestCase extends ArchetypeServiceTest {

    /**
     * Feline species.
     */
    private static final String FELINE = "FELINE";

    /**
     * Canine species.
     */
    private static final String CANINE = "CANINE";


    /**
     * Tests the {@link IArchetypeQuery} instances returned by
     * {@link ProductQueryFactory}.
     */
    @Test
    public void testCreate() {
        // test query on all canine products
        IArchetypeQuery canine = ProductQueryFactory.create(
                new String[]{"product.medication", "product.template",
                             "product.service", "product.merchandise"},
                null, CANINE, null);
        checkQuery(canine, true, false, true);

        // test query on all feline medication products
        IArchetypeQuery feline = ProductQueryFactory.create(
                new String[]{"product.medication"}, null, FELINE, null);
        checkQuery(feline, false, true, false);

        // test query on all canine products named XProduct*.
        IArchetypeQuery canine2 = ProductQueryFactory.create(
                new String[]{"product.medication", "product.template",
                             "product.service", "product.merchandise"},
                "XProduct*", CANINE, null);
        checkQuery(canine2, true, false, true);

        // test query on all feline medication products named XProduct*.
        IArchetypeQuery feline2 = ProductQueryFactory.create(
                new String[]{"product.medication"}, "XProduct*", FELINE, null);
        checkQuery(feline2, false, true, false);
    }

    /**
     * Checks a query against the following 3 products:
     * <ul>
     * <li>a canine <em>product.service</em></li>
     * <li>a feline <em>product.medication</em></li>
     * <li>a <em>product.merchandise</em</li>
     * </ul>
     *
     * @param query             the query
     * @param expectCanine      if <code>true</code> the query should select the
     *                          canine product.service
     * @param expectFeline      if <code>true</code> the query should select the
     *                          feline product.medication
     * @param expectMerchandise if <code>true</code> the query should select the
     *                          product.merchandise
     */
    private void checkQuery(IArchetypeQuery query, boolean expectCanine,
                            boolean expectFeline, boolean expectMerchandise) {
        IArchetypeService service = getArchetypeService();
        query.setCountResults(true);

        // get the current count of products
        int count = service.get(query).getTotalResults();

        // create new products for canines, felines and all species
        Product canineProduct = TestHelper.createProduct("product.service",
                                                         CANINE);
        Product felineProduct = TestHelper.createProduct("product.medication",
                                                         FELINE);
        Product merchProduct = TestHelper.createProduct("product.merchandise",
                                                        null);

        boolean canineFound = false;
        boolean felineFound = false;
        boolean merchandiseFound = false;

        int matches = 0;
        if (expectCanine) {
            ++matches;
        }
        if (expectFeline) {
            ++matches;
        }
        if (expectMerchandise) {
            ++matches;
        }
        int newCount = service.get(query).getTotalResults();
        assertEquals(count + matches, newCount);

        // verify that the expected products are in the results
        QueryIterator<Product> iterator
                = new IMObjectQueryIterator<Product>(query);
        while (iterator.hasNext()) {
            Product product = iterator.next();
            if (product.equals(canineProduct)) {
                canineFound = true;
            } else if (product.equals(felineProduct)) {
                felineFound = true;
            } else if (product.equals(merchProduct)) {
                merchandiseFound = true;
            }
        }

        assertEquals(expectCanine, canineFound);
        assertEquals(expectFeline, felineFound);
        assertEquals(expectMerchandise, merchandiseFound);
    }

}
