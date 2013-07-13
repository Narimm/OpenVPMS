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

import echopointng.DateField;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.bound.BoundDateFieldFactory;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ComponentHelper;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_PARTICIPATION;


/**
 * Queries <em>act.patientPrescription</em> acts for a patient.
 *
 * @author Tim Anderson
 */
public class PatientPrescriptionQuery extends ActQuery<Act> {

    /**
     * Indicates if all dates should be selected. If so, the expiry date is ignored.
     */
    private final SimpleProperty allDates = new SimpleProperty("all", true, Boolean.class,
                                                               Messages.get("daterange.all"));

    /**
     * The expiry date. If allDates is unselected, all prescriptions expiring before this date are excluded.
     */
    private SimpleProperty expiryDate;

    /**
     * The expiry date field.
     */
    private ComponentState dateField;


    /**
     * Patient prescription short names.
     */
    private static final String[] SHORT_NAMES = {PatientArchetypes.PRESCRIPTION};

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
            = new SortConstraint[]{new NodeSortConstraint("startTime", false)};


    /**
     * Constructs a {@link PatientPrescriptionQuery}.
     *
     * @param patient the patient
     */
    public PatientPrescriptionQuery(Party patient) {
        super(patient, "patient", PATIENT_PARTICIPATION, SHORT_NAMES, Act.class);
        setAuto(true);
        setDefaultSortConstraint(DEFAULT_SORT);

        expiryDate = new SimpleProperty("expiringAfter", new Date(), Date.class,
                                        Messages.get("patient.prescription.expiringAfter"));
        allDates.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onAllDatesChanged();
            }
        });
        onAllDatesChanged();
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        ComponentState all = new ComponentState(new BoundCheckBox(allDates), allDates);
        dateField = new ComponentState(BoundDateFieldFactory.create(expiryDate), expiryDate);
        Row row = RowFactory.create(Styles.CELL_SPACING, all.getLabel(), all.getComponent(), dateField.getLabel(),
                                    dateField.getComponent());
        container.add(row);

        FocusGroup group = getFocusGroup();
        group.add(all.getComponent());
        group.add(dateField.getComponent());
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        IConstraint times = null;
        if (!allDates.getBoolean()) {
            times = QueryHelper.createDateConstraint("endTime", expiryDate.getDate(), null);
        }
        ParticipantConstraint[] participants = {getParticipantConstraint()};
        return new ActResultSet<Act>(getArchetypeConstraint(), participants, times, getStatuses(), false,
                                     getConstraints(), getMaxResults(), sort);
    }

    /**
     * Invoked when the 'all dates' check box changes.
     */
    private void onAllDatesChanged() {
        boolean enabled = !allDates.getBoolean();
        ComponentHelper.enable(dateField.getLabel(), enabled);
        ComponentHelper.enable((DateField) dateField.getComponent(), enabled);
    }

}
