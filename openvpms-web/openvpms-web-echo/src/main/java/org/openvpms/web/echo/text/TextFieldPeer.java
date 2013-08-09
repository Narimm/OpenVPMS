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

package org.openvpms.web.echo.text;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static nextapp.echo2.app.text.TextComponent.PROPERTY_MAXIMUM_LENGTH;
import static nextapp.echo2.app.text.TextComponent.PROPERTY_TOOL_TIP_TEXT;
import static org.openvpms.web.echo.text.TextField.PROPERTY_CURSOR_POSITION;


/**
 * A peer to register a fixed TextComponent.js as a workaround for the echo2 bug referred to in
 * http://jira.openvpms.org/jira/browse/OVPMS-1017.
 * <p/>
 * This simply replaces the broken TextComponent.js script with a corrected one. It will therefore affect TextAreaPeer,
 * which uses the same script.
 *
 * @author Tim Anderson
 */
public class TextFieldPeer extends TextComponentPeer {

    /**
     * @see DomUpdateSupport#renderHtml(RenderContext,
     *      ServerComponentUpdate, Node, Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate addUpdate, Node parentNode, Component component) {
        TextField textField = (TextField) component;
        String elementId = ContainerInstance.getElementId(textField);

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(TEXT_COMPONENT_SERVICE.getId());

        Element inputElement = parentNode.getOwnerDocument().createElement("input");
        inputElement.setAttribute("id", elementId);
        if (textField instanceof PasswordField) {
            inputElement.setAttribute("type", "password");
        } else {
            inputElement.setAttribute("type", "text");
        }
        String value = textField.getText();
        if (value != null) {
            inputElement.setAttribute("value", value);
        }

        if (textField.isFocusTraversalParticipant()) {
            inputElement.setAttribute("tabindex", Integer.toString(textField.getFocusTraversalIndex()));
        } else {
            inputElement.setAttribute("tabindex", "-1");
        }

        String toolTipText = (String) textField.getRenderProperty(PROPERTY_TOOL_TIP_TEXT);
        if (toolTipText != null) {
            inputElement.setAttribute("title", toolTipText);
        }

        Integer maximumLength = (Integer) textField.getRenderProperty(PROPERTY_MAXIMUM_LENGTH);
        if (maximumLength != null) {
            inputElement.setAttribute("maxlength", maximumLength.toString());
        }

        Integer cursorPos = (Integer) textField.getRenderProperty(PROPERTY_CURSOR_POSITION);
        if (cursorPos != null) {
            inputElement.setAttribute("cursor-position", cursorPos.toString());
        }

        CssStyle cssStyle = createBaseCssStyle(rc, textField);
        if (cssStyle.hasAttributes()) {
            inputElement.setAttribute("style", cssStyle.renderInline());
        }

        parentNode.appendChild(inputElement);

        renderInitDirective(rc, textField);
    }

}
