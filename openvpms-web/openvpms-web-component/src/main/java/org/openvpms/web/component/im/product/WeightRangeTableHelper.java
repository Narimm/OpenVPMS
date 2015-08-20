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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;

/**
 * Helper to format a weight range.
 *
 * @author Tim Anderson
 */
class WeightRangeTableHelper {

    /**
     * Returns the weight range.
     *
     * @param object the object
     * @return the weight range, or {@code null}, if both the minimum and maximum weight are zero
     */
    public static String getWeightRange(IMObject object) {
        return getWeightRange(new IMObjectBean(object));
    }

    /**
     * Returns the weight range.
     *
     * @param bean the object
     * @return the weight range, or {@code null}, if both the minimum and maximum weight are zero
     */
    public static String getWeightRange(IMObjectBean bean) {
        BigDecimal min = bean.getBigDecimal("minWeight", BigDecimal.ZERO);
        BigDecimal max = bean.getBigDecimal("maxWeight", BigDecimal.ZERO);
        if (MathRules.isZero(min) && MathRules.isZero(max)) {
            return null;
        }
        String units = LookupNameHelper.getName(bean.getObject(), "weightUnits");
        return Messages.format("product.weightrange", min, max, units);
    }

}
