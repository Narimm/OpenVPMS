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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.customer;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractEntityObjectSetTableModel;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * A table model that can display customer and associated patient, or either of these.
 *
 * @author Tim Anderson
 */
public abstract class CustomerPatientTableModel extends AbstractEntityObjectSetTableModel {

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * Determines if the customer should be displayed.
     */
    private boolean showCustomer;

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
     * If {@code true}, the active column is displayed for the customer, otherwise it is displayed for the patient.
     */
    private boolean showActiveForCustomer;

    /**
     * Sort by ascending customer id.
     */
    private static final SortConstraint CUSTOMER_ID_ASC = sort("customer.id", true);

    /**
     * Sort by ascending customer name.
     */
    private static final SortConstraint CUSTOMER_NAME_ASC = sort("customer.name", true);

    /**
     * Sort by ascending patient id.
     */
    private static final SortConstraint PATIENT_ID_ASC = sort("patient.id", true);

    /**
     * Sort by ascending name.
     */
    private static final SortConstraint PATIENT_NAME_ASC = sort("patient.name", true);

    /**
     * The patient id index.
     */
    private static final int PATIENT_ID_INDEX = NEXT_INDEX;

    /**
     * The patient name index.
     */
    private static final int PATIENT_NAME_INDEX = PATIENT_ID_INDEX + 1;

    /**
     * The patient description index.
     */
    private static final int PATIENT_DESC_INDEX = PATIENT_NAME_INDEX + 1;

    /**
     * The contact index.
     */
    private static final int CONTACT_INDEX = PATIENT_DESC_INDEX + 1;


    /**
     * Constructs a {@link CustomerPatientTableModel}.
     */
    public CustomerPatientTableModel(boolean showActiveForCustomer) {
        super("customer", "identity");
        this.showActiveForCustomer = showActiveForCustomer;
        patientRules = ServiceHelper.getBean(PatientRules.class);
    }

