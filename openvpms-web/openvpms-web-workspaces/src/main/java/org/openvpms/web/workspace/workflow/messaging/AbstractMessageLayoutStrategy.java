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

import nextapp.echo2.app.Label;
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
     * Creates a label to display the act date.
     *
     * @param act the act
     * @return a component to display the date
     */
    protected Label createDate(Act act) {
        Label label = LabelFactory.create();
        label.setText(MessageTableModel.formatStartTime(act));
        return label;
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
