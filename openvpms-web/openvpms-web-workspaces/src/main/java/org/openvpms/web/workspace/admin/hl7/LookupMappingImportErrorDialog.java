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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Dialog to report lookup mapping import errors.
 *
 * @author Tim Anderson
 */
public class LookupMappingImportErrorDialog extends ModalDialog {

    /**
     * Constructs a {@link LookupMappingImportErrorDialog}.
     *
     * @param errors the import errors
     * @param help   the help context
     */
    public LookupMappingImportErrorDialog(List<LookupMapping> errors, HelpContext help) {
        super(Messages.get("admin.hl7.mapping.import.error.title"), "BrowserDialog", OK, help);

        ResultSet<LookupMapping> resultSet = new ListResultSet<>(errors, 20);
        PagedIMTableModel<LookupMapping, LookupMapping> model = new PagedIMTableModel<>(new ErrorTableModel());
        PagedIMTable<LookupMapping> table = new PagedIMTable<>(model, resultSet);
        Label message = LabelFactory.create("admin.hl7.mapping.import.error.message");
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET,
                                             ColumnFactory.create(Styles.WIDE_CELL_SPACING, message,
                                                                  table.getComponent())));
    }

    private static class ErrorTableModel extends AbstractIMTableModel<LookupMapping> {

        private static final int FROM_TYPE = 0;
        private static final int FROM_CODE = FROM_TYPE + 1;
        private static final int FROM_NAME = FROM_CODE + 1;
        private static final int TO_TYPE = FROM_NAME + 1;
        private static final int TO_CODE = TO_TYPE + 1;
        private static final int TO_NAME = TO_CODE + 1;
        private static final int LINE = TO_NAME + 1;
        private static final int ERROR = LINE + 1;

        public ErrorTableModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(FROM_TYPE, "admin.hl7.mapping.import.fromType"));
            model.addColumn(createTableColumn(FROM_CODE, "admin.hl7.mapping.import.fromCode"));
            model.addColumn(createTableColumn(FROM_NAME, "admin.hl7.mapping.import.fromName"));
            model.addColumn(createTableColumn(TO_TYPE, "admin.hl7.mapping.import.toType"));
            model.addColumn(createTableColumn(TO_CODE, "admin.hl7.mapping.import.toCode"));
            model.addColumn(createTableColumn(TO_NAME, "admin.hl7.mapping.import.toName"));
            model.addColumn(createTableColumn(LINE, "admin.hl7.mapping.import.line"));
            model.addColumn(createTableColumn(ERROR, "admin.hl7.mapping.import.error"));
            setTableColumnModel(model);
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
        protected Object getValue(LookupMapping object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case FROM_TYPE:
                    result = object.getFromType();
                    break;
                case FROM_CODE:
                    result = object.getFromCode();
                    break;
                case FROM_NAME:
                    result = object.getFromName();
                    break;
                case TO_TYPE:
                    result = object.getToType();
                    break;
                case TO_CODE:
                    result = object.getToCode();
                    break;
                case TO_NAME:
                    result = object.getToName();
                    break;
                case LINE:
                    result = object.getLine();
                    break;
                case ERROR:
                    result = object.getError();
                    break;
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
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }
    }

}
