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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow;

import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.alert.MandatoryAlerts;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;

/**
 * Task to display mandatory patient alerts.
 *
 * @author Tim Anderson
 */
public class MandatoryPatientAlertTask extends AbstractTask {

    /**
     * Starts the task.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    @Override
    public void start(TaskContext context) {
        MandatoryAlerts alerts = new MandatoryAlerts(context, context.getHelpContext());
        alerts.show(context.getPatient(), this::notifyCompleted);
    }
}
