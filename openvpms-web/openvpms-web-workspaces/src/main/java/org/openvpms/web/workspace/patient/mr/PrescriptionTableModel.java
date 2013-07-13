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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.system.ServiceHelper;

/**
 * A table model for <em>act.patientPrescription</em> acts.
 *
 * @author Tim Anderson
 */
public class PrescriptionTableModel extends DescriptorTableModel<Act> {

    /**
     * The prescription rules.
     */
    private final PrescriptionRules rules;

    /**
     * The model index of no. of "Times Dispensed" column.
     */
    private int dispensedIndex;

    /**
     * Constructs a {@link PrescriptionTableModel}.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public PrescriptionTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
        rules = ServiceHelper.getBean(PrescriptionRules.class);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(Act object, TableColumn column, int row) {
        Object result;
        if (column.getModelIndex() == dispensedIndex) {
            result = rules.getDispensed(object);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Creates a column model.
     * <p/>
     * This adds a "Times Dispensed" column after the "Repeats" column.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(shortNames, context);
        dispensedIndex = getNextModelIndex(model);
        model.addColumn(createTableColumn(dispensedIndex, "patient.prescription.dispensed"));
        model.moveColumn(model.getColumnCount() - 1, getColumnOffset(model, "repeats") + 1);
        return model;
    }
}
