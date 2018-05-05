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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.print;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Dialog that enables a set of objects to be selected for printing.
 *
 * @author Tim Anderson
 */
public class BatchPrintDialog extends PopupDialog {

    /**
     * The message to display. May be {@code null}
     */
    private final String message;

    /**
     * The table of objects to print.
     */
    private final IMTable<IMObject> table;


    /**
     * Constructs a {@link BatchPrintDialog}.
     *
     * @param title   the window title
     * @param objects the objects to print
     * @param help    the help context
     */
    public BatchPrintDialog(String title, List<IMObject> objects, HelpContext help) {
        this(title, null, OK_CANCEL, objects, help);
    }

    /**
     * Constructs a {@link BatchPrintDialog}.
     *
     * @param title   the window title
     * @param message the message to display. May be {@code null}
     * @param buttons the buttons to display
     * @param objects the objects to print
     * @param help    the help context
     */
    public BatchPrintDialog(String title, String message, String[] buttons, List<IMObject> objects, HelpContext help) {
        super(title, buttons, help);
        this.message = message;
        setModal(true);
        table = new IMObjectTable<>(new PrintTableModel());
        table.setObjects(objects);
    }

    /**
     * Constructs a {@link BatchPrintDialog}.
     *
     * @param title   the window title
     * @param message the message to display. May be {@code null}
     * @param buttons the buttons to display
     * @param objects the objects to print. The boolean value indicates if the object should be selected by default
     * @param help    the help context
     */
    public BatchPrintDialog(String title, String message, String[] buttons, Map<IMObject, Boolean> objects,
                            HelpContext help) {
        super(title, buttons, help);
        this.message = message;
        setModal(true);
        table = new IMObjectTable<>(new PrintTableModel(new ArrayList<>(objects.values())));
        table.setObjects(new ArrayList<>(objects.keySet()));
    }

    /**
     * Returns the selected objects.
     *
     * @return the selected objects
     */
    public List<IMObject> getSelected() {
        PrintTableModel model = (PrintTableModel) table.getModel();
        return model.getSelected();
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes the window if objects are selected.
     */
    @Override
    protected void onOK() {
        if (!getSelected().isEmpty()) {
            super.onOK();
        }
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING);
        if (message != null) {
            Label label = LabelFactory.create(true, true);
            label.setStyleName(Styles.BOLD);
            label.setText(message);
            column.add(label);
        }
        column.add(table);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    private static class PrintTableModel extends BaseIMObjectTableModel<IMObject> {

        /**
         * The print column.
         */
        private final int PRINT_INDEX = NEXT_INDEX;

        /**
         * The print check boxes.
         */
        private List<CheckBox> print = new ArrayList<>();

        /**
         * Determines the initial selections.
         */
        private List<Boolean> selections;


        /**
         * Constructs a {@code PrintTableModel}.
         */
        public PrintTableModel() {
            this(null);
        }

        /**
         * Constructs a {@code PrintTableModel}.
         *
         * @param selections the intial selections. May be {@code null}
         */
        public PrintTableModel(List<Boolean> selections) {
            super(null);
            setTableColumnModel(createTableColumnModel(true));
            this.selections = selections;
        }

        /**
         * Returns the list of objects selected for printing.
         *
         * @return the objects to print
         */
        public List<IMObject> getSelected() {
            List<IMObject> result = new ArrayList<>();
            for (int i = 0; i < print.size(); ++i) {
                CheckBox check = print.get(i);
                if (check.isSelected()) {
                    result.add(getObject(i));
                }
            }
            return result;
        }

        /**
         * Sets the objects to display.
         *
         * @param objects the objects to display
         */
        @Override
        public void setObjects(List<IMObject> objects) {
            super.setObjects(objects);
            print = new ArrayList<>();
            int size = objects.size();
            for (int i = 0; i < size; ++i) {
                boolean selected = (selections != null && i < selections.size()) ? selections.get(i) : true;
                print.add(CheckBoxFactory.create(selected));
            }
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
        protected Object getValue(IMObject object, TableColumn column, int row) {
            Object result;
            if (column.getModelIndex() == PRINT_INDEX) {
                result = print.get(row);
            } else if (column.getModelIndex() == DESCRIPTION_INDEX) {
                String description = object.getDescription();
                if (StringUtils.isEmpty(description) && object instanceof DocumentAct) {
                    ActBean bean = new ActBean((Act) object);
                    Entity template = bean.getParticipant("participation.documentTemplate");
                    if (template != null) {
                        description = template.getName();
                    }
                }
                result = description;
            } else {
                result = super.getValue(object, column, row);
            }
            return result;
        }

        /**
         * Creates a new column model.
         *
         * @param showId        if {@code true}, show the ID
         * @param showArchetype if {@code true} show the archetype
         * @return a new column model
         */
        protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype) {
            TableColumnModel model = new DefaultTableColumnModel();
            TableColumn column = createTableColumn(PRINT_INDEX, "batchprintdialog.print");
            model.addColumn(column);
            return super.createTableColumnModel(showId, showArchetype, model);
        }

    }
}
