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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.patient.mr.PatientPrescriptionCRUDWindow;
import org.openvpms.web.workspace.patient.mr.prescription.PrescriptionDispenser;


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
        if (chargeEditor == null) {
            showStatusError(getObject(), "patient.prescription.dispense", "patient.prescription.noinvoice");
        } else {
            Act prescription = IMObjectHelper.reload(getObject());
            if (prescription != null) {
                PrescriptionDispenser dispenser = new PrescriptionDispenser(getContext(), getHelpContext());
                dispenser.dispense(prescription, chargeEditor, () -> {
                    onSaved(getObject(), false);
                });
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
