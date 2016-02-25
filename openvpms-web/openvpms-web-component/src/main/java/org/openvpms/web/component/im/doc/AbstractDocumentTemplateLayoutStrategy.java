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

import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
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
     *
     * @param object the template
     * @return a component representing the template document
     */
    protected ComponentState getContent(Entity object) {
        TemplateHelper helper = new TemplateHelper(ServiceHelper.getArchetypeService());
        DocumentAct act = helper.getDocumentAct(object);
        Label component = LabelFactory.create();
        if (act != null) {
            component.setText(act.getFileName());
        }
        return new ComponentState(component);
    }

    /**
     * Initialises the content component.
     *
     * @param object the template
     * @return the content component
     */
    protected ComponentState initContent(Entity object) {
        setContent(getContent(object));
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
