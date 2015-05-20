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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Table column associated with one or more {@link NodeDescriptor}.
 *
 * @author Tim Anderson
 * @see DescriptorTableModel
 */
public class DescriptorTableColumn extends TableColumn {

    /**
     * The node name.
     */
    private final String name;

    /**
     * The default value, if the node doesn't have one.
     */
    private Object defaultValue;

    /**
     * Node descriptors, keyed on short name.
     */
    private final Map<String, NodeDescriptor> descriptors = new HashMap<String, NodeDescriptor>();

    /**
     * Constructs a {@link DescriptorTableColumn}.
     *
     * @param modelIndex the column index of model data visualized by this
     *                   column
     * @param name       the node name
     * @param archetypes the archetype descriptors
     */
    public DescriptorTableColumn(int modelIndex, String name, List<ArchetypeDescriptor> archetypes) {
        this(modelIndex, name, null, archetypes);
    }


    /**
     * Constructs a {@link DescriptorTableColumn}.
     *
     * @param modelIndex   the column index of model data visualized by this column
     * @param name         the node name
     * @param defaultValue the default value, if the node doesn't have one
     * @param archetypes   the archetype descriptors
     */
    public DescriptorTableColumn(int modelIndex, String name, Object defaultValue,
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
        this.defaultValue = defaultValue;
    }

    /**
     * Constructs a {@link DescriptorTableColumn}.
     *
     * @param modelIndex the column index of model data visualized by this
     *                   column
     * @param name       the node name
     * @param archetype  the archetype descriptor
     */
    public DescriptorTableColumn(int modelIndex, String name, ArchetypeDescriptor archetype) {
        this(modelIndex, name, null, archetype);
    }

    /**
     * Constructs a {@link DescriptorTableColumn}.
     *
     * @param modelIndex the column index of model data visualized by this
     *                   column
     * @param name       the node name
     * @param archetype  the archetype descriptor
     */
    public DescriptorTableColumn(int modelIndex, String name, Object defaultValue, ArchetypeDescriptor archetype) {
        super(modelIndex);
        NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
        if (descriptor != null) {
            descriptors.put(archetype.getShortName(), descriptor);
            setHeaderValue(descriptor.getDisplayName());
        }
        this.name = name;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the value of the cell.
     *
     * @param object the object
     * @return the value of the cell, or {@code null} if the object doesn't have node
     */
    public Object getValue(IMObject object) {
        Object result = null;
        NodeDescriptor node = getDescriptor(object);
        if (node != null) {
            result = node.getValue(object);
            if (result == null) {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
     * Returns the values of the cell.
     *
     * @param object the object
     * @return the values of the cell, or {@code null} if the object doesn't have node or the node isn't a collection
     *         node
     */
    public List<IMObject> getValues(IMObject object) {
        NodeDescriptor node = getDescriptor(object);
        return (node != null) ? node.getChildren(object) : null;
    }

    /**
     * Sets the default value to use, if the node doesn't have one.
     *
     * @param defaultValue the default value. May be {@code null}
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the value of the cell, as a component.
     *
     * @param object  the object
     * @param context the context
     * @return the value of the cell, or {@code null} if the object doesn't have node
     */
    public Component getComponent(IMObject object, LayoutContext context) {
        Component result;
        NodeDescriptor node = getDescriptor(object);
        if (node != null) {
            IMObjectComponentFactory factory = context.getComponentFactory();
            Property property = new IMObjectProperty(object, node);
            if (defaultValue != null) {
                property = new DelegatingProperty(property) {
                    @Override
                    public Object getValue() {
                        Object value = super.getValue();
                        return value == null ? defaultValue : value;
                    }
                };
            }
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
     * @return {@code true} if this column can be sorted on, otherwise
     *         {@code false}
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
     * @return a new sort constraint
     */
    public SortConstraint createSortConstraint(boolean ascending) {
        return new NodeSortConstraint(name, ascending);
    }

    /**
     * Returns the descriptor for a specific object.
     *
     * @param object the object
     * @return the descriptor for {@code object}, or {@code null} if
     *         no descriptor is registered
     */
    protected NodeDescriptor getDescriptor(IMObject object) {
        String shortName = object.getArchetypeId().getShortName();
        return descriptors.get(shortName);
    }

}
