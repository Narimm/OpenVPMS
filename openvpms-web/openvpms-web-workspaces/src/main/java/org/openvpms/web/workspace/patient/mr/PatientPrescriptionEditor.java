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

import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

/**
 * An editor for <em>act.patientPrescription</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientPrescriptionEditor extends PatientActEditor {

    /**
     * Constructs a {@link PatientPrescriptionEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public PatientPrescriptionEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (act.isNew()) {
            calculateEndTime();
        }
        addStartEndTimeListeners();
    }

    /**
     * Invoked when layout has completed.
     * <p/>
     * This registers a listener to be notified of product changes.
     */
    @Override
    protected void onLayoutCompleted() {
        ParticipationEditor<Entity> editor = getParticipationEditor("product", true);
        if (editor != null) {
            editor.addModifiableListener(new ModifiableListener() {
                @Override
                public void modified(Modifiable modifiable) {
                    onProductChanged();
                }
            });
        }

    }

    /**
     * Invoked when the start time changes. Sets the value to end time if start time > end time.
     */
    @Override
    protected void onStartTimeChanged() {
        try {
            calculateEndTime();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the end time changes. Recalculates the end time if it is less than the start time.
     */
    @Override
    protected void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                calculateEndTime();
            }
        }
    }

    /**
     * Calculates the end time if the start time  and practice is set.
     *
     * @throws OpenVPMSException for any error
     */
    private void calculateEndTime() {
        Date start = getStartTime();
        Party practice = getLayoutContext().getContext().getPractice();
        if (start != null && practice != null) {
            PracticeRules rules = ServiceHelper.getBean(PracticeRules.class);
            setEndTime(rules.getPrescriptionExpiryDate(start, practice));
        }
    }

    /**
     * Invoked when the product changes. This updates the label with the product's dispensing instructions.
     */
    private void onProductChanged() {
        Product product = (Product) getParticipant("product");
        if (product != null) {
            IMObjectBean bean = new IMObjectBean(product);
            if (bean.hasNode("dispInstructions")) {
                Property label = getProperty("label");
                label.setValue(bean.getValue("dispInstructions"));
            }
        }
    }

}
