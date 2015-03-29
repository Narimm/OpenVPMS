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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.patient.mr.PrescriptionMedicationActEditor;
import org.openvpms.web.workspace.patient.mr.Prescriptions;

/**
 * An editor for <em>actRelationship.invoiceItemDispensing</em> collections.
 * <p/>
 * This creates {@link PrescriptionMedicationActEditor} editors if the <em>act.patientMedication</em> is associated
 * with a prescription.
 *
 * @author Tim Anderson
 */
public class DispensingActRelationshipCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * The prescriptions.
     */
    private Prescriptions prescriptions;

    /**
     * Constructs a {@link DispensingActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public DispensingActRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
        setExcludeDefaultValueObject(false);
    }

    /**
     * Registers the prescriptions.
     *
     * @param prescriptions the prescriptions. May be {@code null}
     */
    public void setPrescriptions(Prescriptions prescriptions) {
        this.prescriptions = prescriptions;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        super.remove(object);
        if (prescriptions != null) {
            prescriptions.removeMedication((Act) object);
        }
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public PrescriptionMedicationActEditor createEditor(IMObject object, LayoutContext context) {
        return new PrescriptionMedicationActEditor((Act) object, (Act) getObject(), prescriptions, context);
    }

}
