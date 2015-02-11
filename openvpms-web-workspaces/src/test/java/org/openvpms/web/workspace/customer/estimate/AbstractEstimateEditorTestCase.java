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
public class AbstractEstimateEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Verifies an item's properties match that expected.
     *
     * @param items      the items to check
     * @param patient    the expected patient
     * @param product    the expected product
     * @param author     the expected author
     * @param quantity   the expected quantity
     * @param unitPrice  the expected unit price
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param total      the expected total
     */
    protected void checkItem(List<Act> items, Party patient, Product product, User author, BigDecimal quantity,
                             BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal discount, BigDecimal total) {
        Act item = find(items, product);
        checkItem(item, patient, product, author, quantity, unitPrice, fixedPrice, discount, total);
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item       the item to check
     * @param patient    the expected patient
     * @param product    the expected product
     * @param author     the expected author
     * @param quantity   the expected quantity
     * @param unitPrice  the expected unit price
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param total      the expected total
     */
    protected void checkItem(Act item, Party patient, Product product, User author, BigDecimal quantity,
                             BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal discount, BigDecimal total) {
        ActBean bean = new ActBean(item);
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        checkEquals(quantity, bean.getBigDecimal("lowQty"));
        checkEquals(quantity, bean.getBigDecimal("highQty"));
        checkEquals(fixedPrice, bean.getBigDecimal("fixedPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("lowUnitPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("highUnitPrice"));
        checkEquals(discount, bean.getBigDecimal("discount"));
        checkEquals(total, bean.getBigDecimal("lowTotal"));
        checkEquals(total, bean.getBigDecimal("highTotal"));
    }
}
