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

package org.openvpms.web.component.im.patient;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.table.AbstractEntityObjectSetTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * Patient table model that can display the owner of a patient.
 *
 * @author Tim Anderson
 */
public class PatientTableModel extends AbstractEntityObjectSetTableModel {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * Determines if the owner should be displayed.
     */
    private boolean showOwner;

    /**
     * Determines if the identity should be displayed.
     */
    private boolean showIdentity;

    /**
     * Determines if the active column should be displayed.
     */
    private boolean showActive;

    /**
     * The owner index.
     */
    private static final int OWNER_INDEX = NEXT_INDEX;


    /**
     * Constructs a {@code PatientTableModel}.
     */
    public PatientTableModel(Context context) {
        super("patient", "identity");
        this.context = context;
        rules = new PatientRules(ServiceHelper.getArchetypeService(), ServiceHelper.getLookupService());
        setTableColumnModel(createTableColumnModel());
    }

    /**
     * Determines which columns should be displayed
     *
     * @param owner    if {@code true} show the patient-owner column
     * @param identity if {@code true} show the identity column
     * @param active   if {@code true} show the active column
     */
    public void showColumns(boolean owner, boolean identity, boolean active) {
        if (owner != showOwner || identity != showIdentity || active != showActive) {
            showOwner = owner;
            showIdentity = identity;
            showActive = active;
            setTableColumnModel(createTableColumnModel());
        }
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        if (showOwner) {
            model.addColumn(createTableColumn(OWNER_INDEX, "patienttablemodel.owner"));
        }
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        if (showIdentity) {
            model.addColumn(createTableColumn(IDENTITY_INDEX, IDENTITY));
        }
        if (showActive) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }
        return model;
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
            case OWNER_INDEX:
                result = getOwner(set);
                break;
            case DESCRIPTION_INDEX:
                result = getDescriptionLabel(set);
                break;
            default:
                result = super.getValue(set, column, row);
                break;
        }
        return result;
    }

    /**
     * Returns the patient description.
     *
     * @param set the set
     * @return the customer description, or {@code null} if none is found
     */
    protected Component getDescriptionLabel(ObjectSet set) {
        Component result;
        Party patient = (Party) getEntity(set);
        Label label = LabelFactory.create();
        label.setText(getDescription(set));
        if (rules.isDeceased(patient)) {
            Label deceased = LabelFactory.create("patient.deceased", "Patient.Deceased");
            result = RowFactory.create("CellSpacing", label, deceased);
        } else {
            result = label;
        }
        return result;
    }

    /**
     * Returns a component to display the owner of a patient.
     *
     * @param set the object set
     * @return a component for the patient's owner, or {@code null} if the patient has no owner
     */
    protected Component getOwner(ObjectSet set) {
        Component result = null;
        Party patient = (Party) getEntity(set);
        if (patient != null) {
            Party owner = rules.getOwner(patient);
            if (owner != null) {
                IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(owner.getObjectReference(), false,
                                                                             context);
                result = viewer.getComponent();
            }
        }
        return result;
    }

}
