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

package org.openvpms.web.app.workflow;

import org.openvpms.web.app.workflow.appointment.AppointmentWorkspace;
import org.openvpms.web.app.workflow.investigation.InvestigationsWorkspace;
import org.openvpms.web.app.workflow.messaging.MessagingWorkspace;
import org.openvpms.web.app.workflow.worklist.TaskWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Workflow subsystem.
 *
 * @author Tim Anderson
 */
public class WorkflowSubsystem extends AbstractSubsystem {

    /**
     * Constructs a {@code WorkflowSubsystem}.
     *
     * @param context the context
     */
    public WorkflowSubsystem(Context context) {
        super("workflow");
        addWorkspace(new AppointmentWorkspace(context));
        addWorkspace(new TaskWorkspace(context));
        addWorkspace(new MessagingWorkspace(context));
        addWorkspace(new InvestigationsWorkspace(context, new PracticeMailContext(context)));
    }
}
