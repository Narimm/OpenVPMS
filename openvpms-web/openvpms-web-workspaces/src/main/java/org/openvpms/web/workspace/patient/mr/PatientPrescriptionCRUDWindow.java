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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.DefaultEditorQueue;

/**
 * CRUD window for patient prescriptions.
 *
 * @author Tim Anderson
 */
public class PatientPrescriptionCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * The dispense button.
     */
    protected static final String DISPENSE_ID = "button.dispense";


    /**
     * Constructs a {@link PatientPrescriptionCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public PatientPrescriptionCRUDWindow(Archetypes<Act> archetypes, Context context, HelpContext help) {
        super(archetypes, new PrescriptionActions(), context, help);
    }

    /**
     * Constructs a {@link PatientPrescriptionCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param actions    the actions. If {@code null}, actions must be registered via {@link #setActions}.
     * @param context    the context
     * @param help       the help context
     */
    protected PatientPrescriptionCRUDWindow(Archetypes<Act> archetypes, PrescriptionActions actions, Context context,
                                            HelpContext help) {
        super(archetypes, actions, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPrintButton());
        buttons.add(ButtonFactory.create(DISPENSE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onDispense();
            }
        }));
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
        buttons.setEnabled(PRINT_ID, enable);
        buttons.setEnabled(DISPENSE_ID, getActions().canDispense(getObject()));
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected PrescriptionActions getActions() {
        return (PrescriptionActions) super.getActions();
    }

    /**
     * Dispenses a prescription.
     */
    protected void onDispense() {
        CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
        Context context = getContext();
        Party customer = context.getCustomer();
        if (customer != null) {
            FinancialAct invoice = rules.getInvoice(customer);
            if (invoice == null) {
                invoice = (FinancialAct) IMObjectCreator.create(CustomerAccountArchetypes.INVOICE);
                ActBean invoiceBean = new ActBean(invoice);
                invoiceBean.addNodeParticipation("customer", customer);
            }
            Act prescription = getObject();
            ActBean prescriptionBean = new ActBean(prescription);
            DefaultLayoutContext layout = new DefaultLayoutContext(new LocalContext(context), getHelpContext());
            final CustomerChargeActEditor editor = new CustomerChargeActEditor(invoice, null, layout, false);
            editor.getComponent();
            CustomerChargeActItemEditor item = editor.addItem();
            item.getComponent();
            item.setPromptForPrescriptions(false);
            item.setCancelPrescription(true);
            item.getPrescriptions().add(prescription);
            item.setEditorQueue(new DefaultEditorQueue(context) {
                @Override
                protected void completed() {
                    super.completed();
                    SaveHelper.save(editor);
                    onSaved(getObject(), false);
                }
            });
            item.setProductRef(prescriptionBean.getNodeParticipantRef("product"));
        } else {
            ErrorHelper.show(Messages.get("patient.prescription.nocustomer"));
        }
    }

    protected static class PrescriptionActions extends ActActions<Act> {

        /**
         * The prescription rules.
         */
        private final PrescriptionRules rules;

        public PrescriptionActions() {
            rules = ServiceHelper.getBean(PrescriptionRules.class);
        }

        /**
         * Determines if a prescription can be deleted.
         * <br/>
         * A prescription can be deleted if it hasn't been dispensed.
         *
         * @param prescription the prescription to check
         * @return {@code true} if it can be deleted
         */
        @Override
        public boolean canDelete(Act prescription) {
            if (super.canDelete(prescription)) {
                ActBean bean = new ActBean(prescription);
                return bean.getRelationships(PatientArchetypes.PRESCRIPTION_MEDICATION).isEmpty();
            }
            return false;
        }

        /**
         * Determines if a prescription can be dispensed.
         *
         * @param act the prescription
         * @return {@code true} if the prescription can be dispensed
         */
        public boolean canDispense(Act act) {
            return act != null && rules.canDispense(act);
        }
    }
}
