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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.admin.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;


/**
 * Table model for <em>lookup.*</em> objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupTableModel extends BaseIMObjectTableModel<Lookup> {

    /**
     * Constructs a <tt>LookupTableModel</tt>.
     * <p/>
     * This displays the archetype column.
     */
    public LookupTableModel() {
        setTableColumnModel(createTableColumnModel(true));
    }

    /**
     * Constructs a <tt>LookupTableModel</tt>.
     * <p/>
     * This displays the archetype column if the short names reference multiple archetypes.
     *
     * @param shortNames the archetype short names
     */
    public LookupTableModel(String[] shortNames) {
        shortNames = DescriptorHelper.getShortNames(shortNames); // expand any wildcards
        boolean showArchetypes = shortNames.length > 1;
        setTableColumnModel(createTableColumnModel(showArchetypes));
    }

}
