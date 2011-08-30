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

package org.openvpms.web.app.financial;

import org.openvpms.web.app.financial.deposit.DepositWorkspace;
import org.openvpms.web.app.financial.statement.StatementWorkspace;
import org.openvpms.web.app.financial.till.TillWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Financial subsystem.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class FinancialSubsystem extends AbstractSubsystem {

    public FinancialSubsystem() {
        super("financial");
        addWorkspace(new TillWorkspace());
        addWorkspace(new DepositWorkspace());
        addWorkspace(new StatementWorkspace());
    }
}
