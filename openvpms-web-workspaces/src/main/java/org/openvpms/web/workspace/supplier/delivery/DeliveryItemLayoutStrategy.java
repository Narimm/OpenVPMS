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

package org.openvpms.web.workspace.supplier.delivery;

import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;

/**
 * Layout strategy for <em>act.supplierDeliveryItem</em> acts.
 * <p/>
 * This suppresses the order and supplierInvoiceLineId nodes if they are empty.
 *
 * @author Tim Anderson
 */
public class DeliveryItemLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES
            = new ArchetypeNodes().excludeIfEmpty("order", "supplierInvoiceLineId");

    /**
     * Default constructor.
     */
    public DeliveryItemLayoutStrategy() {
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return NODES;
    }
}
