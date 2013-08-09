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
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webrender.ClientProperties;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.service.JavaScriptService;
import org.w3c.dom.Element;

import static org.openvpms.web.echo.text.TextComponent.PROPERTY_CURSOR_POSITION;

/**
 * A peer to register a fixed TextComponent.js as a workaround for the echo2 bug referred to in
 * http://jira.openvpms.org/jira/browse/OVPMS-1017.
 * <p/>
 * This simply replaces the broken TextComponent.js script with a corrected one. It will therefore affect TextAreaPeer,
 * which uses the same script.
 *
 * @author Tim Anderson
 */
public abstract class TextComponentPeer extends nextapp.echo2.webcontainer.syncpeer.TextComponentPeer {

    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service TEXT_COMPONENT_SERVICE = JavaScriptService.forResource(
            "Echo.TextComponent", "/org/openvpms/web/echo/js/TextComponent.js");

    static {
        // NOTE: as this extends TextFieldPeer, the broken TextComponent.js script will always be registered prior to
        // this due to static construction order. It can therefore safely be removed and replaced.
        WebRenderServlet.getServiceRegistry().remove(TEXT_COMPONENT_SERVICE);
        WebRenderServlet.getServiceRegistry().add(TEXT_COMPONENT_SERVICE);
    }

    /**
     * @see PropertyUpdateProcessor#processPropertyUpdate(
     *ContainerInstance,
     *      Component, Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        super.processPropertyUpdate(ci, component, propertyElement);
        String propertyName = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        if (PROPERTY_CURSOR_POSITION.equals(propertyName)) {
            int propertyValue = Integer.parseInt(propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE));
            ci.getUpdateManager().getClientUpdateManager().setComponentProperty(
                    component, PROPERTY_CURSOR_POSITION, propertyValue);
        }
    }

    /**
     * Renders a directive to the outgoing <code>ServerMessage</code> to
     * initialize the state of a text component, performing tasks such as
     * registering event listeners on the client.
     * <p/>
     * This is essentially a copy of the echo2 implementation, with support for PROPERTY_CURSOR_POSITION added.
     *
     * @param rc            the relevant <code>RenderContext</code>
     * @param textComponent the <code>TextComponent<code>
     */
    @Override
    public void renderInitDirective(RenderContext rc, nextapp.echo2.app.text.TextComponent textComponent) {
        TextComponent component = (TextComponent) textComponent;
        Extent horizontalScroll = (Extent) component.getRenderProperty(TextComponent.PROPERTY_HORIZONTAL_SCROLL);
        Extent verticalScroll = (Extent) component.getRenderProperty(TextComponent.PROPERTY_VERTICAL_SCROLL);
        String elementId = ContainerInstance.getElementId(textComponent);
        ServerMessage serverMessage = rc.getServerMessage();

        Element itemizedUpdateElement = serverMessage.getItemizedDirective(
                ServerMessage.GROUP_ID_POSTUPDATE, "EchoTextComponent.MessageProcessor", "init",
                new String[0], new String[0]);
        Element itemElement = serverMessage.getDocument().createElement("item");
        itemElement.setAttribute("eid", elementId);
        if (horizontalScroll != null && horizontalScroll.getValue() != 0) {
            itemElement.setAttribute("horizontal-scroll",
                                     ExtentRender.renderCssAttributePixelValue(horizontalScroll, "0"));
        }
        if (verticalScroll != null && verticalScroll.getValue() != 0) {
            itemElement.setAttribute("vertical-scroll", ExtentRender.renderCssAttributePixelValue(verticalScroll, "0"));
        }
        if (textComponent instanceof TextArea) {
            Integer maximumLength = (Integer) textComponent.getProperty(TextComponent.PROPERTY_MAXIMUM_LENGTH);
            if (maximumLength != null) {
                itemElement.setAttribute("maximum-length", maximumLength.toString());
            }
        }
        if (textComponent instanceof TextArea
            && rc.getContainerInstance().getClientProperties().getBoolean(ClientProperties.QUIRK_TEXTAREA_CONTENT)) {
            String value = textComponent.getText();
            if (value != null) {
                itemElement.setAttribute("text", value);
            }
        }
        Integer cursorPos = (Integer) component.getRenderProperty(PROPERTY_CURSOR_POSITION);
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
}
