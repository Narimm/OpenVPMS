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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.util;

import org.joda.time.Period;
import org.openvpms.component.model.bean.IMObjectBean;

/**
 * Period helper methods.
 *
 * @author Tim Anderson
 */
public class PeriodHelper {

    /**
     * Returns a configured period.
     * <p/>
     * This assumes that the units node name is the period node name with "Unit" appended.
     *
     * @param bean the configuration
     * @param name the period node name
     * @return the period, or {@code null} if none is defined
     */
    public static Period getPeriod(IMObjectBean bean, String name) {
        return getPeriod(bean, name, name + "Units");
    }


    /**
     * Returns a configured period.
     *
     * @param bean       the configuration
     * @param periodName the period node name
     * @param unitsName  the period units node name
     * @return the period, or {@code null} if none is defined
     */
    public static Period getPeriod(IMObjectBean bean, String periodName, String unitsName) {
        Period result = null;
        int period = bean.getInt(periodName, -1);
        if (period > 0) {
            DateUnits units = DateUnits.fromString(bean.getString(unitsName), null);
            if (units != null) {
                result = units.toPeriod(period);
            }
        }
        return result;
    }

}
