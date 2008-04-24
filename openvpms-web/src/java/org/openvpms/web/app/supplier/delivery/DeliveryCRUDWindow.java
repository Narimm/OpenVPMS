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

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.supplier.SupplierActCRUDWindow;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD window for supplier deliveries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-04-06 14:41:46Z $
 */
public class DeliveryCRUDWindow extends SupplierActCRUDWindow<Act> {
    private final OrderRules rules;

    /**
     * Create a new <tt>DeliveryCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public DeliveryCRUDWindow(Archetypes<Act> archetypes) {
        super(archetypes);
        rules = new OrderRules();
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <tt>true</tt> if the act status is <em>IN_PROGRESS</em>
     */
    @Override
    protected boolean canEdit(Act act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act
     * @return <tt>true</tt> if the act status is <em>IN_PROGRESS</em>
     */
    @Override
    protected boolean canDelete(Act act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(final Act act) {
        final OrderTableBrowser browser = new OrderTableBrowser();
        final OrderSelectionBrowserDialog dialog
                = new OrderSelectionBrowserDialog(
                Messages.get("supplier.delivery.selectorders.title"), browser);
        dialog.show();
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (OrderSelectionBrowserDialog.OK_ID.equals(
                        dialog.getAction())) {
                    onCreated(act, browser);
                }
            }
        });
    }

    /**
     * Invoked when an act is posted.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(Act act) {
        try {
            rules.updateOrders(act);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    private void onCreated(Act act, OrderTableBrowser browser) {
        addParticipations(act, browser.getSupplier(),
                          browser.getStockLocation());
        DeliveryEditor editor = new DeliveryEditor(act, null,
                                                   createLayoutContext());
        for (FinancialAct orderItem : browser.getSelectedOrderItems()) {
            FinancialAct deliveryItem = rules.createDeliveryItem(orderItem);
            editor.addItem(deliveryItem, orderItem);
        }
        edit(editor);
    }


}
