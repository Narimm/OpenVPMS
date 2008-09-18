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

package org.openvpms.web.component.im.layout;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundColorSelect;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;


/**
 * Layout strategy that uses a {@link BoundColorSelect} for any "colour" node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ColourNodeLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <tt>property</tt>
     */
    @Override
    protected ComponentState createComponent(Property property,
                                             IMObject parent,
                                             LayoutContext context) {
        if ("colour".equals(property.getName())) {
            BoundColorSelect colour = new BoundColorSelect(property);
            if (!context.isEdit()) {
                colour.setEnabled(false);
            }
            return new ComponentState(colour, property);
        }
        return super.createComponent(property, parent, context);
    }
}
