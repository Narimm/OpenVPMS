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
package org.openvpms.web.workspace.supplier.delivery;

import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;

/**
 * Table model for <em>act.supplierDelivery</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2011-04-11 11:16:31Z $
 */
public class DeliveryTableModel extends AbstractActTableModel {

    /**
     * The nodes to display.
     */
    private static final String[] NODES = {"startTime", "supplier", "stockLocation", "amount", "tax", "status"};

    /**
     * Creates a new <tt>DeliveryTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context. May be <tt>null</tt>
     */
    public DeliveryTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return NODES;
    }

    @Override
    protected int getArchetypeColumnIndex() {
        return 1;
    }
}