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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.query.Query;


/**
 * A table model that displays the ID, archetype, name, description and active status.
 *
 * @author Tim Anderson
 */
public class NameDescTableModel<T extends IMObject> extends BaseIMObjectTableModel<T> {

    /**
     * Constructs an {@link NameDescTableModel}.
     */
    public NameDescTableModel() {
        super(null);
        setTableColumnModel(createTableColumnModel(true, true, true));
    }

    /**
     * Constructs an {@link NameDescTableModel}.
     *
     * @param query the query. If both active and inactive results are being queried, an Active column will be displayed
     */
    public NameDescTableModel(Query<T> query) {
        super(null);
        boolean active = query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createTableColumnModel(true, true, active));
    }

}
