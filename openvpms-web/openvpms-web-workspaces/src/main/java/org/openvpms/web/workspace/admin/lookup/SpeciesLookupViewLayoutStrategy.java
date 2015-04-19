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

package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.text.TextField;


/**
 * Layout strategy for viewing <em>lookup.species</em> lookups.
 * <p/>
 * Displays the display name of any archetype specified for the
 * <em>customFields</em> node.
 *
 * @author Tim Anderson
 */
public class SpeciesLookupViewLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <code>property</code>
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent,
                                             LayoutContext context) {
        if (property.getName().equals("customFields")) {
            String shortName = (String) property.getValue();
            TextField component = TextComponentFactory.create();
            component.setEnabled(false);
            if (shortName != null) {
                String displayName
                        = DescriptorHelper.getDisplayName(shortName);
                if (displayName == null) {
                    displayName = shortName;
                }
                component.setText(displayName);
            }
            return new ComponentState(component, property);
        }
        return super.createComponent(property, parent, context);
    }
}
