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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.stock;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Tests the <em>act.stockAdjust</em> and <em>act.stockTransfer</em> archetypes.
 *
 * @author Tim Anderson
 */
public class StockActTestCase extends AbstractStockTest {

    /**
     * Verifies that when a stock adjustment is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteStockAdjust() {
        checkDeleteActs(StockArchetypes.STOCK_ADJUST, StockArchetypes.STOCK_ADJUST_ITEM);
    }

    /**
     * Verifies that when a stock transfer is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteStockTransfer() {
        List<Act> acts = createActs(StockArchetypes.STOCK_TRANSFER, StockArchetypes.STOCK_TRANSFER_ITEM);
        ActBean bean = new ActBean(acts.get(0));
        bean.addNodeParticipation("to", createStockLocation());
        checkDeleteActs(acts);
    }

    /**
     * Verifies that when a parent act is deleted, its child acts are also deleted.
     *
     * @param shortName     the parent act short name
     * @param itemShortName the child act short name
     */
    private void checkDeleteActs(String shortName, String itemShortName) {
        List<Act> acts = createActs(shortName, itemShortName);
        checkDeleteActs(acts);
    }

    /**
     * Verifies that when a parent act is deleted, its child acts are also deleted.
     *
     * @param acts the parent and child acts
     */
    private void checkDeleteActs(List<Act> acts) {
        save(acts);

        Act parent = acts.get(0);
        Act child = acts.get(1);
        assertNotNull(get(parent));
        assertNotNull(get(child));

        remove(parent);
        assertNull(get(parent));
        assertNull(get(child));
    }

    /**
     * Helper to create a stock act with one child item.
     *
     * @param shortName     the stock act archetype short name
     * @param itemShortName the child act item archetype short name
     * @return the parent and child act'
     */
    private List<Act> createActs(String shortName, String itemShortName) {
        Act act = (Act) create(shortName);
        Act item = (Act) create(itemShortName);
        ActBean itemBean = new ActBean(item);
        itemBean.addParticipation(StockArchetypes.STOCK_PARTICIPATION, TestHelper.createProduct());
        getArchetypeService().deriveValues(item);

        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("stockLocation", createStockLocation());
        bean.addNodeRelationship("items", item);
        List<Act> acts = new ArrayList<Act>();
        acts.add(act);
        acts.add(item);
        return acts;
    }

}