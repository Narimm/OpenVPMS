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
import nextapp.echo2.app.event.WindowPaneEvent;
import static org.openvpms.archetype.rules.act.EstimationActStatus.INVOICED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.CANCELLED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.FinancialActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.FinancialActStatus.POSTED;
import org.openvpms.archetype.rules.finance.estimation.EstimationRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.app.customer.charge.CustomerInvoiceEditDialog;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
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
     * Constructs an <tt>EstimationCRUDWindow</tt>.
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
        boolean enableEdit = false;
        boolean enableDelete = false;
        boolean enablePost = false;
        boolean enableInvoice = false;

        if (enable) {
            Act act = getObject();
            enableEdit = canEdit(act);
            enableDelete = canDelete(act);
            enablePost = canPost(act);
            enableInvoice = canInvoice(act);
        }
        buttons.setEnabled(EDIT_ID, enableEdit);
        buttons.setEnabled(DELETE_ID, enableDelete);
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
        final Act act = IMObjectHelper.reload(getObject()); // make sure we have the latest version
        if (act != null) {
            String status = act.getStatus();
            if (CANCELLED.equals(status) || INVOICED.equals(status)) {
                showStatusError(act, "customer.estimation.noinvoice.title",
                                "customer.estimation.noinvoice.message");
            } else if (act.getActivityEndTime() != null && act.getActivityEndTime().before(new Date())) {
                showStatusError(act, "customer.estimation.expired.title", "customer.estimation.expired.message");
            } else {
                String title = Messages.get("customer.estimation.invoice.title");
                String message = Messages.get("customer.estimation.invoice.message");
                ConfirmationDialog dialog = new ConfirmationDialog(title, message);
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
     * Invoice out an estimation to the customer.
     *
     * @param estimation the estimation
     */
    private void invoice(final Act estimation) {
        rules = new EstimationRules();
        try {
            EstimationInvoicer invoicer = new EstimationInvoicer();
            CustomerInvoiceEditDialog editor = invoicer.invoice(estimation);
            if (editor != null) {
                editor.addWindowPaneListener(new WindowPaneListener() {
                    public void onClose(WindowPaneEvent event) {
                        onRefresh(estimation);
                    }
                });
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

    /**
     * Determines if an act can be posted (i.e finalised).
     * <p/>
     * This implementation returns <tt>true</tt> if the act isn't <tt>POSTED</tt>,<tt>CANCELLED</tt> nor
     * <tt>INVOICED</tt>
     *
     * @param act the act
     * @return <tt>true</tt> if the act can be posted
     */
    @Override
    protected boolean canPost(Act act) {
        return super.canPost(act) && !INVOICED.equals(act.getStatus());
    }

    /**
     * Determines if an estimation can be invoiced.
     *
     * @param act the estimation
     * @return <tt>true</tt> if the estimation can be invoiced, otherwise <tt>false</tt>
     */
    protected boolean canInvoice(Act act) {
        String status = act.getStatus();
        return !CANCELLED.equals(status) && !INVOICED.equals(status);
    }

}
