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

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * An object set table model that displays {@link Entity} details.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEntityObjectSetTableModel extends AbstractIMTableModel<ObjectSet> {

    /**
     * The entity key.
     */
    private final String entityKey;

    /**
     * The identity key.
     */
    private final String identityKey;

    /**
     * The ID index.
     */
    public static final int ID_INDEX = 0;

    /**
     * The name index.
     */
    public static final int NAME_INDEX = 1;

    /**
     * The description index.
     */
    public static final int DESCRIPTION_INDEX = 2;

    /**
     * The identity index.
     */
    public static final int IDENTITY_INDEX = 3;

    /**
     * The active index.
     */
    public static final int ACTIVE_INDEX = 4;

    /**
     * The next index.
     */
    protected static final int NEXT_INDEX = ACTIVE_INDEX + 1;

    /**
     * The identity column message key.
     */
    protected static final String IDENTITY = "table.entity.identity";


    /**
     * Constructs a {@code AbstractEntityObjectSetTableModel}.
     * <p/>
     * The subclass is responsible for creating the table column model.
     *
     * @param entityKey   the key of the entity in the object set
     * @param identityKey the key of the entity identity in the object sett
     */
    public AbstractEntityObjectSetTableModel(String entityKey, String identityKey) {
        this.entityKey = entityKey;
        this.identityKey = identityKey;
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
                result = getId(set);
                break;
            case NAME_INDEX:
                result = getName(set);
                break;
            case DESCRIPTION_INDEX:
                result = getDescription(set);
                break;
            case IDENTITY_INDEX:
                result = getIdentity(set);
                break;
            case ACTIVE_INDEX:
                result = getActive(set);
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
            result = new NodeSortConstraint(entityKey, "id", ascending);
        } else if (column == NAME_INDEX) {
            result = new NodeSortConstraint(entityKey, "name", ascending);
        } else if (column == DESCRIPTION_INDEX) {
            result = new NodeSortConstraint(entityKey, "description", ascending);
        } else if (column == ACTIVE_INDEX) {
            result = new NodeSortConstraint(entityKey, "active", ascending);
        }
        return (result != null) ? new SortConstraint[]{result} : null;
    }

    /**
     * Returns the entity from a set.
     *
     * @param set the set
     * @return the entity, or {@code null} if none is found
     */
    protected Entity getEntity(ObjectSet set) {
        return (Entity) set.get(entityKey);
    }

    /**
     * Returns the entity ID.
     *
     * @param set the set
     * @return the entity ID, or {@code null} if none is found
     */
    protected Long getId(ObjectSet set) {
        Entity entity = getEntity(set);
        return (entity != null) ? entity.getId() : null;
    }

    /**
     * Returns the entity name.
     *
     * @param set the set
     * @return the entity name, or {@code null} if none is found
     */
    protected String getName(ObjectSet set) {
        Entity entity = getEntity(set);
        return (entity != null) ? entity.getName() : null;
    }

    /**
     * Returns the customer description.
     *
     * @param set the set
     * @return the customer description, or {@code null} if none is found
     */
    protected String getDescription(ObjectSet set) {
        Entity entity = getEntity(set);
        return (entity != null) ? entity.getDescription() : null;
    }

    /**
     * Returns the identity.
     *
     * @param set the set
     * @return the identity, or {@code null} if none is found
     */
    protected String getIdentity(ObjectSet set) {
        EntityIdentity identity = (EntityIdentity) set.get(identityKey);
        return (identity != null) ? identity.getName() : null;
    }

    /**
     * Returns a checkbox indicating the entity's active state.
     *
     * @param set the set
     * @return the entity's active state, or {@code null} if no entity exists
     */
    protected CheckBox getActive(ObjectSet set) {
        Entity entity = getEntity(set);
        return (entity != null) ? getActive(entity) : null;
    }

    /**
     * Creates the column model.
     *
     * @param showIdentity if {@code true} display the identity column
     * @param showActive   if {@code true}, show the active column
     * @return a new column model
     */
    protected static DefaultTableColumnModel createTableColumnModel(boolean showIdentity, boolean showActive) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        if (showIdentity) {
            model.addColumn(createTableColumn(IDENTITY_INDEX, IDENTITY));
        }
        if (showActive) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }

        return model;
    }

}
