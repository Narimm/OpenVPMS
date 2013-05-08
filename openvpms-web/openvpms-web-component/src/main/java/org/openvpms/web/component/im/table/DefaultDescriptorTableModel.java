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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.table;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Default implementation of the {@link DescriptorTableModel}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultDescriptorTableModel<T extends IMObject>
    extends DescriptorTableModel<T> {

    /**
     * The node names to include in the table. If empty, all simple nodes
     * will be displayed.
     */
    private final String[] nodeNames;

    /**
     * Creates a new <tt>DefaultDescriptorTableModel</tt>.
     *
     * @param shortName the archetype short name(s). May contain wildcards
     * @param context   the layout context. May be <tt>null</tt>
     * @param names     the node names to display. If empty, all simple nodes
     *                  will be displayed
     */
    public DefaultDescriptorTableModel(String shortName,
                                       LayoutContext context,
                                       String... names) {
        this(new String[]{shortName}, context, names);
    }

    /**
     * Creates a new <tt>DefaultDescriptorTableModel</tt>.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @param context    the layout context. May be <tt>null</tt>
     * @param names      the node names to display. If empty, all simple nodes will
     *                   be displayed
     */
    public DefaultDescriptorTableModel(String[] shortNames,
                                       LayoutContext context,
                                       String... names) {
        super(context);
        this.nodeNames = names;
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
}
