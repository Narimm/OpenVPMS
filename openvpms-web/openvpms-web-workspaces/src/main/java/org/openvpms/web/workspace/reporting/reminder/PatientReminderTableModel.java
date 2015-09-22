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

package org.openvpms.web.workspace.reporting.reminder;

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
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;


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
     * The last processed event.
     */
    private ReminderEvent lastEvent;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;


    /**
     * Constructs a {@code PatientReminderTableModel}.
     *
     * @param context the layout context
     */
    public PatientReminderTableModel(LayoutContext context) {
        super(new String[]{ReminderArchetypes.REMINDER}, context);
        patientRules = ServiceHelper.getBean(PatientRules.class);
        rules = new ReminderRules(ArchetypeServiceHelper.getArchetypeService(), new ReminderTypeCache(), patientRules);
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
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
            result = getCustomer(act);
        } else if (index == actionIndex) {
            result = getAction(act);
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
        switch (name) {
            case "reminderType":
                // uses the cached reminder type to reduce queries
                ReminderEvent event = getEvent(object);
                if (event != null && event.getReminderType() != null) {
                    Entity reminderType = event.getReminderType().getEntity();
                    result = createReferenceViewer(reminderType, false);
                }
                break;
            case "patient":
                // use the cached patient in the event to reduce queries
                Party patient = getPatient(object);
                if (patient != null) {
                    result = createReferenceViewer(patient, true);
                }
                break;
            default:
                result = super.getValue(object, column, row);
                break;
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
    protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(shortNames, context);
        nextDueIndex = getNextModelIndex(model);
        TableColumn nextDueColumn = createTableColumn(nextDueIndex, "patientremindertablemodel.nextDue");
        model.addColumn(nextDueColumn);
        model.moveColumn(model.getColumnCount() - 1, getColumnOffset(model, "reminderType"));

        customerIndex = getNextModelIndex(model);
        TableColumn customerColumn = createTableColumn(customerIndex, "patientremindertablemodel.customer");
        model.addColumn(customerColumn);
        model.moveColumn(model.getColumnCount() - 1, getColumnOffset(model, "patient"));

        actionIndex = getNextModelIndex(model);
        TableColumn actionColumn = createTableColumn(actionIndex, "patientremindertablemodel.action");
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
        return new String[]{"endTime", "reminderType", "patient", "reminderCount", "lastSent", "error"};
    }

    /**
     * Returns a component for the due date of a reminder.
     *
     * @param act the reminder
     * @return the due date. May be {@code null}
     */
    private Component getDueDate(Act act) {
        Label result = null;
        Date due = rules.getNextDueDate(act);
        if (due != null) {
            result = LabelFactory.create();
            result.setText(DateFormatter.formatDate(due, false));
        }
        return result;
    }

    /**
     * Returns a component for the customer of a reminder.
     *
     * @param act the reminder
     * @return the customer component, or {@code null}
     */
    private Component getCustomer(Act act) {
        Component result = null;
        Party customer = getPatientOwner(act);
        if (customer != null) {
            result = createReferenceViewer(customer, true);
        }
        return result;
    }

    /**
     * Returns the action of a reminder.
     *
     * @param act the reminder
     * @return the action component, or {@code null}
     */
    private Component getAction(Act act) {
        Label result = LabelFactory.create();
        ReminderEvent event = getEvent(act);
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
     * @return the corresponding reminder event, or {@code null} if the event can't be processed
     */
    private ReminderEvent getEvent(Act act) {
        if (lastEvent == null || lastEvent.getReminder() != act) {
            IMObjectBean bean = new IMObjectBean(act);
            try {
                lastEvent = getProcessor().process(act, bean.getInt("reminderCount"));
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
     * @return the patient. May be {@code null}
     */
    private Party getPatient(Act act) {
        ReminderEvent event = getEvent(act);
        return (event != null) ? event.getPatient() : null;
    }

    /**
     * Returns the owner for a patient.
     *
     * @param act the act
     * @return the patient owner, or {@code null}
     */
    private Party getPatientOwner(Act act) {
        ReminderEvent event = getEvent(act);
        return (event != null) ? event.getCustomer() : null;
    }

    /**
     * Creates an {@link IMObjectReferenceViewer} for an object.
     *
     * @param object the object
     * @param link   if {@code true} enable hyperlinks
     * @return the viewer component
     */
    private Component createReferenceViewer(IMObject object, boolean link) {
        ContextSwitchListener listener = (link) ? getLayoutContext().getContextSwitchListener() : null;
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(object.getObjectReference(), object.getName(),
                                                                     listener, getLayoutContext().getContext());
        return viewer.getComponent();
    }

    /**
     * Invoked after the table has been rendered.
     */
    @Override
    public void postRender() {
        super.postRender();
        processor = null; // disposes of the processor after rendering to release caches
    }

    /**
     * Returns a reminder processor, creating it if required.
     *
     * @return a reminder processor
     */
    private ReminderProcessor getProcessor() {
        if (processor == null) {
            boolean disableSMS = !SMSHelper.isSMSEnabled(getLayoutContext().getContext().getPractice());
            processor = new ReminderProcessor(null, null, new Date(), disableSMS, ServiceHelper.getArchetypeService(),
                                              patientRules);
            processor.setEvaluateFully(true);
        }
        return processor;
    }

}
