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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;

import java.util.List;

/**
 * Table model for <em>act.customerChargesInvoiceItem</em> and
 * <em>act.customerChargesCreditItem</em>
 * acts.
 */
public class ChargesActTableModel extends AbstractActTableModel {

    /**
     * Creates a new <tt>ChargesActTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     */
    public ChargesActTableModel(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"startTime", "product", "clinician", "qty",
                            "fixedPrice", "unitPrice", "discount", "tax",
                            "total"};
    }

    /**
     * Determines if the archetype column should be displayed.
     * <p/>
     *
     * @param archetypes the archetypes
     * @return <tt>false</tt>
     */
    @Override
    protected boolean showArchetypeColumn(
            List<ArchetypeDescriptor> archetypes) {
        return false;
    }

}
