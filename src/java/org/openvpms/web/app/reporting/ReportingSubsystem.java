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

package org.openvpms.web.app.reporting;

import org.openvpms.web.app.reporting.deposit.DepositWorkspace;
import org.openvpms.web.app.reporting.reminder.ReminderWorkspace;
import org.openvpms.web.app.reporting.statement.StatementWorkspace;
import org.openvpms.web.app.reporting.till.TillWorkspace;
import org.openvpms.web.app.reporting.wip.IncompleteChargesWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Reporting subsystem.
 *
 * @author Tim Anderson
 */
public class ReportingSubsystem extends AbstractSubsystem {

    /**
     * Constructs a {@code ReportingSubsystem}.
     */
    public ReportingSubsystem(Context context) {
        super("reporting");

        MailContext mailContext = new PracticeMailContext(context);

        addWorkspace(new TillWorkspace(context));
        addWorkspace(new DepositWorkspace(context));
        addWorkspace(new StatementWorkspace(context));
        addWorkspace(new IncompleteChargesWorkspace(context));
        addWorkspace(new ReminderWorkspace(context));
        addWorkspace(new ReportingWorkspace(context));
    }

}
