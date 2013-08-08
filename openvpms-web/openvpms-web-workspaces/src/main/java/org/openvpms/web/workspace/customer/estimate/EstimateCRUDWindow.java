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

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.estimate.EstimateRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.CRUDWindowListener;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerActCRUDWindow;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;

import java.util.Date;

import static org.openvpms.archetype.rules.act.ActStatus.CANCELLED;
import static org.openvpms.archetype.rules.act.EstimateActStatus.INVOICED;


/**
 * CRUD window for estimate acts.
 *
 * @author Tim Anderson
 */
public class EstimateCRUDWindow extends CustomerActCRUDWindow<Act> {

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";

    /**
     * Invoice button identifier.
     */
    private static final String INVOICE_ID = "invoice";

    /**
     * The rules.
     */
    private final EstimateRules rules;


    /**
     * Constructs an {@link EstimateCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public EstimateCRUDWindow(Archetypes<Act> archetypes, Context context, HelpContext help) {
        super(archetypes, new EstimateActions(), context, help);
        rules = ServiceHelper.getBean(EstimateRules.class);
    }

    /**
     * Returns the operations that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected EstimateActions getActions() {
        return (EstimateActions) super.getActions();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button copy = ButtonFactory.create(COPY_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCopy();
            }
        });
        Button invoice = ButtonFactory.create(INVOICE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onInvoice();
            }
        });
        buttons.add(createPostButton());
        buttons.add(createPreviewButton());
        buttons.add(copy);
        buttons.add(invoice);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        boolean enablePost = false;
        boolean enableInvoice = false;

        if (enable) {
            Act act = getObject();
            EstimateActions ops = getActions();
            enablePost = ops.canPost(act);
            enableInvoice = ops.canInvoice(act);
        }
        buttons.setEnabled(POST_ID, enablePost);
        buttons.setEnabled(PREVIEW_ID, enable);
        buttons.setEnabled(COPY_ID, enable);
        buttons.setEnabled(INVOICE_ID, enableInvoice);
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        Act object = getObject();
        try {
            String title = Messages.format("customer.estimate.copy.title", object.getTitle());
            Act copy = rules.copy(object, title);
            setObject(copy);
            CRUDWindowListener<Act> listener = getListener();
            if (listener != null) {
                listener.saved(copy, true);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimate.copy.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Invoked when the 'invoice' button is pressed.
     */
    protected void onInvoice() {
        final Act act = IMObjectHelper.reload(getObject()); // make sure we have the latest version
        if (act != null) {
            if (canInvoice(act)) {
                String title = Messages.get("customer.estimate.invoice.title");
                String message = Messages.get("customer.estimate.invoice.message");
                HelpContext help = getHelpContext().subtopic("invoice");
                ConfirmationDialog dialog = new ConfirmationDialog(title, message, help);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        invoice(act);
                    }
                });
                dialog.show();
            }
        } else {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        }
    }

    /**
     * Determines if an estimate can be invoiced.
     *
     * @param act the estimate
     * @return {@code true} if the estimate can be invoiced, otherwise {@code false}
     */
    protected boolean canInvoice(Act act) {
        boolean result = false;
        String status = act.getStatus();
        if (CANCELLED.equals(status) || INVOICED.equals(status)) {
            showStatusError(act, "customer.estimate.noinvoice.title", "customer.estimate.noinvoice.message");
        } else if (expired(act)) {
            showStatusError(act, "customer.estimate.expired.title", "customer.estimate.expired.message");
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Returns the estimate rules.
     *
     * @return the rules
     */
    protected EstimateRules getRules() {
        return rules;
    }

    /**
     * Determines if an estimate has expired.
     *
     * @param act the estimate act
     * @return {@code true} if the estimate has expired
     */
    private boolean expired(Act act) {
        boolean result = false;
        Date endTime = DateRules.getDate(act.getActivityEndTime());
        if (endTime != null) {
            result = endTime.before(DateRules.getToday());
        }
        return result;
    }

    /**
     * Invoice out an estimate to the customer.
     *
     * @param estimate the estimate
     */
    protected void invoice(final Act estimate) {
        try {
            final FinancialAct invoice = getInvoice(estimate);
            if (invoice != null) {
                String title = Messages.get("customer.estimate.existinginvoice.title");
                String message = Messages.get("customer.estimate.existinginvoice.message");
                ConfirmationDialog dialog = new ConfirmationDialog(title, message);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        invoice(estimate, invoice);
                    }
                });
                dialog.show();
            } else {
                invoice(estimate, invoice);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimate.invoice.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Invoices an estimate.
     *
     * @param estimate the estimate
     * @param invoice  the invoice to add items to. If {@code null}, one will be created
     */
    private void invoice(final Act estimate, FinancialAct invoice) {
        try {
            EstimateInvoicer invoicer = new EstimateInvoicer();
            HelpContext edit = getHelpContext().topic(CustomerAccountArchetypes.INVOICE + "/edit");
            CustomerChargeActEditDialog editor = invoicer.invoice(estimate, invoice,
                                                                  new DefaultLayoutContext(true, getContext(), edit));
            editor.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    onRefresh(estimate);
                }
            });
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimate.invoice.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Returns the most recent IN_PROGRESS or COMPLETED invoice to add estimate items to.
     *
     * @param estimate the estimate
     * @return the invoice, or {@code null} if none exists
     */
    private FinancialAct getInvoice(Act estimate) {
        FinancialAct result = null;
        ActBean bean = new ActBean(estimate);
        Party customer = (Party) bean.getNodeParticipant("customer");
        if (customer != null) {
            CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
            result = rules.getInvoice(customer);
        }
        return result;
    }

}
