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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.AbstractWorkspaces;
import org.openvpms.web.workspace.reporting.deposit.DepositWorkspace;
import org.openvpms.web.workspace.reporting.reminder.ReminderWorkspace;
import org.openvpms.web.workspace.reporting.statement.StatementWorkspace;
import org.openvpms.web.workspace.reporting.till.TillWorkspace;
import org.openvpms.web.workspace.reporting.wip.IncompleteChargesWorkspace;


/**
 * Reporting workspaces.
 *
 * @author Tim Anderson
 */
public class ReportingWorkspaces extends AbstractWorkspaces {

    /**
     * Constructs a {@code ReportingWorkspaces}.
     */
    public ReportingWorkspaces(Context context) {
        super("reporting");

        MailContext mailContext = new PracticeMailContext(context);
        addWorkspace(new TillWorkspace(context, mailContext));
        addWorkspace(new DepositWorkspace(context, mailContext));
        addWorkspace(new StatementWorkspace(context, mailContext));
        addWorkspace(new IncompleteChargesWorkspace(context, mailContext));
        addWorkspace(new ReminderWorkspace(context, mailContext));
        addWorkspace(new ReportingWorkspace(context, mailContext));
    }

}
