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

package org.openvpms.web.workspace.supplier.delivery;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActRelationshipTableModel;

/**
 * Table model for <em>actRelationship.supplierDeliveryItem</em> act relationships.
 * <p/>
 * This delegates to {@link DeliveryItemTableModel}.
 *
 * @author Tim Anderson
 */
public class DeliveryItemRelationshipTableModel extends AbstractActRelationshipTableModel<Act> {

    /**
     * Constructs a {@link DeliveryItemRelationshipTableModel}.
     *
     * @param relationshipTypes the act relationship short names
     * @param context           the layout context
     */
    public DeliveryItemRelationshipTableModel(String[] relationshipTypes, LayoutContext context) {
        this(relationshipTypes, null, context);
    }

    /**
     * Constructs a {@link DeliveryItemRelationshipTableModel}.
     *
     * @param relationshipTypes the act relationship short names
     * @param parent            the parent object. May be {@code null}
     * @param context           the layout context
     */
    public DeliveryItemRelationshipTableModel(String[] relationshipTypes, IMObject parent, LayoutContext context) {
        String[] shortNames = getTargetShortNames(relationshipTypes);
        setModel(new DeliveryItemTableModel(shortNames, parent, context));
    }

}
