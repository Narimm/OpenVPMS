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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateTableModel;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_OWNER;


/**
 * Table model for entity relationships to patients that indicates if the patient is deceased.
 *
 * @author Tim Anderson
 */
public class PatientRelationshipStateTableModel extends RelationshipStateTableModel {

    /**
     * The type column index.
     */
    private static final int TYPE_INDEX = ACTIVE_INDEX + 1;

    /**
     * The start time column index.
     */
    private static final int START_TIME_INDEX = TYPE_INDEX + 1;

    /**
     * The end time column index.
     */
    private static final int END_TIME_INDEX = START_TIME_INDEX + 1;

    /**
     * Constructs a {@link PatientRelationshipStateTableModel}.
     *
     * @param context       layout context
     * @param displayTarget if {@code true} display the relationship target, otherwise display the source
     */
    public PatientRelationshipStateTableModel(LayoutContext context, boolean displayTarget) {
        super(context, displayTarget);
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] sort;
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == TYPE_INDEX) {
            sort = new SortConstraint[]{new ArchetypeSortConstraint(ascending), getNameSortConstraint(true)};
        } else if (col.getModelIndex() == START_TIME_INDEX) {
            sort = new SortConstraint[]{new NodeSortConstraint("activeStartTime", ascending),
                                        getNameSortConstraint(true)};
        } else if (col.getModelIndex() == END_TIME_INDEX) {
            sort = new SortConstraint[]{new NodeSortConstraint("activeEndTime", ascending),
                                        getNameSortConstraint(true)};
        } else {
            sort = super.getSortConstraints(column, ascending);
        }
        return sort;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(RelationshipState object, TableColumn column, int row) {
        Object result;
        switch (column.getModelIndex()) {
            case TYPE_INDEX:
                result = DescriptorHelper.getDisplayName(object.getRelationship());
                break;
            case START_TIME_INDEX:
                result = getDate(((PeriodRelationship) object.getRelationship()).getActiveStartTime());
                break;
            case END_TIME_INDEX:
                result = getDate(((PeriodRelationship) object.getRelationship()).getActiveEndTime());
                break;
            default:
                result = super.getValue(object, column, row);

        }
        return result;
    }


    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();

        TableColumn startTime = new TableColumn(START_TIME_INDEX);
        startTime.setHeaderValue(DescriptorHelper.getDisplayName(PATIENT_OWNER, "activeStartTime"));
        TableColumn endTime = new TableColumn((END_TIME_INDEX));
        endTime.setHeaderValue(DescriptorHelper.getDisplayName(PATIENT_OWNER, "activeEndTime"));

        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(TYPE_INDEX, ARCHETYPE));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        model.addColumn(startTime);
        model.addColumn(endTime);
        if (getShowActive()) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }
        return model;
    }

    /**
     * Returns the description of the source or target entity of the relationship, depending on the
     * {@link #displayTarget} flag.
     *
     * @param state the relationship
     * @return the source or target description
     */
    @Override
    protected Object getDescription(RelationshipState state) {
        Object result;
        PatientRelationshipState p = (PatientRelationshipState) state;
        IMObjectReference ref = (displayTarget()) ? state.getTarget() : state.getSource();
        if (TypeHelper.isA(ref, PatientArchetypes.PATIENT) && p.isDeceased()) {
            String desc = (displayTarget()) ? state.getTargetDescription() : state.getSourceDescription();
            Label label = LabelFactory.create();
            label.setText(desc);
            Label deceased = LabelFactory.create("patient.deceased", "Patient.Deceased");
            result = RowFactory.create(Styles.CELL_SPACING, label, deceased);
        } else {
            result = super.getDescription(state);
        }
        return result;
    }

    /**
     * Helper to format a date.
     *
     * @param date the date. May be {@code null}
     * @return the formatted date or {@code null} if {@code date} is {@code null}
     */
    private String getDate(Date date) {
        return (date != null) ? DateFormatter.formatDate(date, false) : null;
    }

}
