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
 * Prompts to move a calendar series.
 *
 * @author Tim Anderson
 */
class MoveSeriesDialog extends SeriesDialog {

    /**
     * Constructs a {@link MoveSeriesDialog}.
     *
     * @param series the appointment series
     * @param help   the help context
     */
    public MoveSeriesDialog(ScheduleEventSeriesState series, HelpContext help) {
        super(Messages.format("workflow.scheduling.appointment.moveseries.title", series.getDisplayName()),
              Messages.format("workflow.scheduling.appointment.moveseries.message", series.getDisplayName()),
              series, help);
    }
}
