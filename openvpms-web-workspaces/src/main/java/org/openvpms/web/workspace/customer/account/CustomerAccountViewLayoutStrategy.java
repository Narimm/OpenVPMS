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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.account;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * A layout strategy for customer account acts that displays the "hide" node, if {@code true}.
 *
 * @author Tim Anderson
 */
public class CustomerAccountViewLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Constructs a {@link CustomerAccountViewLayoutStrategy}.
     */
    public CustomerAccountViewLayoutStrategy() {
        super();
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @param object  the object to display
     * @param context the layout context
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes(IMObject object, LayoutContext context) {
        ArchetypeNodes nodes = super.getArchetypeNodes(object, context);
        IMObjectBean bean = new IMObjectBean(object);
        if (bean.getBoolean("hide")) {
            nodes = new ArchetypeNodes(nodes);
            nodes.simple("hide");
        }
        return nodes;
    }
}
