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
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.output.CssStyle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Replacement peer for the echo2 TextArea peer.
 * <p/>
 * This supports cursor positioning.
 *
 * @author Tim Anderson
 */
public class TextAreaPeer extends TextComponentPeer {

    /**
     * @see nextapp.echo2.webcontainer.DomUpdateSupport#renderHtml(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, org.w3c.dom.Node, nextapp.echo2.app.Component)
     */
    public void renderHtml(RenderContext rc, ServerComponentUpdate addUpdate, Node parentNode, Component component) {
        TextArea textArea = (TextArea) component;
        String elementId = ContainerInstance.getElementId(component);

        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(TEXT_COMPONENT_SERVICE.getId());

        Element textAreaElement = parentNode.getOwnerDocument().createElement("textarea");
        textAreaElement.setAttribute("id", elementId);

        if (textArea.isFocusTraversalParticipant()) {
            textAreaElement.setAttribute("tabindex", Integer.toString(textArea.getFocusTraversalIndex()));
        } else {
            textAreaElement.setAttribute("tabindex", "-1");
        }

        String toolTipText = (String) textArea.getRenderProperty(nextapp.echo2.app.TextArea.PROPERTY_TOOL_TIP_TEXT);
        if (toolTipText != null) {
            textAreaElement.setAttribute("title", toolTipText);
        }

        String value = textArea.getText();
        if (value != null) {
            if (!rc.getContainerInstance().getClientProperties().getBoolean(
                    ClientProperties.QUIRK_TEXTAREA_CONTENT)) {
                textAreaElement.appendChild(rc.getServerMessage().getDocument().createTextNode(value));
            }
        }

        CssStyle cssStyle = createBaseCssStyle(rc, textArea);
        if (cssStyle.hasAttributes()) {
            textAreaElement.setAttribute("style", cssStyle.renderInline());
        }

        parentNode.appendChild(textAreaElement);

        renderInitDirective(rc, textArea);
    }

}
