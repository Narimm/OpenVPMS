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
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.LabelFactory;

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
     * Lays out child components in 2 columns.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param grid        the grid to use
     * @param context     the layout context
     */
    @Override
    protected void doGridLayout(IMObject object,
                                List<NodeDescriptor> descriptors,
                                PropertySet properties, Grid grid,
                                LayoutContext context) {
        super.doGridLayout(object, descriptors, properties, grid,
                           context);
        add(grid, "Content", content);
    }

}
