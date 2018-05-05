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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.List;

/**
 * An editor for <em>act.patientPrescription</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientPrescriptionEditor extends PatientActEditor {

    /**
     * The dispensing notes.
     */
    private final DispensingNotes dispensingNotes;

    /**
     * Constructs a {@link PatientPrescriptionEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public PatientPrescriptionEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        dispensingNotes = new DispensingNotes();
        if (act.isNew()) {
            calculateEndTime();
        }
        addStartEndTimeListeners(); // startTime is read-only so only the end time listener will trigger
        dispensingNotes.setProduct((Product) getParticipant("product"));
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PrescriptionLayoutStrategy() {
            /**
             * Lays out components in a grid.
             *
             * @param object     the object to lay out
             * @param properties the properties
             * @param context    the layout context
             * @param columns    the no. of columns to use
             */
            @Override
            protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context,
                                               int columns) {
                ComponentGrid grid = super.createGrid(object, properties, context, columns);
                ComponentState usage = dispensingNotes.getComponent(context);
                Component label = ColumnFactory.create(usage.getLabel());
                Component text = ColumnFactory.create(usage.getComponent());
                text.setLayoutData(ComponentGrid.layout(1, columns * 2 - 1));
                grid.add(label, text);
                return grid;
            }

        };
    }

    /**
     * Invoked when layout has completed.
     * <p>
     * This registers a listener to be notified of product changes.
     */
    @Override
    protected void onLayoutCompleted() {
        ProductParticipationEditor editor
                = (ProductParticipationEditor) (ParticipationEditor) getParticipationEditor("product", true);
        if (editor != null) {
            editor.setPatient(getPatient());
            editor.addModifiableListener(modifiable -> onProductChanged());
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
        dispensingNotes.setProduct(product);
    }

}
