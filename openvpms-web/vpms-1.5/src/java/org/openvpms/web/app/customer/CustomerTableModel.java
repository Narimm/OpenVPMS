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
import org.openvpms.web.component.im.table.AbstractEntityObjectSetTableModel;


/**
 * Table model for rendering customer details as returned by
 * {@link CustomerObjectSetQuery}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerTableModel extends AbstractEntityObjectSetTableModel {

    /**
     * Determines if the patient should be displayed.
     */
    private boolean showPatient;

    /**
     * Determines if the contact should be displayed.
     */
    private boolean showContact;

    /**
     * Determines if the identity should be displayed.
     */
    private boolean showIdentity;

    /**
     * The patient index.
     */
    private static final int PATIENT_INDEX = NEXT_INDEX;

    /**
     * The contact index.
     */
    private static final int CONTACT_INDEX = PATIENT_INDEX + 1;


    /**
     * Creates a new <tt>CustomerTableModel</tt>.
     */
    public CustomerTableModel() {
        super("customer", "identity");
        setTableColumnModel(createTableColumnModel(false, false, false));
    }

    /**
     * Determines if the patient, contact and/or identity columns should be displayed.
     *
     * @param patient  if <tt>true</tt> display the patient column
     * @param contact  if <tt>true</tt> display the contact column
     * @param identity if <tt>true</tt> display the identity column
     */
    public void showColumns(boolean patient, boolean contact, boolean identity) {
        if (patient != showPatient || contact != showContact || identity != showIdentity) {
            showPatient = patient;
            showContact = contact;
            showIdentity = identity;
            setTableColumnModel(createTableColumnModel(showPatient, showContact, showIdentity));
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
        Object result;
        int index = column.getModelIndex();
        switch (index) {
            case PATIENT_INDEX:
                result = getPatientName(set);
                break;
            case CONTACT_INDEX:
                result = getContact(set);
                break;
            default:
                result = super.getValue(set, column, row);
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise sort in <tt>descending</tt> order
     * @return the sort criteria, or <tt>null</tt> if the column isn't sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        if (column == PATIENT_INDEX) {
            result = new SortConstraint[]{new NodeSortConstraint("patient", "name", ascending)};
        } else if (column == CONTACT_INDEX) {
            result = new SortConstraint[]{new NodeSortConstraint("contact", "description", ascending)};
        } else {
            result = super.getSortConstraints(column, ascending);
        }
        return result;
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
     * @param showPatient  if <tt>true</tt> display the patient column
     * @param showContact  if <tt>true</tt> display the contact column
     * @param showIdentity if <tt>true</tt> display the identity column
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showPatient, boolean showContact, boolean showIdentity) {
        DefaultTableColumnModel model = createTableColumnModel(false);
        if (showPatient) {
            model.addColumn(createTableColumn(PATIENT_INDEX, "customerquery.patient"));
        }
        if (showContact) {
            model.addColumn(createTableColumn(CONTACT_INDEX, "customerquery.contact"));
        }
        if (showIdentity) {
            model.addColumn(createTableColumn(IDENTITY_INDEX, IDENTITY));
        }

        return model;
    }

}
