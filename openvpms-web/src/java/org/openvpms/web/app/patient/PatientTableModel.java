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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientTableModel extends BaseIMObjectTableModel<Party> {

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * Determines if the owner should be displayed.
     */
    private boolean showOwner;

    /**
     * The owner model index.
     */
    private static final int OWNER_INDEX = NEXT_INDEX;


    /**
     * Constructs a new <tt>PatientTableModel</tt>.
     *
     * @param showOwner if <code>true</code> display the owner
     */
    public PatientTableModel(boolean showOwner) {
        super(null);
        this.showOwner = showOwner;
        rules = new PatientRules();
        setTableColumnModel(createTableColumnModel());
    }

    /**
     * Determines if the patient-owner column should be displayed.
     *
     * @param showOwner if <code>true</code> show the patient-owner column
     */
    public void setShowOwner(boolean showOwner) {
        if (showOwner != this.showOwner) {
            this.showOwner = showOwner;
            setTableColumnModel(createTableColumnModel());
        }
    }

    /**
     * Determines if the patient-owner column should be displayed.
     *
     * @return <code>true</code> if the patient-owner column should be displayed
     */
    public boolean isShowOwner() {
        return showOwner;
    }


    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(NAME_INDEX, "table.imobject.name"));
        if (showOwner) {
            model.addColumn(
                    createTableColumn(OWNER_INDEX, "patienttablemodel.owner"));
        }
        model.addColumn(createTableColumn(DESCRIPTION_INDEX,
                                          "table.imobject.description"));
        return model;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(Party object, TableColumn column, int row) {
        if (column.getModelIndex() == OWNER_INDEX) {
            Party owner = rules.getOwner(object);
            IMObjectReference ref = (owner != null)
                    ? owner.getObjectReference() : null;
            IMObjectReferenceViewer viewer
                    = new IMObjectReferenceViewer(ref, false);
            return viewer.getComponent();
        } else if (column.getModelIndex() == DESCRIPTION_INDEX
                && rules.isDeceased(object)) {
            String description = object.getDescription();
            Label label = LabelFactory.create();
            label.setText(description);
            Label deceased = LabelFactory.create("patient.deceased",
                                                 "Patient.Deceased");
            return RowFactory.create("CellSpacing", label, deceased);
        } else {
            return super.getValue(object, column, row);
        }
    }

}
