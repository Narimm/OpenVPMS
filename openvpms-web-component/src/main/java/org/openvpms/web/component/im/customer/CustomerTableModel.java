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

package org.openvpms.web.component.im.customer;

import nextapp.echo2.app.CheckBox;
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
 * @author Tim Anderson
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
     * Determines if the active column should be displayed.
     */
    private boolean showActive;

    /**
     * The patient name index.
     */
    private static final int PATIENT_NAME_INDEX = NEXT_INDEX;

    /**
     * The patient description index.
     */
    private static final int PATIENT_DESC_INDEX = PATIENT_NAME_INDEX + 1;

    /**
     * The patient active index.
     */
    private static final int PATIENT_ACTIVE_INDEX = PATIENT_DESC_INDEX + 1;

    /**
     * The contact index.
     */
    private static final int CONTACT_INDEX = PATIENT_ACTIVE_INDEX + 1;


    /**
     * Constructs a {@link CustomerTableModel}.
     */
    public CustomerTableModel() {
        super("customer", "identity");
        setTableColumnModel(createTableColumnModel(false, false, false, false));
    }

    /**
     * Determines if the patient, contact and, identity, and/or active columns should be displayed.
     *
     * @param patient  if {@code true} display the patient column
     * @param contact  if {@code true} display the contact column
     * @param identity if {@code true} display the identity column
     * @param active   if {@code true} display the active column
     */
    public void showColumns(boolean patient, boolean contact, boolean identity, boolean active) {
        if (patient != showPatient || contact != showContact || identity != showIdentity || active != showActive) {
            showPatient = patient;
            showContact = contact;
            showIdentity = identity;
            showActive = active;
            setTableColumnModel(createTableColumnModel(showPatient, showContact, showIdentity, showActive));
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
            case PATIENT_NAME_INDEX:
                result = getPatientName(set);
                break;
            case PATIENT_DESC_INDEX:
                result = getPatientDescription(set);
                break;
            case PATIENT_ACTIVE_INDEX:
                result = getPatientActive(set);
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
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        if (column == PATIENT_NAME_INDEX) {
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
     * @return the patient name, or {@code null} if none is found
     */
    private String getPatientName(ObjectSet set) {
        Party patient = (Party) set.get("patient");
        return (patient != null) ? patient.getName() : null;
    }

    /**
     * Returns the patient description.
     *
     * @param set the set
     * @return the patient description, or {@code null} if none is found
     */
    private String getPatientDescription(ObjectSet set) {
        Party patient = (Party) set.get("patient");
        return (patient != null) ? patient.getDescription() : null;
    }

    /**
     * Returns a checkbox indicating if the patient is active.
     *
     * @param set the set
     * @return the patient active status, or {@code null} if no patient is found
     */
    private CheckBox getPatientActive(ObjectSet set) {
        Party patient = (Party) set.get("patient");
        return (patient != null) ? getActive(patient) : null;
    }

    /**
     * Returns the contact description.
     *
     * @param set the set
     * @return the contact description, or {@code null} if none is found
     */
    private String getContact(ObjectSet set) {
        Contact contact = (Contact) set.get("contact");
        return (contact != null) ? contact.getDescription() : null;
    }

    /**
     * Creates the column model.
     *
     * @param showPatient  if {@code true}, display the patient column
     * @param showContact  if {@code true}, display the contact column
     * @param showIdentity if {@code true}, display the identity column
     * @param showActive   if {@code true}, display the active column
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showPatient, boolean showContact, boolean showIdentity,
                                                      boolean showActive) {
        DefaultTableColumnModel model = createTableColumnModel(false, showActive);
        if (showPatient) {
            model.addColumn(createTableColumn(PATIENT_NAME_INDEX, "customerquery.patient.name"));
            model.addColumn(createTableColumn(PATIENT_DESC_INDEX, "customerquery.patient.description"));
            model.addColumn(createTableColumn(PATIENT_ACTIVE_INDEX, "customerquery.patient.active"));
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
