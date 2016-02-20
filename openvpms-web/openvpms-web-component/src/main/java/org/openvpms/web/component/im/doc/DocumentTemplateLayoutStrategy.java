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

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.sms.BoundCountedTextArea;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;


/**
 * Layout strategy for <em>entity.documentTemplate</em> entities.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateLayoutStrategy extends AbstractDocumentTemplateLayoutStrategy {

    /**
     * Constructs a {@link DocumentTemplateLayoutStrategy}.
     */
    public DocumentTemplateLayoutStrategy() {
        super();
    }

    /**
     * Constructs a {@link DocumentTemplateLayoutStrategy}.
     *
     * @param content    the component representing the 'content' node
     */
    public DocumentTemplateLayoutStrategy(ComponentState content) {
        super(content);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        Property sms = properties.get("sms");
        TextArea textArea;
        if (context.isEdit()) {
            textArea = new BoundCountedTextArea(sms, 40, 8);
            textArea.setStyleName(Styles.DEFAULT);

            Property copies = properties.get("copies");
            SpinBox spinBox = new SpinBox(copies, 1, 99);
            addComponent(new ComponentState(spinBox, copies, spinBox.getFocusGroup()));
        } else {
            textArea = BoundTextComponentFactory.createTextArea(sms, 40, 8);
            textArea.setEnabled(false);
        }
        addComponent(new ComponentState(RowFactory.create(textArea), sms));
        return super.apply(object, properties, parent, context);
    }

}
