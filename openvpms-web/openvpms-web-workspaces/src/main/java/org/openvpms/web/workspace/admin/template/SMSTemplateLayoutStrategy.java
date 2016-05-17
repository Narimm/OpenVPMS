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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.template;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.List;

/**
 * Layout strategy for <em>entity.documentTemplateSMS*</em> entities.
 *
 * @author Tim Anderson
 */
public class SMSTemplateLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The content node name.
     */
    private static final String CONTENT = "content";

    /**
     * The nodes to display. The content node is handled explicitly.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude(CONTENT);

    /**
     * Constructs an {@link SMSTemplateLayoutStrategy}.
     */
    public SMSTemplateLayoutStrategy() {
        super(NODES);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        addComponent(createComponent(properties.get(CONTENT), object, context));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out components in a grid.
     *
     * @param object     the object to lay out
     * @param properties the properties
     * @param context    the layout context
     */
    @Override
    protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context) {
        ComponentGrid grid = super.createGrid(object, properties, context);
        grid.add(getComponent(CONTENT), 2);
        return grid;
    }
}
