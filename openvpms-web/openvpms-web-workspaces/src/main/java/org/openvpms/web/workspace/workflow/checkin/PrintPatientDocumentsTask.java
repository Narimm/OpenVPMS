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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Task to optionally print <em>act.patientDocumentForm</em> and <em>act.patientDocumentLetter</em> for a patient.
 *
 * @author Tim Anderson
 */
class PrintPatientDocumentsTask extends AbstractPrintPatientDocumentsTask {

    /**
     * Constructs a {@link PrintPatientDocumentsTask}.
     */
    public PrintPatientDocumentsTask(HelpContext help) {
        super(PrintIMObjectTask.PrintMode.BACKGROUND, help);
        setRequired(false);
    }

    /**
     * Returns the schedule to use to locate templates.
     *
     * @param context the context
     * @return the schedule, or {@code null} if there is no schedule
     */
    @Override
    protected Entity getSchedule(TaskContext context) {
        return context.getSchedule();
    }

    /**
     * Returns the work list to use to locate templates.
     *
     * @param context the context
     * @return the work list, or {@code null} if there is no work list
     */
    @Override
    protected Entity getWorkList(TaskContext context) {
        return CheckInHelper.getWorkList(context);
    }

}
