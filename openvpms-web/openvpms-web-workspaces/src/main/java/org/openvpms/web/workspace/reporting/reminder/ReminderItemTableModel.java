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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;

import java.util.List;

import static org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes.REMINDER;
import static org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes.REMINDER_ITEMS;
import static org.openvpms.component.business.service.archetype.helper.DescriptorHelper.getDisplayName;

/**
 * Table model for <em>act.patientReminderItem*</em> acts.
 *
 * @author Tim Anderson
 */
public class ReminderItemTableModel extends AbstractIMTableModel<ObjectSet> {

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * Archetype column index.
     */
    private static final int ARCHETYPE_INDEX = 0;

    /**
     * Status index.
     */
    private static final int STATUS_INDEX = ARCHETYPE_INDEX + 1;

    /**
     * Process by column index.
     */
    private static final int SEND_DATE_INDEX = STATUS_INDEX + 1;

    /**
     * Due date column index.
     */
    private static final int DUE_DATE_INDEX = SEND_DATE_INDEX + 1;

    /**
     * Customer column index.
     */
    private static final int CUSTOMER_INDEX = DUE_DATE_INDEX + 1;

    /**
     * Patient column index.
     */
    private static final int PATIENT_INDEX = CUSTOMER_INDEX + 1;

    /**
     * Reminder type column index.
     */
    private static final int REMINDER_TYPE_INDEX = PATIENT_INDEX + 1;

    /**
     * Reminder count column index.
     */
    private static final int REMINDER_COUNT_INDEX = REMINDER_TYPE_INDEX + 1;

    /**
     * Error column index.
     */
    private static final int ERROR_INDEX = REMINDER_COUNT_INDEX + 1;


    /**
     * Constructs a {@link ReminderItemTableModel}.
     *
     * @param context the layout context
     */
    public ReminderItemTableModel(LayoutContext context) {
        this.context = context;
        setTableColumnModel(createTableColumnModel());
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
        return null;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    @Override
    protected Object getValue(ObjectSet object, TableColumn column, int row) {
        switch (column.getModelIndex()) {
            case ARCHETYPE_INDEX:
                return DescriptorHelper.getDisplayName(getReminderItem(object));
            case STATUS_INDEX:
            case SEND_DATE_INDEX:
            case DUE_DATE_INDEX:
            case REMINDER_COUNT_INDEX:
            case ERROR_INDEX:
                return ((DescriptorTableColumn) column).getComponent(getReminderItem(object), context);
            case CUSTOMER_INDEX:
                Party customer = (Party) object.get("customer");
                Context customerContext = new LocalContext();
                customerContext.addObject(customer);
                return new IMObjectReferenceViewer(customer.getObjectReference(), customer.getName(), true,
                                                   customerContext).getComponent();
            case PATIENT_INDEX:
                Party patient = (Party) object.get("patient");
                Context patientContext = new LocalContext();
                patientContext.addObject(patient);
                return new IMObjectReferenceViewer(patient.getObjectReference(), patient.getName(), true,
                                                   patientContext).getComponent();
            case REMINDER_TYPE_INDEX:
                return ((DescriptorTableColumn) column).getComponent(getReminder(object), context);
        }
        return null;
    }

    private Act getReminder(ObjectSet object) {
        return (Act) object.get("reminder");
    }

    private Act getReminderItem(ObjectSet object) {
        return (Act) object.get("item");
    }

    /**
     * Creates the column model.
     *
     * @return a new column model
     */
    protected DefaultTableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        List<ArchetypeDescriptor> itemArchetypes = DescriptorHelper.getArchetypeDescriptors(REMINDER_ITEMS);
        List<ArchetypeDescriptor> reminderArchetype = DescriptorHelper.getArchetypeDescriptors(REMINDER);
        model.addColumn(createTableColumn(ARCHETYPE_INDEX, ARCHETYPE));
        model.addColumn(new DescriptorTableColumn(STATUS_INDEX, "status", itemArchetypes));
        model.addColumn(new DescriptorTableColumn(SEND_DATE_INDEX, "startTime", itemArchetypes));
        model.addColumn(new DescriptorTableColumn(DUE_DATE_INDEX, "endTime", itemArchetypes));
        model.addColumn(createTableColumn(CUSTOMER_INDEX, "patientremindertablemodel.customer"));
        model.addColumn(createColumn(PATIENT_INDEX, getDisplayName(REMINDER, "patient")));
        model.addColumn(new DescriptorTableColumn(REMINDER_TYPE_INDEX, "reminderType", reminderArchetype));
        model.addColumn(new DescriptorTableColumn(REMINDER_COUNT_INDEX, "count", itemArchetypes));
        model.addColumn(new DescriptorTableColumn(ERROR_INDEX, "error", itemArchetypes));
        return model;
    }

    private TableColumn createColumn(int index, String displayName) {
        TableColumn column = new TableColumn(index);
        column.setHeaderValue(displayName);
        return column;
    }

}
