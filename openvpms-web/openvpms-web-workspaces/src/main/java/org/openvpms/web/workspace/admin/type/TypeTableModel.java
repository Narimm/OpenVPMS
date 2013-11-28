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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.admin.type;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;


/**
 * Table model for <em>entity.*Type</em> objects. Displays the archetype name.
 *
 * @author Tim Anderson
 */
public class TypeTableModel extends BaseIMObjectTableModel<Entity> {

    /**
     * Constructs an {@link TypeTableModel}.
     */
    public TypeTableModel() {
        super(null);
        setTableColumnModel(createTableColumnModel(false, true, true));
    }

    /**
     * Constructs an {@link TypeTableModel}.
     *
     * @param query the query. If both active and inactive results are being queried, an Active column will be displayed
     */
    public TypeTableModel(Query<Entity> query) {
        super(null);
        boolean showActive = query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createTableColumnModel(false, true, showActive));
    }

}
