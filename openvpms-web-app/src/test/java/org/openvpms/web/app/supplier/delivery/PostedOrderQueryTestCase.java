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

package org.openvpms.web.app.supplier.delivery;

import org.junit.Test;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.archetype.rules.supplier.SupplierTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.List;

import static org.openvpms.web.component.im.query.QueryTestHelper.checkEmpty;
import static org.openvpms.web.component.im.query.QueryTestHelper.checkExists;


/**
 * Tests the {@link PostedOrderQuery} class.
 *
 * @author Tim Anderson
 */
public class PostedOrderQueryTestCase extends AbstractAppTest {

    /**
     * Verifies that the query only returns POSTED and ACCEPTED orders for a specific supplier and stock location.
     */
    @Test
    public void testQuery() {
        Party supplier1 = TestHelper.createSupplier();
        Party supplier2 = TestHelper.createSupplier();
        Party stockLocation = SupplierTestHelper.createStockLocation();

        FinancialAct inProgress = createOrder(supplier1, stockLocation, OrderStatus.IN_PROGRESS);
        FinancialAct completed = createOrder(supplier1, stockLocation, OrderStatus.COMPLETED);
        FinancialAct cancelled = createOrder(supplier1, stockLocation, OrderStatus.CANCELLED);
        FinancialAct posted1 = createOrder(supplier1, stockLocation, OrderStatus.POSTED);
        FinancialAct posted2 = createOrder(supplier2, stockLocation, OrderStatus.POSTED);
        FinancialAct accepted = createOrder(supplier1, stockLocation, OrderStatus.ACCEPTED);
        FinancialAct rejected = createOrder(supplier1, stockLocation, OrderStatus.REJECTED);

        PostedOrderQuery query = new PostedOrderQuery(true, new DefaultLayoutContext(new LocalContext(),
                                                                                     new HelpContext("foo", null)));
        checkEmpty(query);
        checkExists(inProgress, query, false);
        checkExists(completed, query, false);
        checkExists(cancelled, query, false);
        checkExists(posted1, query, false);
        checkExists(posted2, query, false);
        checkExists(accepted, query, false);
        checkExists(rejected, query, false);

        query.setSupplier(supplier1);
        query.setStockLocation(stockLocation);

        checkExists(inProgress, query, false);
        checkExists(completed, query, false);
        checkExists(cancelled, query, false);
        checkExists(posted1, query, true);
        checkExists(accepted, query, true);
        checkExists(rejected, query, false);
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected Query<FinancialAct> createQuery() {
        return new PostedOrderQuery(true, null);
    }

    /**
     * Creates a new order.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @param status        the order status
     * @return the new order
     */
    protected FinancialAct createOrder(Party supplier, Party stockLocation, String status) {
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = SupplierTestHelper.createOrder(BigDecimal.ONE, supplier, stockLocation, product);
        FinancialAct order = acts.get(0);
        order.setStatus(status);
        save(acts);
        return order;
    }
}
