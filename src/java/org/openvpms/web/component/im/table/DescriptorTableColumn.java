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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;

import java.util.Map;


/**
 * Table column associated with a {@link NodeDescriptor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @see DescriptorTableModel
 */
public class DescriptorTableColumn extends TableColumn {

    /**
     * The default node descriptor.
     */
    private final NodeDescriptor descriptor;

    /**
     * Node descriptors, keyed on short name.
     */
    private final Map<String, NodeDescriptor> descriptors;


    /**
     * Construct a new <code>DescriptorTableColumn</code> with the specified
     * model index,undefined width, and undefined cell and header renderers.
     *
     * @param modelIndex  the column index of model data visualized by this
     *                    column
     * @param descriptors the node descriptors
     */
    public DescriptorTableColumn(int modelIndex,
                                 Map<String, NodeDescriptor> descriptors) {
        super(modelIndex);
        this.descriptors = descriptors;
        this.descriptor = descriptors.values().toArray(
                new NodeDescriptor[0])[0];
    }

    /**
     * Returns the header value for this column.  The header value is the object
     * that will be provided to the header renderer to produce a component that
     * will be used as the table header for this column.
     *
     * @return the header value for this column
     */
    @Override
    public Object getHeaderValue() {
        return descriptor.getDisplayName();
    }

    /**
     * Returns the value of the cell.
     *
     * @param object the context
     * @return the value of the cell
     */
    public Component getValue(IMObject object, LayoutContext context) {
        IMObjectComponentFactory factory = context.getComponentFactory();
        Property property = new IMObjectProperty(object, descriptor);
        return factory.create(property, object).getComponent();
    }

    /**
     * Returns the default descriptor.
     *
     * @return the default descriptor
     */
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the descriptor for a specific object.
     *
     * @param object the object
     * @return the descriptor for <code>object</code>
     */
    public NodeDescriptor getDescriptor(IMObject object) {
        String shortName = object.getArchetypeId().getShortName();
        NodeDescriptor result = descriptors.get(shortName);
        return (result != null) ? result : descriptor;
    }

    /**
     * Determines if this column can be sorted on.
     *
     * @return <tt>true</tt> if this column can be sorted on, otherwise
     *         <tt>false</tt>
     */
    public boolean isSortable() {
        // can only sort on top-level or participation nodes
        return (!descriptor.isCollection() &&
                descriptor.getPath().lastIndexOf("/") <= 0)
                || QueryHelper.isParticipationNode(descriptor);
    }

    /**
     * Creates a new sort constraint for this column.
     *
     * @param ascending whether to sort in ascending or descending order
     * @return a new sort cosntraint
     */
    public SortConstraint createSortConstraint(boolean ascending) {
        return new NodeSortConstraint(descriptor.getName(), ascending);
    }

}
