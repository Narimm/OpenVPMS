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

package org.openvpms.web.app.customer.estimation;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.estimation.EstimationRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.app.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.app.workflow.GetInvoiceTask;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.subsystem.CRUDWindowListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

import static org.openvpms.archetype.rules.act.EstimationActStatus.INVOICED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.CANCELLED;


/**
 * CRUD window for estimation acts.
 *
 * @author Tim Anderson
 */
public class EstimationCRUDWindow extends CustomerActCRUDWindow<Act> {

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
    private EstimationRules rules;


    /**
     * Constructs an {@code EstimationCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public EstimationCRUDWindow(Archetypes<Act> archetypes, Context context, HelpContext help) {
        super(archetypes, new EstimateActions(), context, help);
        rules = new EstimationRules();
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
            String title = Messages.get("customer.estimate.copy.title",
                                        object.getTitle());
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
            String status = act.getStatus();
            if (CANCELLED.equals(status) || INVOICED.equals(status)) {
                showStatusError(act, "customer.estimate.noinvoice.title", "customer.estimate.noinvoice.message");
            } else if (expired(act)) {
                showStatusError(act, "customer.estimate.expired.title", "customer.estimate.expired.message");
            } else {
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
            ErrorDialog.show(Messages.get("imobject.noexist", getArchetypes().getDisplayName()));
        }
    }

    /**
     * Determines if an estimation has expired.
     *
     * @param act the estimation act
     * @return the estimation act
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
     * Invoice out an estimation to the customer.
     *
     * @param estimation the estimation
     */
    private void invoice(final Act estimation) {
        try {
            final FinancialAct invoice = getInvoice(estimation);
            if (invoice != null) {
                String title = Messages.get("customer.estimate.existinginvoice.title");
                String message = Messages.get("customer.estimate.existinginvoice.message");
                ConfirmationDialog dialog = new ConfirmationDialog(title, message);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        invoice(estimation, invoice);
                    }
                });
                dialog.show();
            } else {
                invoice(estimation, invoice);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimate.invoice.failed");
            ErrorHelper.show(title, exception);
        }
    }

    private void invoice(final Act estimation, FinancialAct invoice) {
        try {
            EstimationInvoicer invoicer = new EstimationInvoicer();
            HelpContext edit = getHelpContext().topic(invoice, "edit");
            CustomerChargeActEditDialog editor = invoicer.invoice(estimation, invoice,
                                                                  new DefaultLayoutContext(true, getContext(), edit));
            editor.addWindowPaneListener(new WindowPaneListener() {
                public void onClose(WindowPaneEvent event) {
                    onRefresh(estimation);
                }
            });
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimate.invoice.failed");
            ErrorHelper.show(title, exception);
        }
    }

    private FinancialAct getInvoice(Act estimation) {
        ActBean bean = new ActBean(estimation);
        Party customer = (Party) bean.getNodeParticipant("customer");
        if (customer != null) {
            TaskContext context = new DefaultTaskContext(getContext(), getHelpContext());
            context.setCustomer(customer);
            GetInvoiceTask task = new GetInvoiceTask();
            task.execute(context);
            return (FinancialAct) context.getObject(CustomerAccountArchetypes.INVOICE);
        }
        return null;
    }

}
