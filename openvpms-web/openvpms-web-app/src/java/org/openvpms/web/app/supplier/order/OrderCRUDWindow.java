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

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.esci.adapter.client.OrderServiceAdapter;
import org.openvpms.web.app.supplier.SelectStockDetailsDialog;
import org.openvpms.web.app.supplier.SupplierHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.component.dialog.InformationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.processor.BatchProcessorDialog;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;
import java.util.List;


/**
 * CRUD window for supplier orders.
 *
 * @author Tim Anderson
 */
public class OrderCRUDWindow extends ESCISupplierCRUDWindow {

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";

    /**
     * Generate orders button identifier.
     */
    private static final String GENERATE_ID = "generateOrders";


    /**
     * Constructs an {@code OrderCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public OrderCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, OrderActions.INSTANCE, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        Button copy = ButtonFactory.create(COPY_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCopy();
            }
        });
        Button generate = ButtonFactory.create(GENERATE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onGenerate();
            }
        });
        super.layoutButtons(buttons);
        buttons.add(createPostButton());
        buttons.add(createPreviewButton());
        buttons.add(copy);
        buttons.add(generate);
        buttons.add(createCheckInboxButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        boolean editEnabled = false;
        boolean deletePostEnabled = false;
        if (enable) {
            FinancialAct object = getObject();
            editEnabled = getActions().canEdit(object);
            String status = object.getStatus();
            deletePostEnabled = !OrderStatus.POSTED.equals(status) && !OrderStatus.ACCEPTED.equals(status)
                                && !OrderStatus.CANCELLED.equals(status);
        }
        buttons.setEnabled(EDIT_ID, editEnabled);
        buttons.setEnabled(DELETE_ID, deletePostEnabled);
        buttons.setEnabled(POST_ID, deletePostEnabled);
        buttons.setEnabled(PREVIEW_ID, enable);
        buttons.setEnabled(COPY_ID, enable);
    }

    /**
     * Invoked when a new order has been created.
     * <p/>
     * This implementation pops up a dialog to select the supplier and stock location, then displays an edit dialog for
     * the act.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(final FinancialAct act) {
        String title = Messages.get("supplier.order.selectdetails.title", DescriptorHelper.getDisplayName(act));
        final SelectStockDetailsDialog dialog = new SelectStockDetailsDialog(title, getContext(),
                                                                             getHelpContext().subtopic("new"));
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
            OrderRules rules = SupplierHelper.createOrderRules(getContext().getPractice());
            FinancialAct object = getObject();
            FinancialAct copy = rules.copyOrder(object);
            String notes = Messages.get("supplier.order.copy.notes", object.getTitle());
            copy.setTitle(notes);
            edit(copy);
        } catch (OpenVPMSException exception) {
            String title = Messages.get("supplier.order.copy.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Invoked when posting of an act is complete, either by saving the act
     * with <em>POSTED</em> status, or invoking {@link #onPost()}.
     * <p/>
     * This implementation sends the order to the supplier if they are ESCI-enabled; otherwise it just prints the act.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(FinancialAct act) {
        SupplierRules rules = new SupplierRules(ServiceHelper.getArchetypeService());
        if (rules.getSupplierStockLocation(act) != null) {
            // ESCI is configured for the supplier, so submit the order
            try {
                OrderServiceAdapter service = ServiceHelper.getOrderService();
                service.submitOrder(act);
                scheduleCheckInbox(true); // poll in 30 secs to see if there any responses
                InformationDialog.show(Messages.get("supplier.order.sent.title"),
                                       Messages.get("supplier.order.sent.message"));
            } catch (Throwable exception) {
                // failed to submit the order, so revert to IN_PROGRESS
                act.setStatus(ActStatus.IN_PROGRESS);
                SaveHelper.save(act);

                ErrorHelper.show(exception);
                onRefresh(act);
            }
        } else {
            print(act);
        }
    }

    /**
     * Invoked to generate orders.
     */
    private void onGenerate() {
        String title = Messages.get("supplier.order.generate.title");
        HelpContext help = getHelpContext().subtopic("generate");
        final StockLocationSupplierDialog dialog = new StockLocationSupplierDialog(title, getContext(), help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                Party location = dialog.getStockLocation();
                Party supplier = dialog.getSupplier();
                generateOrders(location, supplier, dialog.getStockLocations(), dialog.getSuppliers());
            }
        });
        dialog.show();
    }

    /**
     * Generates orders.
     *
     * @param stockLocation the selected stock location. May be {@code null} to indicate all stock locations
     * @param supplier      the selected supplier. May be {@code null} to indicate all suppliers
     * @param locations     the available stock locations
     * @param suppliers     the available suppliers
     */
    private void generateOrders(Party stockLocation, Party supplier, List<IMObject> locations,
                                List<IMObject> suppliers) {
        final String title = Messages.get("supplier.order.generate.title");
        if (stockLocation != null) {
            locations = Arrays.asList((IMObject) stockLocation);
        }
        if (supplier != null) {
            suppliers = Arrays.asList((IMObject) supplier);
        }
        final OrderProgressBarProcessor processor = new OrderProgressBarProcessor(
            getContext().getPractice(), locations, suppliers, title);
        final BatchProcessorDialog dialog = new BatchProcessorDialog(processor.getTitle(), processor);
        processor.setListener(new BatchProcessorListener() {
            public void completed() {
                dialog.close();
                String message = Messages.get("supplier.order.generated.message", processor.getOrders());
                InformationDialog.show(title, message);
                onRefresh(null);
            }

            public void error(Throwable exception) {
                ErrorHelper.show(exception);
                onRefresh(null);
            }
        });
        dialog.show();
    }

}
