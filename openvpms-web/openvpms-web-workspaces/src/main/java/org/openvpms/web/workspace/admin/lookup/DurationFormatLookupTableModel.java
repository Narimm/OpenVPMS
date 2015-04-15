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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;


/**
 * Table model for <em>lookup.durationformat</em> lookups that supresses the name node and disables sorting.
 * The latter is due to the objects being sorted in order of increasing interval.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DurationFormatLookupTableModel extends DescriptorTableModel<IMObject> {

    /**
     * Constructs a <tt>DurationFormatModel</tt>.
     *
     * @param context the layout context. May be <tt>null</tt>
     */
    public DurationFormatLookupTableModel(LayoutContext context) {
        super(new String[]{"lookup.durationformat"}, context);
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"interval", "units", "showYears", "showMonths", "showWeeks", "showDays"};
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise sort in <tt>descending</tt> order
     * @return <tt>null</tt>
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }
}
