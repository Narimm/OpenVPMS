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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.vet;

import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractEntityBrowser;
import org.openvpms.web.component.im.table.NameDescObjectSetTableModel;

/**
 * Browser for <em>party.supplierVeterinarian</em> parties.
 * <p/>
 * This displays the vet's current practice.
 *
 * @author Tim Anderson
 */
public class VetBrowser extends AbstractEntityBrowser<Party> {

    /**
     * Constructs a {@link VetBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public VetBrowser(VetQuery query, LayoutContext context) {
        super(query, context, new VetTableModel());
    }


    private static class VetTableModel extends NameDescObjectSetTableModel {

        /**
         * Practice column index.
         */
        private static final int PRACTICE_INDEX = ACTIVE_INDEX + 1;

        /**
         * Constructs a {@link VetTableModel}.
         */
        public VetTableModel() {
            super("entity", false, false);
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param set    the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(ObjectSet set, TableColumn column, int row) {
            Object result;
            if (column.getModelIndex() == PRACTICE_INDEX) {
                result = set.getString("practice.name");
            } else {
                result = super.getValue(set, column, row);
            }
            return result;
        }

        /**
         * Creates the column model.
         *
         * @param showArchetype if {@code true} show the archetype
         * @return a new column model
         */
        @Override
        protected TableColumnModel createTableColumnModel(boolean showArchetype, boolean showActive) {
            TableColumnModel model = super.createTableColumnModel(showArchetype, showActive);
            model.addColumn(createTableColumn(PRACTICE_INDEX, "supplier.vet.practice"));
            if (showActive) {
                model.moveColumn(model.getColumnCount() - 1, getColumnOffset(model, ACTIVE_INDEX));
            }
            return model;
        }
    }
}
