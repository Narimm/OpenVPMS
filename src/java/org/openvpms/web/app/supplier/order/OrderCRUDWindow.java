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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.supplier.SelectStockDetailsDialog;
import org.openvpms.web.app.supplier.SupplierActCRUDWindow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD window for supplier orders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OrderCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * The copy button.
     */
    private Button copy;

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";


    /**
     * Create a new <tt>OrderCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public OrderCRUDWindow(Archetypes<FinancialAct> archetypes) {
        super(archetypes);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        copy = ButtonFactory.create(COPY_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCopy();
            }
        });
        enableButtons(buttons, false);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            FinancialAct object = getObject();
            if (canEdit(object)) {
                buttons.add(getEditButton());
            }
            buttons.add(getCreateButton());
            String status = object.getStatus();
            if (!ActStatus.POSTED.equals(status) &&
                !ActStatus.CANCELLED.equals(status)) {
                buttons.add(getDeleteButton());
                buttons.add(getPostButton());
            }
            buttons.add(getPreviewButton());
            buttons.add(copy);
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when a new order has been created.
     * <p/>
     * This implementation pops up a dialog to select the supplier and stock
     * location, then displays an edit dialog for the act.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(final FinancialAct act) {
        String title = Messages.get("supplier.order.selectdetails.title",
                                    DescriptorHelper.getDisplayName(act));
        final SelectStockDetailsDialog dialog
                = new SelectStockDetailsDialog(title,
                                               GlobalContext.getInstance());
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                Party supplier = dialog.getSupplier();
                Party location = dialog.getStockLocation();
                addParticipations(act, supplier, location);
                edit(act);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        try {
            OrderRules rules = new OrderRules();
            FinancialAct object = getObject();
            FinancialAct copy = rules.copyOrder(object);
            String notes = Messages.get("supplier.order.copy.notes",
                                        object.getTitle());
            copy.setTitle(notes);
            edit(copy);
        } catch (OpenVPMSException exception) {
            String title = Messages.get("supplier.order.copy.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <tt>true</tt> if the act can be edited, otherwise
     *         <tt>false</tt>
     */
    @Override
    protected boolean canEdit(Act act) {
        IMObjectBean bean = new IMObjectBean(act);
        return !DeliveryStatus.FULL.toString().equals(bean.getString("deliveryStatus"));
    }

    /**
     * Invoked when posting of an act is complete, either by saving the act
     * with <em>POSTED</em> status, or invoking {@link #onPost()}.
     * <p/>
     * This implementation does nothing.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(FinancialAct act) {
        print(act);
    }

}
