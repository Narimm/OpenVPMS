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

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * An object set table model that displays the ID, archetype, name, description and active status.
 *
 * @author Tim Anderson
 */
public class NameDescObjectSetTableModel extends AbstractIMTableModel<ObjectSet> {

    /**
     * The reference key.
     */
    private final String reference;

    /**
     * The name key.
     */
    private final String name;

    /**
     * The description key.
     */
    private final String description;

    /**
     * The active key.
     */
    private final String active;

    /**
     * Determines if the archetype column should be displayed.
     */
    private boolean showArchetype;

    /**
     * Determines if the active column should be displayed.
     */
    private boolean showActive;

    /**
     * The ID column index.
     */
    private static final int ID_INDEX = 0;

    /**
     * The archetype column index.
     */
    private static final int ARCHETYPE_INDEX = 1;

    /**
     * The name index.
     */
    private static final int NAME_INDEX = 2;

    /**
     * The description index.
     */
    private static final int DESCRIPTION_INDEX = 3;

    /**
     * The active index.
     */
    private static final int ACTIVE_INDEX = 4;


    /**
     * Constructs a {@link NameDescObjectSetTableModel}.
     */
    public NameDescObjectSetTableModel() {
        this(null, false, false);
    }

    /**
     * Constructs a {@link NameDescObjectSetTableModel}.
     *
     * @param alias         the object alias, used to prefix node names. May be {@code null}
     * @param showArchetype if {@code true} show the archetype
     * @param showActive    if  {@code true} show the active status
     */
    public NameDescObjectSetTableModel(String alias, boolean showArchetype, boolean showActive) {
        this.showArchetype = showArchetype;
        setTableColumnModel(createTableColumnModel(showArchetype, showActive));
        if (alias != null) {
            reference = alias + ".reference";
            name = alias + ".name";
            description = alias + ".description";
            active = alias + ".active";
        } else {
            reference = "reference";
            name = "name";
            description = "description";
            active = "active";
        }
    }

    /**
     * Determines if the archetype columns should be displayed.
     *
     * @param show if {@code true} show the archetype
     */
    public void showArchetype(boolean show) {
        if (show != showArchetype) {
            showArchetype = show;
            setTableColumnModel(createTableColumnModel(showArchetype, showActive));
        }
    }

    /**
     * Determines if the active column should be displayed.
     *
     * @param show if {@code true} show the active column
     */
    public void setShowActive(boolean show) {
        if (show != showActive) {
            showActive = show;
            setTableColumnModel(createTableColumnModel(showArchetype, showActive));
        }
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
        Object result = null;
        int index = column.getModelIndex();
        switch (index) {
            case ID_INDEX:
                result = set.getReference(reference).getId();
                break;
            case ARCHETYPE_INDEX:
                IMObjectReference ref = set.getReference(reference);
                String shortName = ref.getArchetypeId().getShortName();
                result = DescriptorHelper.getDisplayName(shortName);
                break;
            case NAME_INDEX:
                result = set.getString(name);
                break;
            case DESCRIPTION_INDEX:
                result = set.getString(description);
                break;
            case ACTIVE_INDEX:
                result = getCheckBox(set.getBoolean(active));
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
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint result = null;
        if (column == ID_INDEX) {
            result = new NodeSortConstraint("id", ascending);
        } else if (column == NAME_INDEX) {
            result = new NodeSortConstraint("name", ascending);
        } else if (column == DESCRIPTION_INDEX) {
            result = new NodeSortConstraint("description", ascending);
        } else if (column == ACTIVE_INDEX) {
            result = new NodeSortConstraint("active", ascending);
        }
        return (result != null) ? new SortConstraint[]{result} : null;
    }

    /**
     * Creates the column model.
     *
     * @param showArchetype if {@code true} show the archetype
     * @return a new column model
     */
    private static TableColumnModel createTableColumnModel(boolean showArchetype, boolean showActive) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        if (showArchetype) {
            model.addColumn(createTableColumn(ARCHETYPE_INDEX, ARCHETYPE));
        }
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        if (showActive) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }
        return model;
    }
}
