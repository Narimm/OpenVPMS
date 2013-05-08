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

package org.openvpms.web.app.reporting.till;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActRelationshipTableModel;


/**
 * Table model for <em>actRelationship.tillBalanceItems</em>.
 * This displays all related acts in a {@link TillActTableModel}.
 *
 * @author Tim Anderson
 */
public class TillBalanceActRelationshipTableModel
    extends AbstractActRelationshipTableModel<FinancialAct> {

    /**
     * Constructs a {@code TillBalanceActRelationshipTableModel}
     *
     * @param context the layout context
     */
    public TillBalanceActRelationshipTableModel(LayoutContext context) {
        setModel(new TillActTableModel(context));
    }
}
