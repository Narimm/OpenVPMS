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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.CustomerObjectSetQuery;
import org.openvpms.web.component.im.table.AbstractIMTableModel;


/**
 * Table model for rendering customer details as returned by
 * {@link CustomerObjectSetQuery}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerTableModel extends AbstractIMTableModel<ObjectSet> {

    /**
     * Determines if the patient should be displayed.
     */
    private boolean showPatient;

    /**
     * Determines if the contact should be displayed.
     */
    private boolean showContact;

    /**
     * The customer name index.
     */
    private static final int NAME_INDEX = 0;

    /**
     * The customer description index.
     */
    private static final int DESCRIPTION_INDEX = 1;

    /**
     * The patient index.
     */
    private static final int PATIENT_INDEX = 2;

    /**
     * The contact index.
     */
    private static final int CONTACT_INDEX = 3;


    /**
     * Creates a new <tt>CustomerTableModel</tt>.
     */
    public CustomerTableModel() {
        setTableColumnModel(createTableColumnModel(false, false));
    }

    /**
     * Determines if the patient and/or contact columns should be displayed.
     *
     * @param patient if <tt>true</tt> display the patient column
     * @param contact if <tt>true</tt> display the contact column
     */
    public void showColumns(boolean patient, boolean contact) {
        if (patient != showPatient || contact != showContact) {
            showPatient = patient;
            showContact = contact;
            setTableColumnModel(
                    createTableColumnModel(showPatient, showContact));
        }
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
            case NAME_INDEX:
                result = getCustomerName(set);
                break;
            case DESCRIPTION_INDEX:
                result = getCustomerDescription(set);
                break;
            case PATIENT_INDEX:
                result = getPatientName(set);
                break;
            case CONTACT_INDEX:
                result = getContact(set);
                break;
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise
     *                  sort in <tt>descending</tt> order
     * @return the sort criteria, or <tt>null</tt> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint result = null;
        if (column == NAME_INDEX) {
            result = new NodeSortConstraint("customer", "name", ascending);
        } else if (column == DESCRIPTION_INDEX) {
            result = new NodeSortConstraint("customer", "description",
                                            ascending);
        } else if (column == PATIENT_INDEX) {
            result = new NodeSortConstraint("patient", "name", ascending);
        } else if (column == CONTACT_INDEX) {
            result = new NodeSortConstraint("contact", "description",
                                            ascending);
        }
        return (result != null) ? new SortConstraint[]{result} : null;
    }

    /**
     * Returns the customer name.
     *
     * @param set the set
     * @return the customer name, or <tt>null</tt> if none is found
     */
    private String getCustomerName(ObjectSet set) {
        Party customer = (Party) set.get("customer");
        return (customer != null) ? customer.getName() : null;
    }

    /**
     * Returns the customer description.
     *
     * @param set the set
     * @return the customer description, or <tt>null</tt> if none is found
     */
    private String getCustomerDescription(ObjectSet set) {
        Party customer = (Party) set.get("customer");
        return (customer != null) ? customer.getDescription() : null;
    }

    /**
     * Returns the patient name.
     *
     * @param set the set
     * @return the patient name, or <tt>null</tt> if none is found
     */
    private String getPatientName(ObjectSet set) {
        Party patient = (Party) set.get("patient");
        return (patient != null) ? patient.getName() : null;
    }

    /**
     * Returns the contact description.
     *
     * @param set the set
     * @return the contact description, or <tt>null</tt> if none is found
     */
    private String getContact(ObjectSet set) {
        Contact contact = (Contact) set.get("contact");
        return (contact != null) ? contact.getDescription() : null;
    }

    /**
     * Creates the column model.
     *
     * @param showPatient if <tt>true</tt> display the patient column
     * @param showContact if <tt>true</tt> display the contact column
     * @return a new column model
     */
    private static TableColumnModel createTableColumnModel(
            boolean showPatient, boolean showContact) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(NAME_INDEX, "table.imobject.name"));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX,
                                          "table.imobject.description"));
        if (showPatient) {
            model.addColumn(
                    createTableColumn(PATIENT_INDEX, "customerquery.patient"));
        }
        if (showContact) {
            model.addColumn(
                    createTableColumn(CONTACT_INDEX, "customerquery.contact"));
        }

        return model;
    }

}
