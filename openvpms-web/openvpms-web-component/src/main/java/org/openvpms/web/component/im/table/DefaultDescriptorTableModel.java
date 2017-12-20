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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;

/**
 * Default implementation of the {@link DescriptorTableModel}.
 *
 * @author Tim Anderson
 */
public class DefaultDescriptorTableModel<T extends IMObject>
        extends DescriptorTableModel<T> {

    /**
     * The nodes to display.
     */
    private final ArchetypeNodes nodes;

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
        if (names.length == 0) {
            nodes = allSimpleNodesMinusIdAndLongText();
        } else {
            nodes = ArchetypeNodes.onlySimple(names);
        }
        boolean showActive = (query == null) || query.getActive() == BaseArchetypeConstraint.State.BOTH;
        if (!showActive) {
            nodes.exclude("active");
        }
        setTableColumnModel(createColumnModel(shortNames, context));
    }

    /**
     * Returns an {@link ArchetypeNodes} that determines what nodes appear in the table.
     *
     * @return the nodes to include
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return nodes;
    }
}
