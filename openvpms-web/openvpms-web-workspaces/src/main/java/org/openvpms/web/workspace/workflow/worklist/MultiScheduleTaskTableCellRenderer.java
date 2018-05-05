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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Table;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Multi-schedule task table renderer.
 *
 * @author Tim Anderson
 */
public class MultiScheduleTaskTableCellRenderer extends TaskTableCellRenderer {

    /**
     * Constructs a {@link MultiScheduleTaskTableCellRenderer}.
     *
     * @param model the table model
     */
    public MultiScheduleTaskTableCellRenderer(MultiScheduleTaskTableModel model) {
        super(model);
    }

    /**
     * Returns a component representing an event.
     *
     * @param table  the table
     * @param event  the event
     * @param column the column
     * @param row    the row
     * @return the component
     */
    @Override
    protected Component getEvent(Table table, PropertySet event, int column, int row) {
        String text = evaluate(event);
        if (text == null) {
            String customer = event.getString(ScheduleEvent.CUSTOMER_NAME);
            String patient = event.getString(ScheduleEvent.PATIENT_NAME);
            String status = getModel().getStatus(event);
            if (patient == null) {
                text = Messages.format("workflow.scheduling.task.table.customer", customer, status);
            } else {
                text = Messages.format("workflow.scheduling.task.table.customerpatient", customer, patient, status);
            }
        }
        String notes = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        Component result = createLabelWithNotes(text, notes);
        styleEvent(event, result, table);
        return result;
    }
}
