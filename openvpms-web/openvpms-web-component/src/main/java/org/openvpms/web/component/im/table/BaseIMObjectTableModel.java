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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;


/**
 * Table model for {@link IMObject}s.
 *
 * @author Tim Anderson
 * @see IMObjectTable
 */
public abstract class BaseIMObjectTableModel<T extends IMObject>
        extends AbstractIMObjectTableModel<T> {

    /**
     * Mark column index.
     */
    public static final int MARK_INDEX = 0;

    /**
     * ID column index.
     */
    public static final int ID_INDEX = 1;

    /**
     * Archetype column index.
     */
    public static final int ARCHETYPE_INDEX = 2;

    /**
     * Name column index.
     */
    public static final int NAME_INDEX = 3;

    /**
     * Description column index.
     */
    public static final int DESCRIPTION_INDEX = 4;

    /**
     * Active column index.
     */
    public static final int ACTIVE_INDEX = 5;

    /**
     * Next unused model index.
     */
    public static final int NEXT_INDEX = 6;

    /**
     * Row marks.
     */
    private CheckBox[] rowMarks;

    /**
     * Constructs a new {@code BaseIMObjectTableModel}, using
     * a new column model created by {@link #createTableColumnModel}.
     */
    public BaseIMObjectTableModel() {
        setTableColumnModel(createTableColumnModel());
    }

    /**
     * Construct a new {@code BaseIMObjectTableModel}.
     *
     * @param model the column model. May be {@code null}
     */
    public BaseIMObjectTableModel(TableColumnModel model) {
        super(model);
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
        switch (column) {
            case ID_INDEX:
                SortConstraint id = new NodeSortConstraint("id", ascending);
                result = new SortConstraint[]{id};
                break;
            case ARCHETYPE_INDEX:
                ArchetypeSortConstraint archetype = new ArchetypeSortConstraint(ascending);
                result = new SortConstraint[]{archetype};
                break;
            case NAME_INDEX:
                SortConstraint name = new NodeSortConstraint("name", ascending);
                result = new SortConstraint[]{name};
                break;
            case DESCRIPTION_INDEX:
                SortConstraint description = new NodeSortConstraint("description", ascending);
                result = new SortConstraint[]{description};
                break;
            case ACTIVE_INDEX:
                SortConstraint active = new NodeSortConstraint("active", ascending);
                result = new SortConstraint[]{active};
                break;
            default:
                result = null;
        }
        return result;
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    @Override
    public void setObjects(List<T> objects) {
        super.setObjects(objects);
        rowMarks = null;
    }

    /**
     * Creates a new column model.
     * <p/>
     * This implementation suppresses the archetype and ID columns.
     *
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel() {
        return createTableColumnModel(false);
    }

    /**
     * Creates a new column model.
     * <p/>
     * This implementation suppresses the ID column.
     *
     * @param showArchetype if {@code true} show the archetype
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showArchetype) {
        return createTableColumnModel(false, showArchetype);
    }

    /**
     * Creates a new column model.
     *
     * @param showId        if {@code true}, show the ID
     * @param showArchetype if {@code true} show the archetype
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype) {
        return createTableColumnModel(showId, showArchetype, false);
    }

    /**
     * Creates a new column model.
     *
     * @param showId        if {@code true}, show the ID
     * @param showArchetype if {@code true} show the archetype
     * @param showActive    if {@code true} show the active status
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype, boolean showActive) {
        TableColumnModel model = new DefaultTableColumnModel();
        return createTableColumnModel(showId, showArchetype, showActive, model);
    }

    /**
     * Creates a new column model.
     *
     * @param showId          if {@code true}, show the ID
     * @param showArchetype   if {@code true} show the archetype
     * @param showName        if {@code true} show the name
     * @param showDescription if {@code true} show the description
     * @param showActive      if {@code true} show the active status
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype, boolean showName,
                                                      boolean showDescription, boolean showActive) {
        TableColumnModel model = new DefaultTableColumnModel();
        return createTableColumnModel(showId, showArchetype, showName, showDescription, showActive, model);
    }

    /**
     * Adds columns to an existing model.
     *
     * @param showId        if {@code true}, show the ID
     * @param showArchetype if {@code true} show the archetype
     * @param model         the column model
     * @return the column model
     */
    protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype, TableColumnModel model) {
        return createTableColumnModel(showId, showArchetype, false, model);
    }

    /**
     * Adds columns to an existing model.
     *
     * @param showId        if {@code true}, show the ID
     * @param showArchetype if {@code true} show the archetype
     * @param model         the column model
     * @return the column model
     */
    protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype, boolean showActive,
                                                      TableColumnModel model) {
        return createTableColumnModel(showId, showArchetype, true, true, showActive, model);
    }

    /**
     * Adds columns to an existing model.
     *
     * @param showId          if {@code true}, show the ID
     * @param showArchetype   if {@code true} show the archetype
     * @param showName        if {@code true} show the name
     * @param showDescription if {@code true} show the description
     * @param showActive      if {@code true} show the active status
     * @param model           the column model
     * @return the column model
     */
    protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype, boolean showName,
                                                      boolean showDescription, boolean showActive,
                                                      TableColumnModel model) {
        if (showId) {
            model.addColumn(createTableColumn(ID_INDEX, ID));
        }
        if (showArchetype) {
            model.addColumn(createTableColumn(ARCHETYPE_INDEX, ARCHETYPE));
        }
        if (showName) {
            model.addColumn(createTableColumn(NAME_INDEX, NAME));
        }
        if (showDescription) {
            model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        }
        if (showActive) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }
        return model;
    }

    /**
     * Adds a mark column.
     *
     * @param model the column model
     */
    protected void addMarkColumn(TableColumnModel model) {
        model.addColumn(new TableColumn(MARK_INDEX));
        model.moveColumn(model.getColumnCount() - 1, 0);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate
     */
    protected Object getValue(T object, TableColumn column, int row) {
        Object result;
        switch (column.getModelIndex()) {
            case MARK_INDEX:
                result = getRowMark(row);
                break;
            case ID_INDEX:
                result = object.getId();
                break;
            case ARCHETYPE_INDEX:
                result = DescriptorHelper.getDisplayName(object);
                break;
            case NAME_INDEX:
                result = object.getName();
                if (result == null) {
                    Label label = LabelFactory.create();
                    label.setText(Messages.get("imobject.none"));
                    result = label;
                }
                break;
            case DESCRIPTION_INDEX:
                result = object.getDescription();
                break;
            case ACTIVE_INDEX:
                CheckBox active = new CheckBox();
                active.setEnabled(false);
                active.setSelected(object.isActive());
                result = active;
                break;
            default:
                throw new IllegalArgumentException("Illegal column=" + column);
        }
        return result;
    }

    /**
     * Helper to determine the next available model index.
     *
     * @param columns the columns
     * @return the next available model index.
     */
    @Override
    protected int getNextModelIndex(TableColumnModel columns) {
        return getNextModelIndex(columns, NEXT_INDEX);
    }

    /**
     * Returns a component to mark the row,
     *
     * @param row the row
     * @return a component to mark the row, or {@code null} if no row mark model has been registered
     */
    protected CheckBox getRowMark(int row) {
        CheckBox result = null;
        ListMarkModel model = getRowMarkModel();
        if (model != null) {
            if (rowMarks == null) {
                rowMarks = new CheckBox[getRowCount()];
            }
            result = rowMarks[row];
            if (result == null) {
                CheckBox checkBox;
                if (model.canMark(row)) {
                    checkBox = CheckBoxFactory.create(model.isMarked(row));
                    checkBox.addPropertyChangeListener(evt -> markRow(row, checkBox));
                    checkBox.addActionListener(new ActionListener() {
                        @Override
                        public void onAction(ActionEvent event) {
                        }
                    });
                    rowMarks[row] = checkBox;
                } else {
                    checkBox = CheckBoxFactory.create(false);
                    checkBox.setEnabled(false);
                }
                TableLayoutData layout = new TableLayoutData();
                layout.setAlignment(Alignment.ALIGN_CENTER);
                checkBox.setLayoutData(layout);
                result = checkBox;
            }
        }
        return result;
    }

    /**
     * Invoked when a row is marked or unmarked via the {@link #setRowMarkModel(ListMarkModel) row mark model}.
     * <p/>
     * Updates the corresponding checkbox.
     *
     * @param row    the row
     * @param marked if {@code true}, row has been marked, otherwise it has been unmarked
     */
    @Override
    protected void markRow(int row, boolean marked) {
        if (rowMarks != null) {
            CheckBox checkBox = getRowMark(row);
            if (checkBox.isSelected() != marked) {
                checkBox.setSelected(marked);
            }
        }
    }

    /**
     * Invoked when all rows are unmarked.
     */
    @Override
    protected void unmarkRows() {
        rowMarks = null;
        fireTableDataChanged();
    }

    /**
     * Updates the {@link #setRowMarkModel(ListMarkModel) row mark model} when the row check box changes.
     *
     * @param row      the row
     * @param checkBox if selected, mark the row, otherwise unmark it
     */
    protected void markRow(int row, CheckBox checkBox) {
        ListMarkModel model = getRowMarkModel();
        if (model != null) {
            model.setMarked(row, checkBox.isSelected());
        }
    }

}