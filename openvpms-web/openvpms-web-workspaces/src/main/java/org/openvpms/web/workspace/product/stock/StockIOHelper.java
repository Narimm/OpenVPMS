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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.system.ServiceHelper;

/**
 * Stock I/O helper methods.
 *
 * @author Tim Anderson
 */
public class StockIOHelper {

    /**
     * Returns the export file field separator.
     *
     * @param practice the practice configuration. May be {@code null}
     * @return the separator
     */
    public static char getFieldSeparator(Party practice) {
        if (practice != null) {
            PracticeRules rules = ServiceHelper.getBean(PracticeRules.class);
            return rules.getExportFileFieldSeparator(practice);
        }
        return ',';
    }

}
