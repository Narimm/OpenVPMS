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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.appointment.repeat.ScheduleEventSeriesState;

/**
 * Prompts to delete a calendar series.
 *
 * @author Tim Anderson
 */
public class DeleteSeriesDialog extends SeriesDialog {

    /**
     * Constructs a {@link DeleteSeriesDialog}.
     *
     * @param series the event series
     * @param help   the help context
     */
    public DeleteSeriesDialog(ScheduleEventSeriesState series, HelpContext help) {
        super(Messages.format("workflow.scheduling.appointment.deleteseries.title", series.getDisplayName()),
              Messages.format("workflow.scheduling.appointment.deleteseries.message", series.getDisplayName()),
              series, help);
    }
}
