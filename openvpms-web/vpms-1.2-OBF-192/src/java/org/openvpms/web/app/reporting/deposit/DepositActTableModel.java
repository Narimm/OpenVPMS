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

package org.openvpms.web.app.reporting.deposit;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;


/**
 * Table model for <em>act.bankDeposit</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DepositActTableModel extends ActAmountTableModel<FinancialAct> {

    /**
     * Construct a new <tt>DepositActTableModel</tt>.
     */
    public DepositActTableModel() {
        super(true, true);
    }
}
