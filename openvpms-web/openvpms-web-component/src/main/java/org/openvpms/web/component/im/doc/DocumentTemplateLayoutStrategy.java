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

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SpinBox;

import java.util.List;


/**
 * Layout strategy for <em>entity.documentTemplate</em> entities.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentTemplateLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The component representing the 'content' node.
     */
    private ComponentState content;


    /**
     * Constructs a new <code>DocumentTemplateLayoutStrategy</code>.
     */
    public DocumentTemplateLayoutStrategy() {
    }

    /**
     * Constructs a new <code>DocumentTemplateLayoutStrategy</code>.
     *
     * @param content    the component representing the 'content' node
     * @param focusGroup the component's focus group. May be <code>null</code>
     */
    public DocumentTemplateLayoutStrategy(Component content,
                                          FocusGroup focusGroup) {
        this.content = new ComponentState(content, focusGroup);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <code>null</code>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        if (content == null) {
            TemplateHelper helper = new TemplateHelper();
            Participation participation
                = helper.getDocumentParticipation((Entity) object);
            if (participation != null) {
                content = context.getComponentFactory().create(participation,
                                                               object);
            } else {
                content = new ComponentState(LabelFactory.create());
            }
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out components in a grid.
     *
     * @param object      the object to lay out
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param context     the layout context
     */
    @Override
    protected Grid createGrid(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                              LayoutContext context) {
        Grid grid = super.createGrid(object, descriptors, properties, context);
        add(grid, "Content", content);
        return grid;
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <code>property</code>
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
        if (context.isEdit() && property.getName().equals("copies")) {
            SpinBox copies = new SpinBox(property, 1, 99);
            return new ComponentState(copies, property, copies.getFocusGroup());
        }
        return super.createComponent(property, parent, context);
    }
}
