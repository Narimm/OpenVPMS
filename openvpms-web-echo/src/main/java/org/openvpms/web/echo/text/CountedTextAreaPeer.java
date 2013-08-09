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
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.DomUpdateSupport;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.output.CssStyle;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.openvpms.web.echo.text.TextComponent.PROPERTY_CURSOR_POSITION;


/**
 * Peer for {@link CountedTextArea}.
 *
 * @author Tim Anderson
 */
public class CountedTextAreaPeer extends TextAreaPeer {

    /**
     * Service to provide supporting JavaScript library.
     */
    static final Service SERVICE = JavaScriptService.forResource(
            "CountedTextArea", "/org/openvpms/web/resource/js/CountedTextArea.js");

    static {
        WebRenderServlet.getServiceRegistry().add(SERVICE);
    }

    /**
     * @see DomUpdateSupport#renderHtml(RenderContext,
     *      ServerComponentUpdate, Node, Component)
     */
    @Override
    public void renderHtml(RenderContext rc, ServerComponentUpdate addUpdate, Node parentNode, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(SERVICE.getId());
        serverMessage.addLibrary(TEXT_COMPONENT_SERVICE.getId());
        TextArea textArea = (TextArea) component;
        String elementId = ContainerInstance.getElementId(component);

        Element container = parentNode.getOwnerDocument().createElement("div");
        container.setAttribute("id", elementId + "_container");

        Element textAreaElement = parentNode.getOwnerDocument().createElement("textarea");
        textAreaElement.setAttribute("id", elementId);

        if (textArea.isFocusTraversalParticipant()) {
            textAreaElement.setAttribute("tabindex", Integer.toString(textArea.getFocusTraversalIndex()));
        } else {
            textAreaElement.setAttribute("tabindex", "-1");
        }

        String toolTipText = (String) textArea.getRenderProperty(TextArea.PROPERTY_TOOL_TIP_TEXT);
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

        parentNode.appendChild(container);
        container.appendChild(textAreaElement);

        renderInitDirective(rc, textArea);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to
     * initialize the state of a text component, performing tasks such as
     * registering event listeners on the client.
     *
     * @param rc            the relevant <code>RenderContext</code>
     * @param textComponent the <code>TextComponent<code>
     */
    public void renderInitDirective(RenderContext rc, TextComponent textComponent) {
        Extent horizontalScroll = (Extent) textComponent.getRenderProperty(TextComponent.PROPERTY_HORIZONTAL_SCROLL);
        Extent verticalScroll = (Extent) textComponent.getRenderProperty(TextComponent.PROPERTY_VERTICAL_SCROLL);
        String elementId = ContainerInstance.getElementId(textComponent);
        ServerMessage serverMessage = rc.getServerMessage();

        Element itemizedUpdateElement = serverMessage.getItemizedDirective(
                ServerMessage.GROUP_ID_POSTUPDATE, "CountedTextArea.MessageProcessor", "init", new String[0],
                new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        if (horizontalScroll != null && horizontalScroll.getValue() != 0) {
            itemElement.setAttribute("horizontal-scroll",
                                     ExtentRender.renderCssAttributePixelValue(horizontalScroll, "0"));
        }
        if (verticalScroll != null && verticalScroll.getValue() != 0) {
            itemElement.setAttribute("vertical-scroll", ExtentRender.renderCssAttributePixelValue(verticalScroll, "0"));
        }
        Integer maximumLength = (Integer) textComponent.getProperty(TextComponent.PROPERTY_MAXIMUM_LENGTH);
        if (maximumLength != null) {
            itemElement.setAttribute("maximum-length", maximumLength.toString());
        }
        if (rc.getContainerInstance().getClientProperties().getBoolean(ClientProperties.QUIRK_TEXTAREA_CONTENT)) {
            String value = textComponent.getText();
            if (value != null) {
                itemElement.setAttribute("text", value);
            }
        }
        Integer cursorPos = (Integer) textComponent.getRenderProperty(PROPERTY_CURSOR_POSITION);
        if (cursorPos != null) {
            itemElement.setAttribute("cursor-position", cursorPos.toString());
        }

        if (!textComponent.isRenderEnabled()) {
            itemElement.setAttribute("enabled", "false");
        }
        if (textComponent.hasActionListeners()) {
            itemElement.setAttribute("server-notify", "true");
        }
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate,
     *      nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        rc.getServerMessage().addLibrary(SERVICE.getId());
        renderDisposeDirective(rc, (TextComponent) component);
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to
     * dispose the state of a text component, performing tasks such as
     * registering event listeners on the client.
     *
     * @param rc            the relevant <code>RenderContext</code>
     * @param textComponent the <code>TextComponent<code>
     */
    public void renderDisposeDirective(RenderContext rc, TextComponent textComponent) {
        String elementId = ContainerInstance.getElementId(textComponent);
        ServerMessage serverMessage = rc.getServerMessage();
        Element itemizedUpdateElement = serverMessage.getItemizedDirective(ServerMessage.GROUP_ID_PREREMOVE,
                                                                           "CountedTextArea.MessageProcessor",
                                                                           "dispose", new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        itemizedUpdateElement.appendChild(itemElement);
    }

    /**
     * @see ComponentSynchronizePeer#renderUpdate(RenderContext,
     *      ServerComponentUpdate, String)
     */
    @Override
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        String elementId = ContainerInstance.getElementId(update.getParent());
        String containerId = elementId + "_container";
        DomUpdate.renderElementRemove(rc.getServerMessage(), containerId);
        renderAdd(rc, update, targetId, update.getParent());
        return false;
    }
}
