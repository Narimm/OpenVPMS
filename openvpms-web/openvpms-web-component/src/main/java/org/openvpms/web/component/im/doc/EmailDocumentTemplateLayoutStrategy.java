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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;


/**
 * Layout strategy for <em>entity.documentTemplateEmail</em> entities.
 *
 * @author Tim Anderson
 */
public class EmailDocumentTemplateLayoutStrategy extends AbstractDocumentTemplateLayoutStrategy {

    /**
     * Constructs a {@link EmailDocumentTemplateLayoutStrategy}.
     */
    public EmailDocumentTemplateLayoutStrategy() {
        super();
    }

    /**
     * Constructs a {@link EmailDocumentTemplateLayoutStrategy}.
     *
     * @param content the component representing the 'content' node
     */
    public EmailDocumentTemplateLayoutStrategy(ComponentState content) {
        super(content);
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
        IMObjectBean bean = new IMObjectBean(object);
        if ("DOCUMENT".equals(bean.getString("contentType"))) {
            ComponentState content = getContent();
            if (content == null) {
                content = initContent((Entity) object);
            }
            addComponent(content);
            // NOTE: this replaces the default "content" node. The pseudo node for the
            // document content must therefore have the same name as the "content" node
        }
        return super.apply(object, properties, parent, context);
    }

}
