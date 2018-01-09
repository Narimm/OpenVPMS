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

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.insurance.service.Changes;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Displays changes to insurers.
 *
 * @author Tim Anderson
 */
public class InsurerChanges extends ConfirmationDialog {

    /**
     * The changes.
     */
    private final List<Changes.Change<Party>> changes;

    /**
     * Constructs an {@link InsurerChanges}.
     */
    public InsurerChanges(List<Changes.Change<Party>> changes) {
        super(Messages.get("admin.organisation.insurer.sync.title"),
              Messages.get("admin.organisation.insurer.sync.message"), OK);
        this.changes = changes;
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create();
        message.setText(getMessage());
        ResultSet<Changes.Change<Party>> set = new ListResultSet<>(changes, 20);

        InsurerModel model = new InsurerModel();
        PagedIMTable<Changes.Change<Party>> table = new PagedIMTable<>(model);
        table.setResultSet(set);

        // need to render the table in a grid in order for it to grow to the width of the window
        Grid grid = GridFactory.create(1, message, TableHelper.createSpacer(), table.getComponent());
        grid.setWidth(Styles.FULL_WIDTH);
        getLayout().add(grid);
    }

    private static class InsurerModel extends AbstractIMTableModel<Changes.Change<Party>> {

        private static final int ID_INDEX = 0;

        private static final int NAME_IDEX = 1;

        private static final int STATUS_IDEX = 2;

        public InsurerModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(ID_INDEX, ID));
            model.addColumn(createTableColumn(NAME_IDEX, NAME));
            model.addColumn(createTableColumn(STATUS_IDEX, "admin.organisation.insurer.sync.status"));
            setTableColumnModel(model);
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
            return null;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Changes.Change<Party> object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case ID_INDEX:
                    result = object.getObject().getId();
                    break;
                case NAME_IDEX:
                    result = object.getObject().getName();
                    break;
                case STATUS_IDEX:
                    switch (object.getState()) {
                        case ADDED:
                            result = Messages.get("admin.organisation.insurer.sync.added");
                            break;
                        case UPDATED:
                            result = Messages.get("admin.organisation.insurer.sync.updated");
                            break;
                        case DEACTIVATED:
                            result = Messages.get("admin.organisation.insurer.sync.deactivated");
                            break;
                    }
            }
            return result;
        }
    }

}
