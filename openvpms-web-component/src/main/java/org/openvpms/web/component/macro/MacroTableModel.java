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
package org.openvpms.web.component.macro;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;

import java.util.List;


/**
 * Table model for <em>lookup.macro</em> and <em>lookup.reportMacro</em> lookups.
 *
 * @author Tim Anderson
 */
public class MacroTableModel extends DescriptorTableModel<Lookup> {

    /**
     * The node names to display. If {@code null} indicates to display all non-hidden nodes.
     */
    private final String[] nodeNames;

    /**
     * Determines if the archetype column should be displayed.
     */
    private final boolean showArchetype;

    /**
     * The nodes to display when displaying a subset of nodes.
     */
    private static final String[] SUMMARY_NODES = {"code", "name", "description"};


    /**
     * Constructs a {@code MacroTableModel} that displays all non-hidden nodes.
     *
     * @param context the layout context
     */
    public MacroTableModel(LayoutContext context) {
        this(true, true, context);
    }

    /**
     * Constructs a {@code MacroTableModel}.
     *
     * @param all           if {@code true} display all non-hidden nodes, otherwise display the code, name and
     *                      description nodes
     * @param showArchetype if {@code true}, display the archetype column
     * @param context       the layout context
     */
    public MacroTableModel(boolean all, boolean showArchetype, LayoutContext context) {
        super(context);
        nodeNames = (all) ? null : SUMMARY_NODES;
        this.showArchetype = showArchetype;
        setTableColumnModel(createColumnModel(MacroQuery.SHORT_NAMES, getLayoutContext()));
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return nodeNames;
    }

    /**
     * Determines if the archetype column should be displayed.
     *
     * @param archetypes the archetypes
     * @return the value of {@link #showArchetype}
     */
    @Override
    protected boolean showArchetypeColumn(List<ArchetypeDescriptor> archetypes) {
        return showArchetype;
    }
}
