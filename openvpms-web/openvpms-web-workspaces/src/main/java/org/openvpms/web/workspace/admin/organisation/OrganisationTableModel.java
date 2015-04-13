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
package org.openvpms.web.workspace.admin.organisation;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;


/**
 * Table model for <em>party.organisation*</em> objects. Displays the archetype name.
 *
 * @author Tim Anderson
 */
public class OrganisationTableModel extends BaseIMObjectTableModel<Party> {

    /**
     * Constructs an {@link OrganisationTableModel}.
     */
    public OrganisationTableModel() {
        super(null);
        setTableColumnModel(createTableColumnModel(true, true, true));
    }

    /**
     * Constructs an {@link OrganisationTableModel}.
     *
     * @param query the query. If both active and inactive results are being queried, an Active column will be displayed
     */
    public OrganisationTableModel(Query<Party> query) {
        super(null);
        boolean active = query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createTableColumnModel(true, true, active));
    }

}
