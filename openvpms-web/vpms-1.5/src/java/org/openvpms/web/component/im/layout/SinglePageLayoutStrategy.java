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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.layout;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.property.PropertySet;

import java.util.Collection;
import java.util.List;


/**
 * {@link IMObjectLayoutStrategy} that lays out {@link IMObject} instances on a
 * single page.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SinglePageLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Construct a new <code>SinglePageLayoutStrategy</code>.
     */
    public SinglePageLayoutStrategy() {
    }


    /**
     * Lays out each child component in a group box.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   PropertySet properties, Component container,
                                   LayoutContext context) {
        for (NodeDescriptor node : descriptors) {
            GroupBox box = new GroupBox(node.getDisplayName());
            Collection values = (Collection) node.getValue(object);
            for (Object value : values) {
                doLayout((IMObject) value, properties, box, context);
            }
            container.add(box);
        }
    }


}
