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

package org.openvpms.web.echo.lightbox;

import echopointng.ComponentEx;
import echopointng.EPNG;
import echopointng.ui.util.ImageManager;
import echopointng.ui.util.RenderingContext;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PartialUpdateParticipant;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.service.JavaScriptService;
import org.w3c.dom.Element;

/**
 * Replacement for the EPNG {@code LightBoxPeer}.
 *
 * @author Tim Anderson
 */
public class LightBoxPeer extends echopointng.ui.syncpeer.LightBoxPeer {

    /**
     * Service to provide supporting JavaScript library.
     */
    private static final Service LIGHT_BOX_SERVICE = JavaScriptService.forResource(
            "EPNG.LightBox", "/org/openvpms/web/echo/js/lightbox.js");

    static {
        WebRenderServlet.getServiceRegistry().remove(LB_SERVICE);
        WebRenderServlet.getServiceRegistry().add(LIGHT_BOX_SERVICE);
    }

    /**
     * Constructs a {@link LightBoxPeer}.
     */
    public LightBoxPeer() {
        partialUpdateManager.add(LightBox.PROPERTY_Z_INDEX, new PartialUpdateParticipant() {
            @Override
            public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
                return true;
            }

            @Override
            public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
                renderZIndexDirective(rc, update.getParent());
            }
        });
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String,
     *      nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(LB_SERVICE.getId());
        renderInitDirective(new RenderingContext(rc, update, component), targetId, component);
    }

    /**
     * Renders an initialization directive.
     */
    private void renderInitDirective(RenderingContext rc, String containerId, Component component) {
        Style fallbackStyle = EPNG.getFallBackStyle(component);

        String elementId = ContainerInstance.getElementId(component);
        ServerMessage serverMessage = rc.getServerMessage();
        Element itemizedUpdateElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_UPDATE, "EPLightBox.MessageProcessor", "init");

        Element itemElement = rc.getServerMessage().getDocument().createElement("item");
        itemizedUpdateElement.appendChild(itemElement);

        itemElement.setAttribute("eid", elementId);
        itemElement.setAttribute("container-eid", containerId);
        itemElement.setAttribute("hidden", String.valueOf(rc.getRP(LightBox.PROPERTY_HIDDEN, fallbackStyle, false)));
        itemElement.setAttribute("enabled", String.valueOf(component.isRenderEnabled()));
        itemElement.setAttribute("zIndex", String.valueOf(rc.getRP(LightBox.PROPERTY_Z_INDEX, -1)));

        ImageReference translucentImage = (ImageReference) rc.getRP(LightBox.PROPERTY_TRANSLUCENT_IMAGE, fallbackStyle);
        if (translucentImage != null) {
            itemElement.setAttribute("translucentImage", ImageManager.getURI(rc, translucentImage));
        }
    }

    private void renderZIndexDirective(RenderContext rc, Component component) {
        Element itemizedUpdateElement = rc.getServerMessage().getItemizedDirective(
                ServerMessage.GROUP_ID_POSTUPDATE, "EPLightBox.MessageProcessor", "zIndex", new String[0],
                new String[0]);
        Element itemElement = rc.getServerMessage().getDocument().createElement("item");
        itemizedUpdateElement.appendChild(itemElement);

        itemElement.setAttribute("eid", ContainerInstance.getElementId(component));
        int zIndex = ComponentEx.getRenderProperty(component, LightBox.PROPERTY_Z_INDEX, -1);
        itemElement.setAttribute("zIndex", String.valueOf(zIndex));
    }

}
