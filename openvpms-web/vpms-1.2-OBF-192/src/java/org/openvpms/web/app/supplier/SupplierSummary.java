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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.RowFactory;

import java.math.BigDecimal;


/**
 * Renders supplier summary information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SupplierSummary {

    /**
     * Returns summary information for a supplier.
     *
     * @param supplier the supplier. May be <code>null</code>
     * @return a summary component, or <code>null</code> if there is no summary
     */
    public static Component getSummary(Party supplier) {
        Component result = null;
        if (supplier != null) {
            Label title = LabelFactory.create("supplier.account.balance");
            Label balance = LabelFactory.create();
            BigDecimal value = ActHelper.getSupplierAccountBalance(supplier);
            balance.setText(NumberFormatter.format(value));
            result = RowFactory.create("CellSpacing", title, balance);
        }
        return result;
    }

}
