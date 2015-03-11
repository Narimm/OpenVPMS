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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Base class for estimate tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEstimateEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Verifies an item's properties match that expected.
     *
     * @param items         the items to check
     * @param patient       the expected patient
     * @param product       the expected product
     * @param author        the expected author
     * @param lowQuantity   the expected low quantity
     * @param highQuantity  the expected high quantity
     * @param lowUnitPrice  the expected low unit price
     * @param highUnitPrice the expected high unit price
     * @param fixedPrice    the expected fixed price
     * @param lowDiscount   the expected low discount
     * @param highDiscount  the expected high discount
     * @param lowTotal      the expected low total
     * @param highTotal     the expected high total
     */
    protected void checkItem(List<Act> items, Party patient, Product product, User author, int lowQuantity,
                             int highQuantity, BigDecimal lowUnitPrice, BigDecimal highUnitPrice,
                             BigDecimal fixedPrice, BigDecimal lowDiscount, BigDecimal highDiscount,
                             BigDecimal lowTotal, BigDecimal highTotal) {
        Act item = find(items, product);
        checkItem(item, patient, product, author, BigDecimal.valueOf(lowQuantity), BigDecimal.valueOf(highQuantity),
                  lowUnitPrice, highUnitPrice, fixedPrice, lowDiscount, highDiscount, lowTotal, highTotal);
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param items         the items to check
     * @param patient       the expected patient
     * @param product       the expected product
     * @param author        the expected author
     * @param lowQuantity   the expected low quantity
     * @param highQuantity  the expected high quantity
     * @param lowUnitPrice  the expected low unit price
     * @param highUnitPrice the expected high unit price
     * @param fixedPrice    the expected fixed price
     * @param lowDiscount   the expected low discount
     * @param highDiscount  the expected high discount
     * @param lowTotal      the expected low total
     * @param highTotal     the expected high total
     */
    protected void checkItem(List<Act> items, Party patient, Product product, User author, BigDecimal lowQuantity,
                             BigDecimal highQuantity, BigDecimal lowUnitPrice, BigDecimal highUnitPrice,
                             BigDecimal fixedPrice, BigDecimal lowDiscount, BigDecimal highDiscount,
                             BigDecimal lowTotal, BigDecimal highTotal) {
        Act item = find(items, product);
        checkItem(item, patient, product, author, lowQuantity, highQuantity, lowUnitPrice, highUnitPrice, fixedPrice,
                  lowDiscount, highDiscount, lowTotal, highTotal);
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item          the item to check
     * @param patient       the expected patient
     * @param product       the expected product
     * @param author        the expected author
     * @param lowQuantity   the expected low quantity
     * @param highQuantity  the expected high quantity
     * @param lowUnitPrice  the expected low unit price
     * @param highUnitPrice the expected high unit price
     * @param fixedPrice    the expected fixed price
     * @param lowDiscount   the expected low discount
     * @param highDiscount  the expected high discount
     * @param lowTotal      the expected low total
     * @param highTotal     the expected high total
     */
    protected void checkItem(Act item, Party patient, Product product, User author, BigDecimal lowQuantity,
                             BigDecimal highQuantity, BigDecimal lowUnitPrice, BigDecimal highUnitPrice,
                             BigDecimal fixedPrice, BigDecimal lowDiscount, BigDecimal highDiscount,
                             BigDecimal lowTotal, BigDecimal highTotal) {
        ActBean bean = new ActBean(item);
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        checkEquals(lowQuantity, bean.getBigDecimal("lowQty"));
        checkEquals(highQuantity, bean.getBigDecimal("highQty"));
        checkEquals(fixedPrice, bean.getBigDecimal("fixedPrice"));
        checkEquals(lowUnitPrice, bean.getBigDecimal("lowUnitPrice"));
        checkEquals(highUnitPrice, bean.getBigDecimal("highUnitPrice"));
        checkEquals(lowDiscount, bean.getBigDecimal("lowDiscount"));
        checkEquals(highDiscount, bean.getBigDecimal("highDiscount"));
        checkEquals(lowTotal, bean.getBigDecimal("lowTotal"));
        checkEquals(highTotal, bean.getBigDecimal("highTotal"));
    }
}
