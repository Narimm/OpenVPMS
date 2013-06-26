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

package org.openvpms.web.workspace.workflow.messaging;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;

/**
 * Message layout strategy.
 *
 * @author Tim Anderson
 */
public class AbstractMessageLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Default extent - 100%.
     */
    protected static final Extent FULL_WIDTH = new Extent(100, Extent.PERCENT);

    /**
     * Creates a component to display the act date.
     *
     * @param act the act
     * @return a component to display the date
     */
    protected ComponentState createDate(Act act) {
        Label label = LabelFactory.create();
        label.setText(MessageTableModel.formatStartTime(act));
        ComponentState date = new ComponentState(label);
        GridLayoutData layout = new GridLayoutData();
        layout.setAlignment(Alignment.ALIGN_RIGHT);
        label.setLayoutData(layout);
        return date;
    }

    /**
     * Creates a component to display the message.
     *
     * @param properties the properties
     * @param context    the layout context
     * @param styleName  the message style name
     * @return a component to display the message
     */
    protected ComponentState createMessage(PropertySet properties, LayoutContext context, String styleName) {
        Property message = properties.get("message");
        TextComponent textArea = new BoundTextArea(message);
        if (message.getMaxLength() != -1) {
            textArea.setMaximumLength(message.getMaxLength());
        }
        if (!context.isEdit()) {
            textArea.setEnabled(false);
        }
        textArea.setStyleName(styleName);
        return new ComponentState(textArea, message);
    }


}
