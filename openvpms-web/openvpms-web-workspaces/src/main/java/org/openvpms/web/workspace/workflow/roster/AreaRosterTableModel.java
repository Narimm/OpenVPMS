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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.archetype.rules.workflow.roster.RosterEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.table.Cell;

/**
 * Displays roster events by area.
 *
 * @author Tim Anderson
 */
class AreaRosterTableModel extends RosterTableModel {

    /**
     * Constructs a {@link AreaRosterTableModel}.
     *
     * @param grid    the grid
     * @param context the context
     */
    AreaRosterTableModel(RosterEventGrid grid, Context context) {
        super(grid, "workflow.rostering.area", RosterEvent.USER_NAME, context);
    }

    /**
     * Returns the cell that the specified event appears in.
     *
     * @param event the event
     * @return the corresponding cell. May be {@code null}
     */
    @Override
    public Cell getCell(PropertySet event) {
        return getCell(event.getReference(RosterEvent.SCHEDULE_REFERENCE),
                       event.getReference(RosterEvent.ACT_REFERENCE));
    }

}
