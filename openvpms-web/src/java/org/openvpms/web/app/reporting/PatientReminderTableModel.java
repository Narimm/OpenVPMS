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

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.patient.reminder.ReminderQuery;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Patient reminder table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientReminderTableModel extends AbstractIMTableModel<ObjectSet> {

    /**
     * The due date.
     */
    private static final int DUE_DATE_INDEX = 0;

    /**
     * The reminder type name index.
     */
    private static final int REMINDER_INDEX = 1;

    /**
     * The customer name index.
     */
    private static final int CUSTOMER_INDEX = 2;

    /**
     * The patient name index.
     */
    private static final int PATIENT_INDEX = 3;

    /**
     * The action index.
     */
    private static final int ACTION_INDEX = 4;

    /**
     * The column names.
     */
    private final String[] columnNames = {
            Messages.get("patientremindertablemodel.dueDate"),
            Messages.get("patientremindertablemodel.reminder"),
            Messages.get("patientremindertablemodel.customer"),
            Messages.get("patientremindertablemodel.patient"),
            Messages.get("patientremindertablemodel.action"),
    };

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;


    /**
     * Creates a new <tt>PatientReminderTableModel</tt>.
     */
    public PatientReminderTableModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        for (int i = 0; i < columnNames.length; ++i) {
            model.addColumn(new TableColumn(i));
        }
        setTableColumnModel(model);

        rules = new ReminderRules();
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
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
     * @param set    the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected Object getValue(ObjectSet set, TableColumn column, int row) {
        Object result = null;
        int index = column.getModelIndex();
        switch (index) {
            case DUE_DATE_INDEX:
                Date date = (Date) set.get(ReminderQuery.ACT_END_TIME);
                Label label = LabelFactory.create();
                if (date != null) {
                    label.setText(DateFormatter.formatDate(date, false));
                }
                result = label;
                break;
            case REMINDER_INDEX:
                result = getViewer(set, ReminderQuery.REMINDER_REFERENCE);
                break;
            case CUSTOMER_INDEX:
                result = getViewer(set, ReminderQuery.CUSTOMER_REFERENCE,
                                   ReminderQuery.CUSTOMER_NAME);
                break;
            case PATIENT_INDEX:
                result = getViewer(set, ReminderQuery.PATIENT_REFERENCE,
                                   ReminderQuery.PATIENT_NAME);
                break;
            case ACTION_INDEX:
                result = getAction(set);
        }
        return result;
    }

    private Component getAction(ObjectSet set) {
        Label result = LabelFactory.create();
        IMObjectReference ref = (IMObjectReference) set.get(
                ReminderQuery.CUSTOMER_REFERENCE);
        Party customer = (Party) IMObjectHelper.getObject(ref);
        if (customer != null) {
            Contact contact = rules.getContact(customer.getContacts());
            if (contact != null) {
                if (TypeHelper.isA(contact, "contact.location")) {
                    result.setText(
                            Messages.get("patientremindertablemodel.post"));
                } else if (TypeHelper.isA(contact, "contact.email")) {
                    result.setText(
                            Messages.get("patientremindertablemodel.email"));
                } else if (TypeHelper.isA(contact, "contact.phoneNumber")) {
                    result.setText(
                            Messages.get("patientremindertablemodel.list"));
                }
            }
        }
        return result;
    }

    /**
     * Returns a viewer for an object reference.
     *
     * @param set    the object set
     * @param refKey the object reference key
     * @return a new component to view the object reference
     */
    private Component getViewer(ObjectSet set, String refKey) {
        IMObjectReference ref = (IMObjectReference) set.get(refKey);
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(ref,
                                                                     false);
        return viewer.getComponent();
    }

    /**
     * Returns a viewer for an object reference.
     *
     * @param set     the object set
     * @param refKey  the object reference key
     * @param nameKey the entity name key
     * @return a new component to view the object reference
     */
    private Component getViewer(ObjectSet set, String refKey, String nameKey) {
        IMObjectReference ref = (IMObjectReference) set.get(refKey);
        String name = (String) set.get(nameKey);
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(
                ref, name, true);
        return viewer.getComponent();
    }
}
