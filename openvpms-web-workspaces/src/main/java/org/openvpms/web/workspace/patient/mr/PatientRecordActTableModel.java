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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;


/**
 * Table model for patient record acts. Displays the archetype in the second
 * column.
 *
 * @author Tim Anderson
 */
public class PatientRecordActTableModel extends AbstractActTableModel {

    /**
     * Constructs a {@code PatientRecordActTableModel}.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context
     */
    public PatientRecordActTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Returns the index to insert the archetype column.
     *
     * @return the index to insert the archetype column
     */
    @Override
    protected int getArchetypeColumnIndex() {
        return 1;
    }
}
