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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer;

import java.math.BigDecimal;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeConstraint;
import org.openvpms.component.system.common.query.ArchetypeLongNameConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.RowFactory;


/**
 * Renders customer summary information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CustomerSummary {


    /**
     * Returns summary information for a customer.
     *
     * @param customer the customer. May be <code>null</code>
     * @return a summary component, or <code>null</code> if there is no summary
     */
    public static Component getSummary(Party customer) {
        Component result = null;
        if (customer != null) {
            Label title = LabelFactory.create("customer.account.balance");
            Label balance = LabelFactory.create();
            BigDecimal value = getBalance(customer);
            balance.setText(NumberFormatter.format(value));
            result = RowFactory.create("CellSpacing", title, balance);
        }
        return result;
    }

    private static BigDecimal getBalance(Party customer) {
        String[] statuses = {"Posted"};
        ArchetypeConstraint archetypes = new ArchetypeLongNameConstraint(
                null, "act", "customerAccountCharges*", true, true);
        ActResultSet set = new ActResultSet(customer.getObjectReference(),
                                            archetypes, null, null, statuses,
                                            50, null);
        BigDecimal balance = BigDecimal.ZERO;
        while (set.hasNext()) {
            IPage<Act> acts = set.next();
            balance = ActHelper.sum(balance, acts.getRows(), "amount");
        }
        return balance;
    }

}
