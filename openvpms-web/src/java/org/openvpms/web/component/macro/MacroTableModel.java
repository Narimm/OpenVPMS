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
 *
 *  $Id$
 */
package org.openvpms.web.component.macro;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.table.DescriptorTableModel;


/**
 * Table model for <em>lookup.macro</em> lookups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacroTableModel extends DescriptorTableModel<Lookup> {

    /**
     * The node names to display. If <tt>null</tt> indicates to display all non-hidden nodes.
     */
    private final String[] nodeNames;

    /**
     * The nodes to display when displaying a subset of nodes.
     */
    private static final String[] SUMMARY_NODES = {"code", "name", "description"};

    
    /**
     * Constructs a <tt>MacroTablelModel</tt> that displays all non-hidden nodes.
     */
    public MacroTableModel() {
        this(true);
    }

    /**
     * Constructs a <tt>MacroTablelModel</tt>.
     *
     * @param all if <tt>true</tt> display all non-hidden nodes, otherwise display the code, name and description
     *            nodes
     */
    public MacroTableModel(boolean all) {
        super(MacroQuery.SHORT_NAMES);
        nodeNames = (all) ? null : SUMMARY_NODES;
        setTableColumnModel(createColumnModel(MacroQuery.SHORT_NAMES, createDefaultLayoutContext()));
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
