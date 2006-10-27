/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.LabelFactory;

import java.util.Date;


/**
 * Appointment table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentTableModel extends AbstractActTableModel {

    /**
     * The nodes to display.
     */
    public static final String[] NODE_NAMES
            = new String[]{"startTime", "endTime", "status", "appointmentType",
                           "customer", "patient", "reason", "description"};

    /**
     * Creates a new <code>AppointmentTableModel</code>.
     */
    public AppointmentTableModel() {
        super(new String[]{"act.customerAppointment"});
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @return the value for the column
     */
    @Override
    protected Object getValue(IMObject object, DescriptorTableColumn column) {
        Object result;
        NodeDescriptor descriptor = column.getDescriptor();
        String name = descriptor.getName();
        if (name.equals("startTime") || name.equals("endTime")) {
            Date time = (Date) descriptor.getValue(object);
            String text = null;
            if (time != null) {
                text = DateFormatter.formatTime(time, false);
            }
            Label label = LabelFactory.create();
            label.setText(text);
            result = label;
        } else {
            result = super.getValue(object, column);
        }
        return result;
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getDescriptorNames() {
        return NODE_NAMES;
    }

}
