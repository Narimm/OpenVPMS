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

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Table model for {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @see IMObjectTable
 */
public abstract class BaseIMObjectTableModel<T extends IMObject>
        extends AbstractIMObjectTableModel<T> {

    /**
     * Archetype column index.
     */
    public static final int ARCHETYPE_INDEX = 0;

    /**
     * Name column index.
     */
    public static final int NAME_INDEX = 1;

    /**
     * Description column index.
     */
    public static final int DESCRIPTION_INDEX = 2;

    /**
     * Next unused model index.
     */
    public static final int NEXT_INDEX = 3;


    /**
     * Table column identifiers.
     */
    private static final String[] COLUMNS = {
            "table.imobject.archetype", "table.imobject.name",
            "table.imobject.description"};


    /**
     * Constructs a new <code>BaseIMObjectTableModel</code>, using
     * a new column model created by {@link #createTableColumnModel}.
     */
    public BaseIMObjectTableModel() {
        setTableColumnModel(createTableColumnModel());
    }

    /**
     * Construct a new <code>BaseIMObjectTableModel</code>.
     *
     * @param model the column model. May be <code>null</code>
     */
    public BaseIMObjectTableModel(TableColumnModel model) {
        super(model);
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel() {
        return createTableColumnModel(false);
    }

    /**
     * Creates a new column model.
     *
     * @param showArchetype if <code>true</code> show the archetype
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showArchetype) {
        TableColumnModel model = new DefaultTableColumnModel();
        return createTableColumnModel(showArchetype, model);
    }

    /**
     * Adds columns to an existing model.
     *
     * @param showArchetype if <code>true</code> show the archetype
     * @param model         the column model
     * @return the column model
     */
    protected TableColumnModel createTableColumnModel(boolean showArchetype,
                                                      TableColumnModel model) {
        int index = (showArchetype) ? 0 : 1;
        for (int i = index; i < COLUMNS.length; ++i) {
            model.addColumn(createTableColumn(i, COLUMNS[i]));
        }
        return model;
    }

    /**
     * @see TableModel#getColumnName
     */
    public String getColumnName(int column) {
        TableColumn col = getColumn(column);
        Object value = col.getHeaderValue();
        return (value != null) ? value.toString() : null;
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
            default:
                throw new IllegalArgumentException("Illegal column=" + column);
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria, or <code>null</code> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        switch (column) {
            case ARCHETYPE_INDEX:
                ArchetypeSortConstraint archetype
                        = new ArchetypeSortConstraint(ascending);
                result = new SortConstraint[]{archetype};
                break;
            case NAME_INDEX:
                SortConstraint name = new NodeSortConstraint("name", ascending);
                result = new SortConstraint[]{name};
                break;
            case DESCRIPTION_INDEX:
                SortConstraint description = new NodeSortConstraint(
                        "description", ascending);
                result = new SortConstraint[]{description};
                break;
            default:
                result = null;
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

}