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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.estimation;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import static org.openvpms.archetype.rules.act.EstimationActStatus.INVOICED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.*;
import org.openvpms.archetype.rules.finance.estimation.EstimationRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * CRUD window for estimation acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EstimationCRUDWindow extends CustomerActCRUDWindow<Act> {

    /**
     * The copy button.
     */
    private Button copy;

    /**
     * The invoice button.
     */
    private Button invoice;

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
     * Create a new <tt>EstimationCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public EstimationCRUDWindow(Archetypes<Act> archetypes) {
        super(archetypes);
        rules = new EstimationRules();
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
        invoice = ButtonFactory.create(INVOICE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onInvoice();
            }
        });
        enableButtons(buttons, true);
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
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPostButton());
            buttons.add(getPreviewButton());
            buttons.add(copy);
            buttons.add(invoice);
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        Act object = getObject();
        try {
            String title = Messages.get("customer.estimation.copy.title",
                                        object.getTitle());
            Act copy = rules.copy(object, title);
            setObject(copy);
            CRUDWindowListener<Act> listener = getListener();
            if (listener != null) {
                listener.saved(copy, true);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimation.copy.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Invoked when the 'invoice' button is pressed.
     */
    protected void onInvoice() {
        final Act act = getObject();
        String status = act.getStatus();
        if (CANCELLED.equals(status) || INVOICED.equals(status)) {
            showStatusError(act, "customer.estimation.noinvoice.title",
                            "customer.estimation.noinvoice.message");
        } else if (act.getActivityEndTime() != null
                && act.getActivityEndTime().before(new Date())) {
            showStatusError(act, "customer.estimation.expired.title",
                            "customer.estimation.expired.message");
        } else {
            String title = Messages.get("customer.estimation.invoice.title");
            String message = Messages.get(
                    "customer.estimation.invoice.message");
            ConfirmationDialog dialog = new ConfirmationDialog(title, message);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    invoice(act);
                }
            });
            dialog.show();
        }
    }

    /**
     * Invoice out an estimation to the customer.
     *
     * @param estimation the estimation
     */
    private void invoice(Act estimation) {
        rules = new EstimationRules();
        try {
            GlobalContext context = GlobalContext.getInstance();
            rules.invoice(estimation, context.getClinician());
            setObject(estimation);
            CRUDWindowListener<Act> listener = getListener();
            if (listener != null) {
                listener.saved(estimation, false);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimation.invoice.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act
     * @return <tt>true</tt> if the act can be deleted, otherwise
     *         <tt>false</tt>
     */
    @Override
    protected boolean canDelete(Act act) {
        String status = act.getStatus();
        return !(POSTED.equals(status) || INVOICED.equals(status));
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
        String status = act.getStatus();
        return IN_PROGRESS.equals(status) || COMPLETED.equals(status)
                || CANCELLED.equals(status);
    }


}
