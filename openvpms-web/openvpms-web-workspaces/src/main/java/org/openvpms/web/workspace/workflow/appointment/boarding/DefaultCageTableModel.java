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

import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;

/**
 * Table model for appointments that span multiple days where the appointments are grouped by cage type.
 *
 * @author Tim Anderson
 */
public class DefaultCageTableModel extends CageTableModel {

    /**
     * Constructs a {@link DefaultCageTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public DefaultCageTableModel(CageScheduleGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, colours);
    }

    /**
     * Returns a renderer to render event cells.
     *
     * @return a new renderer
     */
    @Override
    protected TableCellRenderer createEventRenderer() {
        return new CageTableCellRenderer(this);
    }
}
