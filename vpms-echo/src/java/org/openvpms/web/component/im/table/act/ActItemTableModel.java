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

package org.openvpms.web.component.im.table.act;

import java.util.List;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Table model for {@link Act}s of archetype <em>"act.customerEstimationItem"</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActItemTableModel extends DescriptorTableModel {

    /**
     * Construct a <code>ActItemTableModel</code>.
     *
     * @param archetype the act archetype descriptor
     * @param context   the layout context
     */
    public ActItemTableModel(ArchetypeDescriptor archetype,
                             LayoutContext context) {
        super(createColumnModel(archetype, context), context);
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
        return getLayoutContext().isEdit();
    }

    /**
     * Helper to create a column model.
     *
     * @param archetype the act archetype descriptor
     * @param context   the layout context
     * @return a new column model
     */
    protected static TableColumnModel createColumnModel(
            ArchetypeDescriptor archetype, LayoutContext context) {

        NodeDescriptor participants
                = archetype.getNodeDescriptor("participants");
        ChainedNodeFilter filter = new ChainedNodeFilter();
        filter.add(context.getDefaultNodeFilter());
        filter.add(new NamedNodeFilter("participants"));
        List<NodeDescriptor> nodes = FilterHelper.filter(null, filter,
                                                         archetype);
        TableColumnModel columns = new DefaultTableColumnModel();

        String[] range = participants.getArchetypeRange();
        for (int i = 0; i < range.length; ++i) {
            String shortName = range[i];
            if (!shortName.equals("participation.author")) {
                TableColumn column = new ParticipantTableColumn(
                        shortName, participants, NEXT_INDEX + i);
                columns.addColumn(column);
            }
        }
        DescriptorTableModel.create(nodes, columns);
        return columns;
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
        Object result = null;
        TableColumn c = getColumn(column);
        if (c instanceof ParticipantTableColumn) {
            ParticipantTableColumn col = (ParticipantTableColumn) c;
            NodeDescriptor descriptor = col.getDescriptor();
            IMObject child = getByShortName(col.getShortName(),
                                            descriptor.getChildren(object));
            if (child != null) {
                ArchetypeDescriptor archetype
                        = DescriptorHelper.getArchetypeDescriptor(child);
                NodeDescriptor entity = archetype.getNodeDescriptor("entity");
                result = getFactory().create(child, entity);
            } else {
                Label label = LabelFactory.create();
                label.setText("No " + col.getHeaderValue());
                result = label;
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the first object that has a matching short name.
     *
     * @param shortName the short name to matches on
     * @param objects   the objects to search
     * @return the first object with a short name the same as
     *         <code>shortName</code> or <code>null</code> if none exists
     */
    private IMObject getByShortName(String shortName, List<IMObject> objects) {
        IMObject result = null;
        for (IMObject object : objects) {
            if (IMObjectHelper.isA(object, shortName)) {
                result = object;
                break;
            }
        }
        return result;
    }


    /**
     * Helper to associate a participant short name with a column.
     */
    private static class ParticipantTableColumn extends DescriptorTableColumn {

        /**
         * The participant archetype short name.
         */
        private final String _shortName;

        /**
         * The participant archetype display name.
         */
        private final String _displayName;


        /**
         * Creates a <code>ParticipantTableColumn</code> with the specified
         * model index, undefined width, and undefined cell and header
         * renderers.
         *
         * @param shortName  the participant archetype short name
         * @param modelIndex the column index of model data visualized by this
         *                   column
         */
        public ParticipantTableColumn(String shortName,
                                      NodeDescriptor descriptor,
                                      int modelIndex) {
            super(modelIndex, descriptor);
            _shortName = shortName;
            _displayName = DescriptorHelper.getDisplayName(shortName);
        }

        /**
         * Returns the header value for this column.  The header value is the
         * object that will be provided to the header renderer to produce a
         * component that will be used as the table header for this column.
         *
         * @return the header value for this column
         */
        @Override
        public Object getHeaderValue() {
            return _displayName;
        }

        /**
         * Returns the participant's archetype short name.
         *
         * @return the participant's archetype short name
         */
        public String getShortName() {
            return _shortName;
        }
    }

}
