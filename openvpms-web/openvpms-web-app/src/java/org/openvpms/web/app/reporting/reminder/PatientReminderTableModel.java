/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting.reminder;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypeCache;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.Iterator;


/**
 * Patient reminder table model.
 *
 * @author Tim Anderson
 */
public class PatientReminderTableModel extends AbstractActTableModel {

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * The next due column index.
     */
    private int nextDueIndex;

    /**
     * The customer column index.
     */
    private int customerIndex;

    /**
     * The action column index.
     */
    private int actionIndex;

    /**
     * The reminder processor.
     */
    private ReminderProcessor processor;

    /**
     * The last processed row.
     */
    private int lastRow;

    /**
     * The last processed event.
     */
    private ReminderEvent lastEvent;


    /**
     * Constructs a <tt>PatientReminderTableModel</tt>.
     *
     * @param context the layout context
     */
    public PatientReminderTableModel(LayoutContext context) {
        super(new String[]{ReminderArchetypes.REMINDER}, context);
        rules = new ReminderRules(ArchetypeServiceHelper.getArchetypeService(),
                                  new ReminderTypeCache(), new PatientRules(ServiceHelper.getArchetypeService(),
                                                                            ServiceHelper.getLookupService()));
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria, or <code>null</code> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the object the object
     * @param column the table column
     * @param row    the table row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(Act act, TableColumn column, int row) {
        Object result;
        int index = column.getModelIndex();
        if (index == nextDueIndex) {
            result = getDueDate(act);
        } else if (index == customerIndex) {
            result = getCustomer(act, row);
        } else if (index == actionIndex) {
            result = getAction(act, row);
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @param row    the row
     * @return the value for the column
     */
    @Override
    protected Object getValue(Act object, DescriptorTableColumn column, int row) {
        Object result = null;
        String name = column.getName();
        if (name.equals("reminderType")) {
            // uses the cached reminder type to reduce queries
            ReminderEvent event = getEvent(object, row);
            if (event != null && event.getReminderType() != null) {
                Entity reminderType = event.getReminderType().getEntity();
                result = createReferenceViewer(reminderType, false);
            }
        } else if (name.equals("patient")) {
            // use the cached patient in the event to reduce queries
            Party patient = getPatient(object, row);
            if (patient != null) {
                result = createReferenceViewer(patient, true);
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(String[] shortNames,
                                                 LayoutContext context) {
        DefaultTableColumnModel model
            = (DefaultTableColumnModel) super.createColumnModel(shortNames,
                                                                context);
        nextDueIndex = getNextModelIndex(model);
        TableColumn nextDueColumn = createTableColumn(
            nextDueIndex, "patientremindertablemodel.nextDue");
        model.addColumn(nextDueColumn);
        model.moveColumn(model.getColumnCount() - 1,
                         getColumnOffset(model, "reminderType"));

        customerIndex = getNextModelIndex(model);
        TableColumn customerColumn = createTableColumn(
            customerIndex, "patientremindertablemodel.customer");
        model.addColumn(customerColumn);
        model.moveColumn(model.getColumnCount() - 1,
                         getColumnOffset(model, "patient"));

        actionIndex = getNextModelIndex(model);
        TableColumn actionColumn = createTableColumn(
            actionIndex, "patientremindertablemodel.action");
        model.addColumn(actionColumn);

        return model;
    }


    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"endTime", "reminderType", "patient",
            "reminderCount", "lastSent", "error"};
    }

    /**
     * Returns a column offset given its node name.
     *
     * @param model the model
     * @param name  the node name
     * @return the column offset, or <code>-1</code> if a column with the
     *         specified name doesn't exist
     */
    private int getColumnOffset(TableColumnModel model, String name) {
        int result = -1;
        int offset = 0;
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col instanceof DescriptorTableColumn) {
                DescriptorTableColumn descriptorCol
                    = (DescriptorTableColumn) col;
                if (descriptorCol.getName().equals(name)) {
                    result = offset;
                    break;
                }
            }
            ++offset;
        }
        return result;
    }

    /**
     * Returns a component for the due date of a reminder.
     *
     * @param act the reminder
     * @return the due date. May be <tt>null</tt>
     */
    private Component getDueDate(Act act) {
        Label result = null;
        Date due = rules.getNextDueDate(act);
        if (due != null) {
            result = LabelFactory.create();
            result.setText(DateHelper.formatDate(due, false));
        }
        return result;
    }

    /**
     * Returns a component for the customer of a reminder.
     *
     * @param act the reminder
     * @param row the current row
     * @return the customer component, or <tt>null</tt>
     */
    private Component getCustomer(Act act, int row) {
        Component result = null;
        Party customer = getPatientOwner(act, row);
        if (customer != null) {
            result = createReferenceViewer(customer, true);
        }
        return result;
    }

    /**
     * Returns the action of a reminder.
     *
     * @param act the reminder
     * @param row the current row
     * @return the action component, or <tt>null</tt>
     */
    private Component getAction(Act act, int row) {
        Label result = LabelFactory.create();
        ReminderEvent event = getEvent(act, row);
        if (event != null) {
            result.setText(Messages.get("patientremindertablemodel." + event.getAction().name()));
        } else {
            // error processing the event
            result.setText(Messages.get("patientremindertablemodel.SKIP"));
        }
        return result;
    }

    /**
     * Returns the reminder event for the specified act and row.
     *
     * @param act the reminder
     * @param row the current row
     * @return the corresponding reminder event, or <tt>null</tt> if the event can't be processed
     */
    private ReminderEvent getEvent(Act act, int row) {
        if (lastEvent == null || lastEvent.getReminder() != act) {
            IMObjectBean bean = new IMObjectBean(act);
            try {
                lastEvent = getProcessor(row).process(act, bean.getInt("reminderCount"));
            } catch (Throwable exception) {
                lastEvent = null;
            }
        }
        return lastEvent;
    }

    /**
     * Returns the patient.
     *
     * @param act the reminder
     * @param row the current row
     * @return the patient. May be <tt>null</tt>
     */
    private Party getPatient(Act act, int row) {
        ReminderEvent event = getEvent(act, row);
        return (event != null) ? event.getPatient() : null;
    }

    /**
     * Returns the owner for a patient.
     *
     * @param act the act
     * @param row the current row
     * @return the patient owner, or <tt>null</tt>
     */
    private Party getPatientOwner(Act act, int row) {
        ReminderEvent event = getEvent(act, row);
        return (event != null) ? event.getCustomer() : null;
    }

    /**
     * Creates an {@link IMObjectReferenceViewer} for an object.
     *
     * @param object the object
     * @param link   if <tt>true</tt> enable hyperlinks
     * @return the viewer component
     */
    private Component createReferenceViewer(IMObject object, boolean link) {
        ContextSwitchListener listener = (link) ? getLayoutContext().getContextSwitchListener() : null;
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(object.getObjectReference(), object.getName(),
                                                                     listener, getLayoutContext().getContext());
        return viewer.getComponent();
    }

    /**
     * Returns a reminder processor.
     * <p/>
     * TODO - this is a hack to cache a processor, but not for too long (i.e don't want the processingDate to become
     * too old, and internal caches too big).
     *
     * @param row the current row being rendered
     * @return a reminder processor
     */
    private ReminderProcessor getProcessor(int row) {
        if (processor == null || row < lastRow) {
            processor = new ReminderProcessor(null, null, new Date(), ServiceHelper.getArchetypeService(),
                                              new PatientRules(ServiceHelper.getArchetypeService(),
                                                               ServiceHelper.getLookupService()));
            processor.setEvaluateFully(true);
        }
        lastRow = row;
        return processor;
    }

}
