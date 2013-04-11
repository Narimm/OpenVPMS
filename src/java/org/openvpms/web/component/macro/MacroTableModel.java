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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.macro;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;


/**
 * Table model for <em>lookup.macro</em> lookups.
 *
 * @author Tim Anderson
 */
public class MacroTableModel extends DescriptorTableModel<Lookup> {

    /**
     * The node names to display. If {@code null} indicates to display all non-hidden nodes.
     */
    private final String[] nodeNames;

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
        this(true, context);
    }

    /**
     * Constructs a {@code MacroTableModel}.
     *
     * @param all     if {@code true} display all non-hidden nodes, otherwise display the code, name and description
     *                nodes
     * @param context the layout context
     */
    public MacroTableModel(boolean all, LayoutContext context) {
        super(MacroQuery.SHORT_NAMES, context);
        nodeNames = (all) ? null : SUMMARY_NODES;
        setTableColumnModel(createColumnModel(MacroQuery.SHORT_NAMES,
                                              createDefaultLayoutContext(context.getHelpContext())));
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
}
