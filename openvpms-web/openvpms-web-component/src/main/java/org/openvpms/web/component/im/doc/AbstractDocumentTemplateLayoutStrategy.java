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

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * Layout strategy for <em>entity.documentTemplate*</em> entities.
 *
 * @author Tim Anderson
 */
public class AbstractDocumentTemplateLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The component representing the 'content' node.
     */
    private ComponentState content;

    /**
     * Constructs an {@link AbstractDocumentTemplateLayoutStrategy}.
     */
    public AbstractDocumentTemplateLayoutStrategy() {
        super();
    }

    /**
     * Constructs an {@link AbstractDocumentTemplateLayoutStrategy}.
     *
     * @param content the component representing the 'content' node
     */
    public AbstractDocumentTemplateLayoutStrategy(ComponentState content) {
        this.content = content;
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
        if (content != null && !content.hasLabel()) {
            content.setDisplayName(Messages.get("document.template.content"));
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns a component representing the template document.
     * <p/>
     * This associates the component with the "content" property so that it can be included
     * in the layout without further customisation.
     *
     * @param object     the template
     * @param properties the properties
     * @param context    the layout context
     * @return a component representing the template document
     */
    protected ComponentState getContent(Entity object, PropertySet properties, LayoutContext context) {
        ComponentState component;
        TemplateHelper helper = new TemplateHelper(ServiceHelper.getArchetypeService());
        Participation participation = helper.getDocumentParticipation(object);
        Property property = properties.get("content");
        if (participation != null) {
            component = context.getComponentFactory().create(participation, object);
            if (component.getProperty() == null) {
                component = new ComponentState(component.getComponent(), property, component.getFocusGroup(),
                                               component.getDisplayName());
            }
        } else {
            component = new ComponentState(LabelFactory.create(), property);
        }
        return component;
    }

    /**
     * Initialises the content component.
     *
     * @param object     the template
     * @param properties the properties
     * @param context    the layout context  @return the content component
     */
    protected ComponentState initContent(Entity object, PropertySet properties, LayoutContext context) {
        setContent(getContent(object, properties, context));
        return content;
    }

    /**
     * Registers a component representing the template document.
     *
     * @param content the content. May be {@code null}
     */
    protected void setContent(ComponentState content) {
        this.content = content;
    }

    /**
     * Returns the content component.
     *
     * @return the content component. May be {@code null}
     */
    protected ComponentState getContent() {
        return content;
    }
}
