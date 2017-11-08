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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.boarding;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayScheduleGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;

/**
 * A table model for displaying check-ins for a particular date.
 * Displays a message if there are no check-outs on the date.
 *
 * @author Tim Anderson
 */
public class CheckOutTableModel extends CheckInOutTableModel {

    /**
     * Constructs a {@link CheckOutTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public CheckOutTableModel(AbstractMultiDayScheduleGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, colours);
    }

    /**
     * Creates a cell renderer to render the table when there are no check-ins or check-outs.
     *
     * @return the renderer
     */
    @Override
    protected EmptyTableRenderer createEmptyTableCellRenderer() {
        return new EmptyTableRenderer("workflow.scheduling.appointment.checkout.none");
    }
}
