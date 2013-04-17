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
 */

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DelegatingIMTableModel;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Table model for {@link RelationshipState}s that intercepts calls for
 * the <em>"source"</em> and <em>"target"</em> nodes but delegates other
 * calls to the underlying table model.
 * <p/>
 * This improves rendering performance, as the source and target nodes
 * don't need to be fetched from the archetype service.
 * <p/>
 * In order for the interception <em>"source"</em> and <em>"target"</em> nodes
 * to be successful, they must be represented by {@link DescriptorTableColumn}
 * instances in the underlying model. The simplest way to achieve this is to
 * use {@link DescriptorTableModel} which creates these columns.
 *
 * @author Tim Anderson
 */
public class DelegatingRelationshipStateTableModel
        extends DelegatingIMTableModel<RelationshipState,
        IMObjectRelationship> {

    /**
     * The relationship states.
     */
    private List<RelationshipState> states;

    /**
     * The source node column index, or {@code -1} if it is not present.
     */
    private int sourceColumn = -1;

    /**
     * The target node column index, or {@code -1} if it is not present.
     */
    private int targetColumn = -1;

    /**
     * The layout context.
     */
    private final LayoutContext context;


    /**
     * Creates a new {@code DelegatingRelationshipStateTableModel}.
     *
     * @param model   the underlying model
     * @param context the layout context
     */
    public DelegatingRelationshipStateTableModel(
            IMTableModel<IMObjectRelationship> model,
            LayoutContext context) {
        setModel(model);
        this.context = context;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     * Column and row values are 0-based.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    @Override
    public Object getValueAt(int column, int row) {
        if (column == sourceColumn) {
            return getSource(row);
        } else if (column == targetColumn) {
            return getTarget(row);
        }
        return super.getValueAt(column, row);
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    @Override
    public void setObjects(List<RelationshipState> objects) {
        this.states = objects;
        List<IMObjectRelationship> list
                = new ArrayList<IMObjectRelationship>(states.size());
        for (RelationshipState state : states) {
            list.add(state.getRelationship());
        }
        getModel().setObjects(list);
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    @Override
    public List<RelationshipState> getObjects() {
        return states;
    }

    /**
     * Sets the model to delegate to.
     *
     * @param model the model to delegate to
     */
    @Override
    protected void setModel(IMTableModel<IMObjectRelationship> model) {
        super.setModel(model);
        TableColumnModel columns = model.getColumnModel();
        sourceColumn = getModelIndex(columns, "source");
        targetColumn = getModelIndex(columns, "target");
    }

    /**
     * Returns a component to view the source column.
     *
     * @param row the row
     * @return a component to view the source column, or {@code null} if the
     *         row exceeds the no. of states
     */
    private Component getSource(int row) {
        if (row < states.size()) {
            RelationshipState state = states.get(row);
            ContextSwitchListener listener = (context.isEdit()) ? context.getContextSwitchListener() : null;
            IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(state.getSource(), state.getSourceName(),
                                                                         listener, context.getContext());
            return viewer.getComponent();
        }
        return null;
    }

    /**
     * Returns a component to view the target column.
     *
     * @param row the row
     * @return a component to view the target column, or {@code null} if the
     *         row exceeds the no. of states
     */
    private Object getTarget(int row) {
        if (row < states.size()) {
            RelationshipState state = states.get(row);
            ContextSwitchListener listener = (context.isEdit()) ? context.getContextSwitchListener() : null;
            IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(state.getTarget(), state.getTargetName(),
                                                                         listener, context.getContext());
            return viewer.getComponent();
        }
        return null;
    }

    /**
     * Returns the index of a node in a column model.
     *
     * @param model the column model
     * @param node  the node name
     * @return the index of the node, or {@code -1} if it is not found
     */
    private int getModelIndex(TableColumnModel model, String node) {
        Iterator iter = model.getColumns();
        while (iter.hasNext()) {
            TableColumn next = (TableColumn) iter.next();
            if (next instanceof DescriptorTableColumn) {
                DescriptorTableColumn col = (DescriptorTableColumn) next;
                if (col.getName().equals(node)) {
                    return col.getModelIndex();
                }
            }
        }
        return -1;
    }
}
