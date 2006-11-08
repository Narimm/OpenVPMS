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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.resource.util.Messages;


/**
 * Table model for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityRelationshipTableModel extends BaseIMObjectTableModel {

    /**
     * The layout context.
     */
    private final LayoutContext layoutContext;

    /**
     * Entity relationship description index.
     */
    private static final int DETAIL_INDEX = NEXT_INDEX;


    /**
     * Construct a new <code>EntityRelationshipTableModel</code>.
     *
     * @param context layout context
     */
    public EntityRelationshipTableModel(LayoutContext context) {
        layoutContext = context;
    }

    /**
     * @see TableModel#getColumnName
     */
    public String getColumnName(int column) {
        return getColumn(column).getHeaderValue().toString();
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
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result = null;
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == NAME_INDEX) {
            result = super.getSortConstraints(column, ascending);
        } else if (col.getModelIndex() == DETAIL_INDEX) {
            SortConstraint name = new NodeSortConstraint("description",
                                                         ascending);
            result = new SortConstraint[]{name};
        }
        return result;
    }

    /**
     * Determines if selection should be enabled. This implementation returns
     * <code>true</code> if in edit mode.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    @Override
    public boolean getEnableSelection() {
        return layoutContext.isEdit();
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        Object result;
        EntityRelationship relationship = (EntityRelationship) object;
        if (column == NAME_INDEX) {
            result = getEntityViewer(relationship);
        } else if (column == DESCRIPTION_INDEX) {
            IMObject entity = IMObjectHelper.getObject(getEntity(relationship));
            result = (entity != null) ? entity.getDescription() : null;
        } else if (column == DETAIL_INDEX) {
            result = object.getDescription();
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns a viewer for the "non-current" entity in a relationship.
     * This returns the "non-current" or target side of the relationship.
     * "Non-current" refers the object that is NOT currently being
     * viewed/edited. If the source and target entities don't refer to the
     * current object being viewed/edited, then the target entity of the
     * relationship is used.
     *
     * @param relationship the relationship
     * @return a viewer of the "non-current" entity of the relationship
     */
    protected Component getEntityViewer(EntityRelationship relationship) {
        IMObjectReference entity = getEntity(relationship);
        boolean hyperlink = !getEnableSelection();
        return new IMObjectReferenceViewer(entity, hyperlink).getComponent();
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createColumn(NAME_INDEX, "table.imobject.name"));
        model.addColumn(
                createColumn(DESCRIPTION_INDEX, "table.imobject.description"));
        model.addColumn(
                createColumn(DETAIL_INDEX, "table.entityrelationship.details"));
        return model;
    }

    /**
     * Helper to create a table column.
     *
     * @param modelIndex the column model index
     * @param nameKey    the column's name localisation key
     * @return a new column
     */
    private TableColumn createColumn(int modelIndex, String nameKey) {
        TableColumn column = new TableColumn(modelIndex);
        column.setHeaderValue(Messages.get(nameKey));
        return column;
    }

    /**
     * Returns a reference to the entity in a relationship. This returns the
     * "non-current" or target side of the relationship. "Non-current" refers
     * the object that is NOT currently being viewed/edited. If the source and
     * target entities don't refer to the current object being viewed/edited,
     * then the target entity of the relationship is used.
     *
     * @param relationship the relationship
     * @return the "non-current" entity of the relationship. May be
     *         <code>null</code>
     */
    private IMObjectReference getEntity(EntityRelationship relationship) {
        IMObjectReference entity;
        IMObject current = layoutContext.getContext().getCurrent();
        if (current == null) {
            entity = relationship.getTarget();
        } else {
            IMObjectReference ref = current.getObjectReference();

            if (relationship.getSource() != null
                    && ref.equals(relationship.getSource())) {
                entity = relationship.getTarget();
            } else if (relationship.getTarget() != null
                    && ref.equals(relationship.getTarget())) {
                entity = relationship.getSource();
            } else {
                entity = relationship.getTarget();
            }
        }
        return entity;
    }

}
