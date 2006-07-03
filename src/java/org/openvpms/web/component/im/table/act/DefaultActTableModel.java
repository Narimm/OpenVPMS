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

import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Default table model for displaying {@link Act}s.
 * Any "items" nodes are filtered.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultActTableModel extends DescriptorTableModel {

    /**
     * Construct a <code>DefaultActTableModel</code>.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context
     */
    public DefaultActTableModel(String[] shortNames, LayoutContext context) {
        super(createColumnModel(shortNames, context), context);
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
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    protected static TableColumnModel createColumnModel(String[] shortNames,
                                                        LayoutContext context) {
        TableColumnModel columns;
        List<ArchetypeDescriptor> archetypes
                = DescriptorHelper.getArchetypeDescriptors(shortNames);
        if (archetypes.size() > 1) {
            columns = createColumnModel(archetypes, context);
        } else if (!archetypes.isEmpty()) {
            ArchetypeDescriptor archetype = archetypes.get(0);
            columns = addColumns(new DefaultTableColumnModel(),
                                 archetype.getAllNodeDescriptors(),
                                 context);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'shortNames' doesn't refer to a valid archetype");
        }
        return columns;
    }

    /**
     * Creates a column model for a list of archetypes. The first column is the
     * archetype, the last column the archetype description. The middle columns
     * are the nodes common to all the archetypes.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    private static TableColumnModel createColumnModel(
            List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        TableColumnModel columns = new DefaultTableColumnModel();

        columns.addColumn(new TableColumn(ARCHETYPE_INDEX));

        List<NodeDescriptor> common = null;
        for (ArchetypeDescriptor archetype : archetypes) {
            List<NodeDescriptor> nodes = archetype.getAllNodeDescriptors();
            if (common == null) {
                common = nodes;
            } else {
                common = getIntersection(common, nodes);
            }
        }

        addColumns(columns, common, context);
        columns.addColumn(new TableColumn(DESCRIPTION_INDEX));
        return columns;
    }

    /**
     * Adds columns to a column model.
     *
     * @param columns     the column model
     * @param descriptors the column descriptors
     * @param context     the layout context
     * @return the column model
     */
    private static TableColumnModel addColumns(
            TableColumnModel columns,
            List<NodeDescriptor> descriptors,
            LayoutContext context) {

        NodeFilter filter = FilterHelper.chain(new NamedNodeFilter("items"),
                                               context.getDefaultNodeFilter());
        descriptors = FilterHelper.filter(null, filter, descriptors);
        DescriptorTableModel.create(descriptors, columns);
        return columns;
    }

    /**
     * Helper to return the intersection of two lists of node descriptors.
     *
     * @param first  the first list of nodes
     * @param second the second list of nodes
     * @return the intersection of the two lists
     */
    private static List<NodeDescriptor> getIntersection(
            List<NodeDescriptor> first, List<NodeDescriptor> second) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor a : first) {
            for (NodeDescriptor b : second) {
                if (a.getName().equals(b.getName())) {
                    result.add(a);
                    break;
                }
            }
        }
        return result;
    }

}
