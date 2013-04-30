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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Table column associated with one or more {@link NodeDescriptor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @see DescriptorTableModel
 */
public class DescriptorTableColumn extends TableColumn {

    /**
     * The node name.
     */
    private final String name;

    /**
     * Node descriptors, keyed on short name.
     */
    private final Map<String, NodeDescriptor> descriptors
        = new HashMap<String, NodeDescriptor>();


    /**
     * Creates a new <tt>DescriptorTableColumn</tt>.
     *
     * @param modelIndex the column index of model data visualized by this
     *                   column
     * @param name       the node name
     * @param archetypes the archetype descriptors
     */
    public DescriptorTableColumn(int modelIndex, String name,
                                 List<ArchetypeDescriptor> archetypes) {
        super(modelIndex);
        for (ArchetypeDescriptor archetype : archetypes) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
            if (descriptor != null) {
                descriptors.put(archetype.getShortName(), descriptor);
                if (getHeaderValue() == null) {
                    setHeaderValue(descriptor.getDisplayName());
                }
            }
        }
        this.name = name;
    }

    /**
     * Creates a new <tt>DescriptorTableColumn</tt>.
     *
     * @param modelIndex the column index of model data visualized by this
     *                   column
     * @param name       the node name
     * @param archetype  the archetype descriptor
     */
    public DescriptorTableColumn(int modelIndex, String name,
                                 ArchetypeDescriptor archetype) {
        super(modelIndex);
        NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
        if (descriptor != null) {
            descriptors.put(archetype.getShortName(), descriptor);
            setHeaderValue(descriptor.getDisplayName());
        }
        this.name = name;
    }

    /**
     * Returns the value of the cell.
     *
     * @param object the object
     * @return the value of the cell, or <tt>null</tt> if the object doesn't have node
     */
    public Object getValue(IMObject object) {
        NodeDescriptor node = getDescriptor(object);
        return (node != null) ? node.getValue(object) : null;
    }

    /**
     * Returns the values of the cell.
     *
     * @param object the object
     * @return the values of the cell, or <tt>null</tt> if the object doesn't have node or the node isn't a collection
     *         node
     */
    public List<IMObject> getValues(IMObject object) {
        NodeDescriptor node = getDescriptor(object);
        return (node != null) ? node.getChildren(object) : null;
    }

    /**
     * Returns the value of the cell, as a component.
     *
     * @param object  the object
     * @param context the context
     * @return the value of the cell, or <tt>null</tt> if the object doesn't have node
     */
    public Component getComponent(IMObject object, LayoutContext context) {
        Component result;
        NodeDescriptor node = getDescriptor(object);
        if (node != null) {
            IMObjectComponentFactory factory = context.getComponentFactory();
            Property property = new IMObjectProperty(object, node);
            result = factory.create(property, object).getComponent();
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Returns the descriptor's node name.
     *
     * @return the descriptor's node name
     */
    public String getName() {
        return name;
    }

    /**
     * Determines if this column can be sorted on.
     *
     * @return <tt>true</tt> if this column can be sorted on, otherwise
     *         <tt>false</tt>
     */
    public boolean isSortable() {
        boolean sortable = true;
        for (NodeDescriptor descriptor : descriptors.values()) {
            // can only sort on top-level or participation nodes
            if (descriptor.isCollection() && !QueryHelper.isParticipationNode(descriptor)) {
                sortable = false;
                break;
            } else if (descriptor.getPath().lastIndexOf("/") > 0) {
                sortable = false;
                break;
            }
        }
        return sortable;
    }

    /**
     * Creates a new sort constraint for this column.
     *
     * @param ascending whether to sort in ascending or descending order
     * @return a new sort cosntraint
     */
    public SortConstraint createSortConstraint(boolean ascending) {
        return new NodeSortConstraint(name, ascending);
    }

    /**
     * Returns the descriptor for a specific object.
     *
     * @param object the object
     * @return the descriptor for <tt>object</tt>, or <tt>null</tt> if
     *         no descriptor is registered
     */
    protected NodeDescriptor getDescriptor(IMObject object) {
        String shortName = object.getArchetypeId().getShortName();
        return descriptors.get(shortName);
    }

}
