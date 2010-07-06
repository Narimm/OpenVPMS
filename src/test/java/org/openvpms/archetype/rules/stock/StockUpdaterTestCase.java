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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.stock;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import static org.openvpms.archetype.rules.stock.StockArchetypes.*;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;


/**
 * Tests the {@link StockUpdater} class, when invoked by the
 * <em>archetypeService.save.act.stockTransfer.before</em> and
 * <em>archetypeService.save.act.stockAdjust.before</em>
 * rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockUpdaterTestCase extends AbstractStockTest {

    /**
     * The product.
     */
    private Product product;

    /**
     * The stock location.
     */
    private Party stockLocation;


    /**
     * Verifies that stock is updated when an <em>act.stockTransfer</em>
     * is posted.
     */
    @Test
    public void testTransfer() {
        BigDecimal quantity = new BigDecimal(100);
        Party xferLocation = createStockLocation();
        Act act = (Act) create(STOCK_TRANSFER);
        ActBean bean = new ActBean(act);
        bean.addParticipation(STOCK_LOCATION_PARTICIPATION, stockLocation);
        bean.addParticipation(STOCK_XFER_LOCATION_PARTICIPATION, xferLocation);
        Act item = (Act) create(STOCK_TRANSFER_ITEM);
        ActBean itemBean = new ActBean(item);
        bean.addRelationship(STOCK_TRANSFER_ITEM_RELATIONSHIP, item);
        itemBean.addParticipation(STOCK_PARTICIPATION, product);
        itemBean.setValue("quantity", quantity);
        save(act, item);

        // verify transfer doesn't take place till the act is posted
        checkEquals(BigDecimal.ZERO, getStock(stockLocation));
        checkEquals(BigDecimal.ZERO, getStock(xferLocation));

        // post the transfer
        bean.setValue("status", ActStatus.POSTED);
        bean.save();

        // verify stock at the from and to locations. Note that stock may
        // go negative
        checkEquals(quantity.negate(), getStock(stockLocation));
        checkEquals(quantity, getStock(xferLocation));

        // verify subsequent save doesn't change the stock
        bean.save();
        checkEquals(quantity.negate(), getStock(stockLocation));
        checkEquals(quantity, getStock(xferLocation));
    }

    /**
     * Verifies that stock is updated when an <em>act.stockAdjust</em>
     * is posted.
     */
    @Test
    public void testAdjust() {
        BigDecimal quantity = new BigDecimal(100);
        Act act = (Act) create(STOCK_ADJUST);
        ActBean bean = new ActBean(act);
        bean.addParticipation(STOCK_LOCATION_PARTICIPATION, stockLocation);
        Act item = (Act) create(STOCK_ADJUST_ITEM);
        ActBean itemBean = new ActBean(item);
        bean.addRelationship(STOCK_ADJUST_ITEM_RELATIONSHIP, item);
        itemBean.addParticipation(STOCK_PARTICIPATION, product);
        itemBean.setValue("quantity", quantity);

        save(act, item);

        // verify stock is not adjusted till the act is posted
        checkEquals(BigDecimal.ZERO, getStock(stockLocation));

        // post the act
        bean.setValue("status", ActStatus.POSTED);
        bean.save();

        // verify stock adjusted
        checkEquals(quantity, getStock(stockLocation));

        // verify subsequent save doesn't change the stock
        bean.save();
        checkEquals(quantity, getStock(stockLocation));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        product = TestHelper.createProduct();
        stockLocation = createStockLocation();
    }

    /**
     * Returns the stock in hand for the product and specified stock location.
     *
     * @param location the stock location
     * @return the stock in hand
     */
    private BigDecimal getStock(Party location) {
        product = get(product);
        EntityBean prodBean = new EntityBean(product);
        EntityRelationship rel = prodBean.getRelationship(location);
        if (rel != null) {
            IMObjectBean relBean = new IMObjectBean(rel);
            return relBean.getBigDecimal("quantity");
        }
        return BigDecimal.ZERO;
    }

}
