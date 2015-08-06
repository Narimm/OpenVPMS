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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActRelationshipTableModel;

/**
 * Relationship table model for <em>actRelationship.customerAccountInvoiceItem</em>,
 * <em>actRelationship.customerAccountCreditItem</em>, <em>actRelationship.customerAccountCounterItem</em>
 * and <em>actRelationship.customerEstimateItem</em>.
 * <p/>
 * This uses an {@link ChargeItemTableModel}.
 *
 * @author Tim Anderson
 */
public class ChargeActRelationshipTableModel extends AbstractActRelationshipTableModel<Act> {

    /**
     * Constructs a {@link ChargeActRelationshipTableModel}.
     *
     * @param relationshipTypes the act relationship short names
     * @param context           the layout context. May be {@code null}
     */
    public ChargeActRelationshipTableModel(String[] relationshipTypes, LayoutContext context) {
        String[] shortNames = getTargetShortNames(relationshipTypes);
        setModel(new ChargeItemTableModel<Act>(shortNames, context));
    }
}