    /**
     * Determines if the customer, patient, contact and, identity, and/or active columns should be displayed.
     *
     * @param customer if {@code true}, display the customer columns
     * @param patient  if {@code true}, display the patient columns
     * @param contact  if {@code true}, display the contact column
     * @param identity if {@code true}, display the identity column
     * @param active   if {@code true}, display the active column
     */
    public void showColumns(boolean customer, boolean patient, boolean contact, boolean identity, boolean active) {
        if (customer != showCustomer || patient != showPatient || contact != showContact || identity != showIdentity
            || active != showActive) {
            showCustomer = customer;
            showPatient = patient;
            showContact = contact;
            showIdentity = identity;
            showActive = active;
            setTableColumnModel(createTableColumnModel(showCustomer, showPatient, showContact, showIdentity,
                                                       showActive));
        }
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
        SortConstraint[] result = null;
        if (column == ID_INDEX) {
            SortConstraint customer = sort("customer.id", ascending);
            if (showPatient) {
                result = new SortConstraint[]{customer, PATIENT_NAME_ASC, PATIENT_ID_ASC};
            } else {
                result = new SortConstraint[]{customer};
            }
        } else if (column == NAME_INDEX) {
            SortConstraint customer = sort("customer.name", ascending);
            if (showPatient) {
                result = new SortConstraint[]{customer, CUSTOMER_ID_ASC, PATIENT_NAME_ASC, PATIENT_ID_ASC};
            } else {
                result = new SortConstraint[]{customer, CUSTOMER_ID_ASC};
            }
        } else if (column == PATIENT_ID_INDEX) {
            result = new SortConstraint[]{sort("patient.id", ascending), CUSTOMER_NAME_ASC};
        } else if (column == PATIENT_NAME_INDEX) {
            result = new SortConstraint[]{sort("patient.name", ascending), PATIENT_ID_ASC, CUSTOMER_NAME_ASC};
        } else if (column == CONTACT_INDEX) {
            SortConstraint contact = sort("contact.description", ascending);
            if (showPatient) {
                result = new SortConstraint[]{contact, CUSTOMER_NAME_ASC, CUSTOMER_ID_ASC, PATIENT_NAME_ASC,
                                              PATIENT_ID_ASC};
            } else {
                result = new SortConstraint[]{contact, CUSTOMER_NAME_ASC, CUSTOMER_ID_ASC};
            }
        }
        return result;
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
            case PATIENT_ID_INDEX:
                result = getPatientId(set);
                break;
            case PATIENT_NAME_INDEX:
                result = getPatientName(set);
                break;
            case PATIENT_DESC_INDEX:
                result = getPatientDescription(set);
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
     * Creates the column model.
     *
     * @param showCustomer if {@code true}, display the customer columns
     * @param showPatient  if {@code true}, display the patient columns
     * @param showContact  if {@code true}, display the contact column
     * @param showIdentity if {@code true}, display the identity column
     * @param showActive   if {@code true}, display the active column
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showCustomer, boolean showPatient, boolean showContact,
                                                      boolean showIdentity, boolean showActive) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        if (showCustomer) {
            model.addColumn(createTableColumn(ID_INDEX, "customerpatientquery.customer"));
            model.addColumn(createTableColumn(NAME_INDEX, "customerpatientquery.customer.name"));
            model.addColumn(createTableColumn(DESCRIPTION_INDEX, "customerpatientquery.customer.description"));
            if (showActive && showActiveForCustomer) {
                model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
            }
        }
        if (showContact) {
            model.addColumn(createTableColumn(CONTACT_INDEX, "customerpatientquery.contact"));
        }
        if (showPatient) {
            model.addColumn(createTableColumn(PATIENT_ID_INDEX, "customerpatientquery.patient"));
            model.addColumn(createTableColumn(PATIENT_NAME_INDEX, "customerpatientquery.patient.name"));
            model.addColumn(createTableColumn(PATIENT_DESC_INDEX, "customerpatientquery.patient.description"));
            if (showActive && !showActiveForCustomer) {
                model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
            }
        }
        if (showIdentity) {
            model.addColumn(createTableColumn(IDENTITY_INDEX, IDENTITY));
        }
        return model;
    }

    /**
     * Returns a checkbox indicating the entity's active state.
     *
     * @param set the set
     * @return the entity's active state, or {@code null} if no entity exists
     */
    protected CheckBox getActive(ObjectSet set) {
        Entity entity = (showActiveForCustomer) ? getEntity(set) : getPatient(set);
        return (entity != null) ? getActive(entity) : null;
    }

    /**
     * Returns the patient.
     *
     * @param set the set
     * @return the patient. May be {@code null}
     */
    protected Party getPatient(ObjectSet set) {
        return (Party) set.get("patient");
    }

    /**
     * Returns the patient id.
     *
     * @param set the set
     * @return the patient id, or {@code null} if none is found
     */
    private Long getPatientId(ObjectSet set) {
        Party patient = getPatient(set);
        return (patient != null) ? patient.getId() : null;
    }

    /**
     * Returns the patient name.
     *
     * @param set the set
     * @return the patient name, or {@code null} if none is found
     */
    private String getPatientName(ObjectSet set) {
        Party patient = getPatient(set);
        return (patient != null) ? patient.getName() : null;
    }

    /**
     * Returns the patient description.
     *
     * @param set the set
     * @return the patient description, or {@code null} if none is found
     */
    private Component getPatientDescription(ObjectSet set) {
        Component result = null;
        Party patient = getPatient(set);
        if (patient != null) {

            Label label = LabelFactory.create();
            label.setText(patient.getDescription());
            if (patientRules.isDeceased(patient)) {
                Label deceased = LabelFactory.create("patient.deceased", "Patient.Deceased");
                result = RowFactory.create(Styles.CELL_SPACING, label, deceased);
            } else {
                result = label;
            }
        }

        return result;
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
}
