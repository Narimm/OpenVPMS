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

package org.openvpms.archetype.rules.finance.discount;

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Discount test helper methods.
 *
 * @author Tim Anderson
 */
public class DiscountTestHelper {

    /**
     * Helper to create and save a new discount type entity.
     *
     * @param rate          the discount rate
     * @param fixedDiscount determines if the discount applies to the fixed price. If {@code false} it only applies to
     *                      the unit price
     * @param type          the discount type
     * @return a new discount
     */
    public static Entity createDiscount(BigDecimal rate, boolean fixedDiscount, String type) {
        Entity discount = (Entity) TestHelper.create("entity.discountType");
        IMObjectBean bean = new IMObjectBean(discount);
        bean.setValue("name", "XDiscount-" + Math.abs(new Random().nextInt()));
        bean.setValue("rate", rate);
        bean.setValue("discountFixed", fixedDiscount);
        bean.setValue("type", type);
        bean.save();
        return discount;
    }
}
