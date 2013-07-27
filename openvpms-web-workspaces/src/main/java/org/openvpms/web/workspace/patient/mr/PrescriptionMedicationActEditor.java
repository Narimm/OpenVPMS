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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * An editor for <em>act.patientMedication</em> that enables the medication to be dispensed from a prescription.
 *
 * @author Tim Anderson
 */
public class PrescriptionMedicationActEditor extends PatientMedicationActEditor {

    /**
     * The prescription manager.
     */
    private final Prescriptions prescriptions;

    /**
     * The current prescription. May be {@code null}
     */
    private Prescription prescription;


    /**
     * Constructs a {@link PrescriptionMedicationActEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent act. May be {@code null}
     * @param prescriptions the prescriptions. May be {@code null}
     * @param context       the layout context
     */
    public PrescriptionMedicationActEditor(Act act, Act parent, Prescriptions prescriptions, LayoutContext context) {
        super(act, parent, context);
        this.prescriptions = prescriptions;
        setProductReadOnly(true);
    }

    /**
     * Sets the prescription.
     *
     * @param act the prescription act. May be {@code null}
     */
    public void setPrescription(Act act) {
        boolean changeLayout = (prescription != null && act == null) || (prescription == null && act != null);
        boolean readOnly = act != null;
        if (prescription != null) {
            prescription.removeMedication((Act) getObject());
            prescription = null;
        }
        if (act != null) {
            Prescription prescription = prescriptions.create(act);
            prescription.addMedication((Act) getObject());
            setProduct((Product) getObject(prescription.getProduct()));
            setQuantity(prescription.getQuantity());
            this.prescription = prescription;
            String label = prescription.getLabel();
            if (!StringUtils.isEmpty(label)) {
                setLabel(label);
            }
        }
        if (changeLayout) {
            setDispensedFromPrescription(readOnly);
            onLayout();
        }
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient reference. May be {@code null}
     */
    @Override
    public void setPatient(IMObjectReference patient) {
        super.setPatient(patient);
        if (prescription != null) {
            setPrescription(null); // remove the existing prescription
        }
    }

    /**
     * Invoked when the product is modified.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);
        if (prescription != null) {
            setPrescription(null); // remove the existing prescription
        }
    }

}
