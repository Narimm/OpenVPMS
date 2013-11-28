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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;

import java.util.List;

/**
 * Default implementation of the {@link DescriptorTableModel}.
 *
 * @author Tim Anderson
 */
public class DefaultDescriptorTableModel<T extends IMObject>
        extends DescriptorTableModel<T> {

    /**
     * The node names to include in the table. If empty, all simple nodes
     * will be displayed.
     */
    private final String[] nodeNames;

    /**
     * Determines if the active nodes is displayed.
     */
    private final boolean showActive;

    /**
     * Constructs a {@link DefaultDescriptorTableModel}.
     *
     * @param shortName the archetype short name(s). May contain wildcards
     * @param context   the layout context
     * @param names     the node names to display. If empty, all simple nodes will be displayed
     */
    public DefaultDescriptorTableModel(String shortName, LayoutContext context, String... names) {
        this(new String[]{shortName}, context, names);
    }

    /**
     * Constructs a {@link DefaultDescriptorTableModel}.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @param context    the layout context
     * @param names      the node names to display. If empty, all simple nodes will be displayed
     */
    public DefaultDescriptorTableModel(String[] shortNames, LayoutContext context, String... names) {
        this(shortNames, null, context, names);
    }

    /**
     * Constructs a {@link DefaultDescriptorTableModel}.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @param query      the query. May be {@code null}
     * @param context    the layout context
     * @param names      the node names to display. If empty, all simple nodes will be displayed
     */
    public DefaultDescriptorTableModel(String[] shortNames, Query<T> query, LayoutContext context, String... names) {
        super(context);
        this.nodeNames = names;
        showActive = (query == null) || query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createColumnModel(shortNames, context));
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return nodeNames;
    }

    /**
     * Returns the node names for a set of archetypes.
     * <p/>
     * If {@link #getNodeNames()} returns a non-empty list, then
     * these names will be used, otherwise the node names common to each
     * archetype will be returned.
     *
     * @param archetypes the archetype descriptors
     * @param context    the layout context
     * @return the node names for the archetypes
     */
    @Override
    protected List<String> getNodeNames(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        List<String> names = super.getNodeNames(archetypes, context);
        if (!showActive) {
            names.remove("active");
        }
        return names;
    }
}
