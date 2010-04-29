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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
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
 * An object set table model that displays the ID, archetype, name and description.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NameDescObjectSetTableModel
        extends AbstractIMTableModel<ObjectSet> {

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
     * Determines if the archetype column should be displayed.
     */
    private boolean showArchetype;

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
     * Creates a new <tt>NameDescObjectSetTableModel</tt>.
     */
    public NameDescObjectSetTableModel() {
        this(null, false);
    }

    /**
     * Creates a new <tt>NameDescObjectSetTableModel</tt>.
     *
     * @param alias         the object alias, used to prefix node names. May be <tt>null</tt>
     * @param showArchetype if <tt>true</tt> show the archetype
     */
    public NameDescObjectSetTableModel(String alias, boolean showArchetype) {
        this.showArchetype = showArchetype;
        setTableColumnModel(createTableColumnModel(showArchetype));
        if (alias != null) {
            reference = alias + ".reference";
            name = alias + ".name";
            description = alias + ".description";
        } else {
            reference = "reference";
            name = "name";
            description = "description";
        }
    }

    /**
     * Determines if the archetype columns should be displayed.
     *
     * @param show if <tt>true</tt> show the archetype
     */
    public void showArchetype(boolean show) {
        if (show != showArchetype) {
            showArchetype = show;
            setTableColumnModel(createTableColumnModel(showArchetype));
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
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise
     *                  sort in <tt>descending</tt> order
     * @return the sort criteria, or <tt>null</tt> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint result = null;
        if (column == ID_INDEX) {
            result = new NodeSortConstraint("id", ascending);
        } else if (column == NAME_INDEX) {
            result = new NodeSortConstraint("name", ascending);
        } else if (column == DESCRIPTION_INDEX) {
            result = new NodeSortConstraint("description", ascending);
        }
        return (result != null) ? new SortConstraint[]{result} : null;
    }

    /**
     * Creates the column model.
     *
     * @param showArchetype if <tt>true</tt> show the archetype
     * @return a new column model
     */
    private static TableColumnModel createTableColumnModel(boolean showArchetype) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        if (showArchetype) {
            model.addColumn(createTableColumn(ARCHETYPE_INDEX, ARCHETYPE));
        }
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        return model;
    }
}
