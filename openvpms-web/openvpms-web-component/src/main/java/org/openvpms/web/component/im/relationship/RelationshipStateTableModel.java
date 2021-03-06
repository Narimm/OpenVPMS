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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;


/**
 * A table model for {@link RelationshipState} instances, that renders
 * the source or target name and description, and the relationship description.
 *
 * @author Tim Anderson
 */
public class RelationshipStateTableModel extends AbstractIMTableModel<RelationshipState> {

    /**
     * If {@code true} displays the target of the relationship; otherwise displays the source.
     */
    private final boolean displayTarget;

    /**
     * Determines if the active column is displayed.
     */
    private boolean showActive;

    /**
     * The listener to notify when an object is selected.
     */
    private final ContextSwitchListener listener;

    /**
     * The context.
     */
    private final LayoutContext context;

    /**
     * Name column index.
     */
    protected static final int NAME_INDEX = 1;

    /**
     * Description column index.
     */
    protected static final int DESCRIPTION_INDEX = 2;

    /**
     * Entity relationship description index.
     */
    protected static final int DETAIL_INDEX = 3;

    /**
     * The active column index.
     */
    protected static final int ACTIVE_INDEX = 4;


    /**
     * Constructs a {@link RelationshipStateTableModel}.
     * <p/>
     * Enables selection if the context is in edit mode.
     *
     * @param context       layout context
     * @param displayTarget if {@code true} display the relationship target, otherwise display the source
     */
    public RelationshipStateTableModel(LayoutContext context, boolean displayTarget) {
        this.context = context;
        this.displayTarget = displayTarget;
        setTableColumnModel(createTableColumnModel());
        setEnableSelection(context.isEdit());
        this.listener = context.getContextSwitchListener();
    }

    /**
     * Determines if the active column is displayed.
     *
     * @param show if {@code true}, show the active column
     */
    public void setShowActive(boolean show) {
        if (show != showActive) {
            showActive = show;
            setTableColumnModel(createTableColumnModel());
        }
    }

    /**
     * Determines if the active column is displayed.
     *
     * @return {@code true} if the active column is displayed
     */
    public boolean getShowActive() {
        return showActive;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result = null;
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == NAME_INDEX) {
            result = new SortConstraint[]{getNameSortConstraint(ascending)};
        } else if (col.getModelIndex() == DESCRIPTION_INDEX) {
            SortConstraint sort = new VirtualNodeSortConstraint("description", ascending, new Transformer() {
                @Override
                public Object transform(Object input) {
                    RelationshipState state = (RelationshipState) input;
                    return (displayTarget) ? state.getTargetDescription() : state.getSourceDescription();
                }
            });
            result = new SortConstraint[]{sort, getNameSortConstraint(true)};
        } else if (col.getModelIndex() == DETAIL_INDEX) {
            SortConstraint sort = new VirtualNodeSortConstraint("detail", ascending, new Transformer() {
                @Override
                public Object transform(Object input) {
                    RelationshipState state = (RelationshipState) input;
                    return state.getRelationship().getDescription();
                }
            });
            result = new SortConstraint[]{sort, getNameSortConstraint(true)};
        } else if (col.getModelIndex() == ACTIVE_INDEX) {
            SortConstraint sort = new VirtualNodeSortConstraint("active", ascending, new Transformer() {
                @Override
                public Object transform(Object input) {
                    RelationshipState state = (RelationshipState) input;
                    return state.getRelationship().isActive();
                }
            });
            result = new SortConstraint[]{sort, getNameSortConstraint(true)};
        }
        return result;
    }

    /**
     * Returns a sort constraint on the source/target name.
     *
     * @param ascending determines whether to sort in ascending or descending order
     * @return a new sort constraint
     */
    protected SortConstraint getNameSortConstraint(boolean ascending) {
        SortConstraint result;
        result = new VirtualNodeSortConstraint("name", ascending, new Transformer() {
            @Override
            public Object transform(Object input) {
                RelationshipState state = (RelationshipState) input;
                return displayTarget ? state.getTargetName() : state.getSourceName();
            }
        });
        return result;
    }

    /**
     * Indicates whether to display the target or source of the relationship.
     *
     * @return {@code true} to display the target of the relationship, or {@code false} to display the source.
     */
    protected boolean displayTarget() {
        return displayTarget;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(RelationshipState object, TableColumn column, int row) {
        Object result = null;
        switch (column.getModelIndex()) {
            case NAME_INDEX:
                result = getEntityViewer(object);
                break;
            case DESCRIPTION_INDEX:
                result = getDescription(object);
                break;
            case DETAIL_INDEX:
                result = object.getRelationship().getDescription();
                break;
            case ACTIVE_INDEX:
                result = getCheckBox(object.isActive());
                break;
        }
        return result;
    }

    /**
     * Returns a viewer for the source or target entity of the relationship, depending on the {@link #displayTarget}
     * flag.
     *
     * @param state the relationship state
     * @return a viewer for the entity
     */
    protected Component getEntityViewer(RelationshipState state) {
        IMObjectReference ref;
        String name;
        if (displayTarget) {
            ref = state.getTarget();
            name = state.getTargetName();
        } else {
            ref = state.getSource();
            name = state.getSourceName();
        }

        return getEntityViewer(ref, name);
    }

    /**
     * Returns a viewer for a reference and name.
     *
     * @param ref  the reference
     * @param name the name
     * @return a new viewer
     */
    protected Component getEntityViewer(IMObjectReference ref, String name) {
        ContextSwitchListener link = (!getEnableSelection()) ? listener : null;
        return new IMObjectReferenceViewer(ref, name, link, context.getContext()).getComponent();
    }

    /**
     * Returns the description of the source or target entity of the
     * relationship, depending on the {@link #displayTarget} flag.
     *
     * @param state the relationship
     * @return the source or target description
     */
    protected Object getDescription(RelationshipState state) {
        return displayTarget ? state.getTargetDescription() : state.getSourceDescription();
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(NAME_INDEX, "table.imobject.name"));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, "table.imobject.description"));
        model.addColumn(createTableColumn(DETAIL_INDEX, "table.entityrelationship.details"));
        if (showActive) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }
        return model;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getContext() {
        return context;
    }

}
