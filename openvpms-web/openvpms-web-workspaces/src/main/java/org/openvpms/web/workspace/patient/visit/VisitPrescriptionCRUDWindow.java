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

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.DefaultEditorQueue;
import org.openvpms.web.workspace.customer.charge.EditorQueue;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.patient.mr.PatientPrescriptionCRUDWindow;


/**
 * Prescription CRUD window that enables dispensing of prescriptions within the {@link VisitEditor}.
 *
 * @author Tim Anderson
 */
public class VisitPrescriptionCRUDWindow extends PatientPrescriptionCRUDWindow {

    /**
     * The charge editor.
     */
    private VisitChargeEditor chargeEditor;

    /**
     * Constructs a {@link VisitPrescriptionCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param help       the help context
     */
    public VisitPrescriptionCRUDWindow(Archetypes<Act> archetypes, Context context, HelpContext help) {
        super(archetypes, null, context, help);
        setActions(new Actions());
    }

    /**
     * Registers the charge editor.
     *
     * @param chargeEditor the charge editor. May be {@code null}
     */
    public void setChargeEditor(VisitChargeEditor chargeEditor) {
        this.chargeEditor = chargeEditor;
    }

    /**
     * Dispenses a prescription.
     */
    @Override
    protected void onDispense() {
        Act prescription = getObject();
        ActBean prescriptionBean = new ActBean(prescription);
        if (chargeEditor == null) {
            showStatusError(prescription, "patient.prescription.dispense", "patient.prescription.noinvoice");
        } else {
            if (!chargeEditor.isValid()) {
                // don't add prescription to invalid invoice
                showStatusError(prescription, "patient.prescription.dispense", "patient.prescription.saveinvoice");
            } else {
                final CustomerChargeActItemEditor item = chargeEditor.addItem();
                if (item == null) {
                    // shouldn't happen, but prompt user to save just in case
                    showStatusError(prescription, "patient.prescription.dispense", "patient.prescription.saveinvoice");
                } else {
                    item.getComponent();
                    item.setPromptForPrescriptions(false);
                    item.setCancelPrescription(true);
                    item.getPrescriptions().add(prescription);
                    final EditorQueue existing = item.getEditorQueue();
                    item.setEditorQueue(new DefaultEditorQueue(getContext()) {
                        @Override
                        protected void completed() {
                            super.completed();
                            item.setEditorQueue(existing);
                            SaveHelper.save(chargeEditor);
                            onSaved(getObject(), false);
                        }

                        @Override
                        protected void cancelled() {
                            super.cancelled();
                            // need to remove the item from the invoice
                            chargeEditor.removeItem((Act) item.getObject());
                        }
                    });
                    item.setProductRef(prescriptionBean.getNodeParticipantRef("product"));
                }
            }
        }
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        return getContainer();
    }

    /**
     * Creates a layout context for viewing objects.
     *
     * @return a new layout context
     */
    @Override
    protected LayoutContext createViewLayoutContext() {
        LayoutContext context = super.createViewLayoutContext();
        context.setContextSwitchListener(null);
        return context;
    }

    private class Actions extends PrescriptionActions {

        /**
         * Determines if a prescription can be dispensed.
         *
         * @param act the prescription
         * @return {@code true} if the prescription can be dispensed
         */
        @Override
        public boolean canDispense(Act act) {
            return chargeEditor != null && super.canDispense(act);
        }
    }
}
