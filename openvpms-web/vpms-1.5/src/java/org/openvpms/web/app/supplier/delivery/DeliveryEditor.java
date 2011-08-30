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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.delivery;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.FinancialActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for <em>act.supplierDelivery</em> and <em>act.supplierReturn</em>
 * acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DeliveryEditor extends FinancialActEditor {

    /**
     * Construct a new <tt>ActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public DeliveryEditor(FinancialAct act, IMObject parent,
                          LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Adds a delivery item, associated with an order item.
     *
     * @param delivery the delivery item
     * @param order    the order item
     */
    public void addItem(FinancialAct delivery, FinancialAct order) {
        ActRelationshipCollectionEditor items = getEditor();
        items.add(delivery);
        DeliveryItemEditor itemEditor
                = (DeliveryItemEditor) getEditor().getEditor(delivery);
        itemEditor.setOrderItem(order);
    }

}
