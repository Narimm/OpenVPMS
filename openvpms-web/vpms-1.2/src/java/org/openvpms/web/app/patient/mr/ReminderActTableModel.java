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

package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Table model for <em>act.patientReminder</em> and <em>act.patientAlert</em>
 * acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderActTableModel extends PatientRecordActTableModel {

    /**
     * Creates a new <tt>ReminderActTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     */
    public ReminderActTableModel(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"startTime", "endTime", "status", "description"};
    }

    /**
     * Returns the index to insert the archetype column.
     *
     * @return the index to insert the archetype column
     */
    @Override
    protected int getArchetypeColumnIndex() {
        return 2;
    }

    /**
     * Creates a column model for a set of archetypes.
     * This implementation adds the act.patientReminder product node.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(String[] shortNames,
                                                 LayoutContext context) {
        TableColumnModel model = super.createColumnModel(shortNames, context);
        String shortName = "act.patientReminder";
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                shortName);
        if (archetype != null) {
            addColumn(archetype, "product", model);
        }
        return model;
    }
}
