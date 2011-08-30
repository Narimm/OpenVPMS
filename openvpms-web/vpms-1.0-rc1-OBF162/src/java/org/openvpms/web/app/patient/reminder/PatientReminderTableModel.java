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
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.reminder;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.Iterator;


/**
 * Patient reminder table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientReminderTableModel extends AbstractActTableModel {

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

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
     * The last retrieved patient.
     */
    private Party lastPatient;

    /**
     * The last retrieved patient owner.
     */
    private Party lastOwner;


    /**
     * Creates a new <tt>PatientReminderTableModel</tt>.
     */
    public PatientReminderTableModel() {
        super(new String[]{"act.patientReminder"});
        rules = new ReminderRules();
        patientRules = new PatientRules();
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
        Object result = null;
        int index = column.getModelIndex();
        if (index == nextDueIndex) {
            Date due = rules.getNextDueDate(act);
            if (due != null) {
                Label label = LabelFactory.create();
                label.setText(DateHelper.formatDate(due, false));
                result = label;
            }
        } else if (index == customerIndex) {
            Party customer = getPatientOwner(act);
            if (customer != null) {
                IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(
                        customer.getObjectReference(),
                        customer.getName(), true);
                result = viewer.getComponent();
            }
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
     * @return the value for the column
     */
    @Override
    protected Object getValue(Act object, DescriptorTableColumn column) {
        Object result = null;
        String name = column.getDescriptor(object).getName();
        if (name.equals("reminderType")) {
            ActBean bean = new ActBean(object);
            IMObjectReference ref = bean.getParticipantRef(
                    "participation.reminderType");
            if (ref != null) {
                IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(
                        ref, false);
                result = viewer.getComponent();
            }
        } else {
            result = super.getValue(object, column);
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
     * This implementation returns <code>null</code> to indicate that the
     * intersection should be calculated from all descriptors.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getDescriptorNames() {
        return new String[]{"endTime", "reminderType", "patient",
                            "reminderCount", "lastSent"};
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
                NodeDescriptor descriptor
                        = ((DescriptorTableColumn) col).getDescriptor();
                if (name.equals(descriptor.getName())) {
                    result = offset;
                    break;
                }
            }
            ++offset;
        }
        return result;
    }

    /**
     * Returns the action of an reminder.
     *
     * @param act the reminder
     * @return the action component, or <tt>null</tt>
     */
    private Component getAction(Act act) {
        Label result = LabelFactory.create();
        if (rules.shouldCancel(act, new Date())) {
            result.setText(Messages.get("patientremindertablemodel.cancel"));
        } else {
            Party customer = getPatientOwner(act);
            if (customer != null) {
                Contact contact = rules.getContact(customer, act);
                if (contact != null) {
                    if (TypeHelper.isA(contact, "contact.location")) {
                        result.setText(
                                Messages.get("patientremindertablemodel.post"));
                    } else if (TypeHelper.isA(contact, "contact.email")) {
                        result.setText(
                                Messages.get(
                                        "patientremindertablemodel.email"));
                    } else if (TypeHelper.isA(contact, "contact.phoneNumber")) {
                        result.setText(
                                Messages.get("patientremindertablemodel.list"));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the patient owner for a patient.
     *
     * @param act the act
     * @return the patient owner, or <tt>null</tt>
     */
    private Party getPatientOwner(Act act) {
        ActBean bean = new ActBean(act);
        IMObjectReference ref = bean.getParticipantRef("participation.patient");
        if (ref != null) {
            if (lastPatient == null
                    || !lastPatient.getObjectReference().equals(ref)) {
                lastPatient = (Party) IMObjectHelper.getObject(ref);
                if (lastPatient != null) {
                    lastOwner = patientRules.getOwner(lastPatient);
                } else {
                    lastOwner = null;
                }
            }
        }
        return lastOwner;
    }

}
