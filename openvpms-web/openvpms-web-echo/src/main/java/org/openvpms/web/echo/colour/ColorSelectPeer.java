/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.echo.colour;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.update.ServerComponentUpdate;
import nextapp.echo2.app.util.DomUtil;
import nextapp.echo2.extras.app.ColorSelect;
import nextapp.echo2.extras.webcontainer.ExtrasUtil;
import nextapp.echo2.webcontainer.ComponentSynchronizePeer;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PartialUpdateManager;
import nextapp.echo2.webcontainer.PartialUpdateParticipant;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import nextapp.echo2.webcontainer.RenderContext;
import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
import nextapp.echo2.webrender.ServerMessage;
import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.ServiceRegistry;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.servermessage.DomUpdate;
import nextapp.echo2.webrender.service.JavaScriptService;
import nextapp.echo2.webrender.service.StaticBinaryService;
import org.w3c.dom.Element;


/**
 * Replacement of the <tt>nextapp.echo2.extras.webcontainer.ColorSelectPeer</tt> class.
 * <p/>
 * This is pretty much a direct copy that replaces the echo2 ColorSelect.js implementation with a version
 * that corrects the bug:
 * <a href="bugs.nextapp.com/mantis/view.php?id=515">ColorSelect.setColor(Color) fails on subsequent updates</a>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ColorSelectPeer implements ComponentSynchronizePeer, PropertyUpdateProcessor {

    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service COLOR_SELECT_SERVICE = JavaScriptService.forResource("Echo2Extras.Fixed.ColorSelect",
                                                                                     "/org/openvpms/web/resource/js/ColorSelect.js");

    private static final Service ARROW_DOWN_IMAGE_SERVICE = StaticBinaryService.forResource(
        "Echo2Extras.Fixed.ColorSelect.ArrowDown", "image/gif",
        ExtrasUtil.IMAGE_RESOURCE_PATH + "ColorSelectArrowDown.gif");

    private static final Service ARROW_LEFT_IMAGE_SERVICE = StaticBinaryService.forResource(
        "Echo2Extras.Fixed.ColorSelect.ArrowLeft", "image/gif",
        ExtrasUtil.IMAGE_RESOURCE_PATH + "ColorSelectArrowLeft.gif");

    private static final Service ARROW_RIGHT_IMAGE_SERVICE = StaticBinaryService.forResource(
        "Echo2Extras.Fixed.ColorSelect.ArrowRight", "image/gif",
        ExtrasUtil.IMAGE_RESOURCE_PATH + "ColorSelectArrowRight.gif");

    private static final Service ARROW_UP_IMAGE_SERVICE = StaticBinaryService.forResource(
        "Echo2Extras.Fixed.ColorSelect.ArrowUp", "image/gif",
        ExtrasUtil.IMAGE_RESOURCE_PATH + "ColorSelectArrowUp.gif");

    private static final Service H_GRADIENT_IMAGE_SERVICE = StaticBinaryService.forResource(
        "Echo2Extras.Fixed.ColorSelect.HGradient", "image/png",
        ExtrasUtil.IMAGE_RESOURCE_PATH + "ColorSelectHGradient.png");

    private static final Service SV_GRADIENT_IMAGE_SERVICE = StaticBinaryService.forResource(
        "Echo2Extras.Fixed.ColorSelect.SVGradient", "image/png",
        ExtrasUtil.IMAGE_RESOURCE_PATH + "ColorSelectSVGradient.png");

    static {
        ServiceRegistry services = WebRenderServlet.getServiceRegistry();
        services.add(COLOR_SELECT_SERVICE);
        services.add(ARROW_DOWN_IMAGE_SERVICE);
        services.add(ARROW_LEFT_IMAGE_SERVICE);
        services.add(ARROW_RIGHT_IMAGE_SERVICE);
        services.add(ARROW_UP_IMAGE_SERVICE);
        services.add(H_GRADIENT_IMAGE_SERVICE);
        services.add(SV_GRADIENT_IMAGE_SERVICE);
    }

    /**
     * The <code>PartialUpdateManager</code> for this synchronization peer.
     */
    private PartialUpdateManager partialUpdateManager;

    /**
     * <code>PartialUpdateParticipant</code> to set color.
     */
    private PartialUpdateParticipant setColorUpdateParticipant = new PartialUpdateParticipant() {

        /**
         * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#renderProperty(nextapp.echo2.webcontainer
         * .RenderContext,
         *       nextapp.echo2.app.update.ServerComponentUpdate)
         */
        public void renderProperty(RenderContext rc, ServerComponentUpdate update) {
            renderSetColorDirective(rc, (ColorSelect) update.getParent());
        }

        /**
         * @see nextapp.echo2.webcontainer.PartialUpdateParticipant#canRenderProperty(nextapp.echo2.webcontainer
         * .RenderContext,
         *      nextapp.echo2.app.update.ServerComponentUpdate)
         */
        public boolean canRenderProperty(RenderContext rc, ServerComponentUpdate update) {
            return true;
        }
    };

    /**
     * Default constructor.
     */
    public ColorSelectPeer() {
        partialUpdateManager = new PartialUpdateManager();
        partialUpdateManager.add(ColorSelect.COLOR_CHANGED_PROPERTY, setColorUpdateParticipant);
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#getContainerId(nextapp.echo2.app.Component)
     */
    public String getContainerId(Component component) {
        throw new UnsupportedOperationException("Component does not support children.");
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderAdd(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String,
     *      nextapp.echo2.app.Component)
     */
    public void renderAdd(RenderContext rc, ServerComponentUpdate update, String targetId, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(COLOR_SELECT_SERVICE.getId());
        serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
        renderInitDirective(rc, targetId, (ColorSelect) component);
        renderSetColorDirective(rc, (ColorSelect) component);
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderDispose(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate,
     *      nextapp.echo2.app.Component)
     */
    public void renderDispose(RenderContext rc, ServerComponentUpdate update, Component component) {
        ServerMessage serverMessage = rc.getServerMessage();
        serverMessage.addLibrary(COLOR_SELECT_SERVICE.getId());
        serverMessage.addLibrary(ExtrasUtil.JS_EXTRAS_UTIL_SERVICE.getId());
        renderDisposeDirective(rc, (ColorSelect) component);
    }

    /**
     * Renders a dispose directive.
     *
     * @param rc          the relevant <code>RenderContext</code>
     * @param colorSelect the <code>ColorSelect</code> being rendered
     */
    private void renderDisposeDirective(RenderContext rc, ColorSelect colorSelect) {
        String elementId = ContainerInstance.getElementId(colorSelect);
        ServerMessage serverMessage = rc.getServerMessage();
        Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_PREREMOVE,
                                                                "ExtrasColorSelect.MessageProcessor", "dispose");
        initElement.setAttribute("eid", elementId);
    }

    /**
     * Renders an initialization directive.
     *
     * @param rc          the relevant <code>RenderContext</code>
     * @param containerId the container element id
     * @param colorSelect the <code>ColorSelect</code> being rendered
     */
    private void renderInitDirective(RenderContext rc, String containerId, ColorSelect colorSelect) {
        String elementId = ContainerInstance.getElementId(colorSelect);
        ServerMessage serverMessage = rc.getServerMessage();
        Element initElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_UPDATE,
                                                                "ExtrasColorSelect.MessageProcessor", "init");
        initElement.setAttribute("eid", elementId);
        initElement.setAttribute("container-eid", containerId);
        if (!colorSelect.isRenderEnabled()) {
            initElement.setAttribute("enabled", "false");
        }
        Boolean displayValue = (Boolean) colorSelect.getRenderProperty(ColorSelect.PROPERTY_DISPLAY_VALUE);
        if (displayValue != null && !displayValue.booleanValue()) {
            initElement.setAttribute("display-value", "false");
        }
        Extent hueWidth = (Extent) colorSelect.getRenderProperty(ColorSelect.PROPERTY_HUE_WIDTH);
        if (hueWidth != null) {
            initElement.setAttribute("hue-width", ExtentRender.renderCssAttributeValue(hueWidth));
        }
        Extent saturationHeight = (Extent) colorSelect.getRenderProperty(ColorSelect.PROPERTY_SATURATION_HEIGHT);
        if (saturationHeight != null) {
            initElement.setAttribute("saturation-height", ExtentRender.renderCssAttributeValue(saturationHeight));
        }
        Extent valueWidth = (Extent) colorSelect.getRenderProperty(ColorSelect.PROPERTY_VALUE_WIDTH);
        if (valueWidth != null) {
            initElement.setAttribute("value-width", ExtentRender.renderCssAttributeValue(valueWidth));
        }
    }

    /**
     * Renders a set-color directive.
     *
     * @param rc          the relevant <code>RenderContext</code>
     * @param colorSelect the <code>ColorSelect</code> being rendered
     */
    private void renderSetColorDirective(RenderContext rc, ColorSelect colorSelect) {
        String elementId = ContainerInstance.getElementId(colorSelect);
        ServerMessage serverMessage = rc.getServerMessage();
        Element setColorElement = serverMessage.appendPartDirective(ServerMessage.GROUP_ID_UPDATE,
                                                                    "ExtrasColorSelect.MessageProcessor", "set-color");
        setColorElement.setAttribute("eid", elementId);

        Color color = colorSelect.getColor();
        if (color != null) {
            setColorElement.setAttribute("r", Integer.toString(color.getRed()));
            setColorElement.setAttribute("g", Integer.toString(color.getGreen()));
            setColorElement.setAttribute("b", Integer.toString(color.getBlue()));
        }
    }

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(nextapp.echo2.webcontainer
     * .ContainerInstance,
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element element) {
        Element selectionElement = DomUtil.getChildElementByTagName(element, "color");
        int r = Integer.parseInt(selectionElement.getAttribute("r"));
        int g = Integer.parseInt(selectionElement.getAttribute("g"));
        int b = Integer.parseInt(selectionElement.getAttribute("b"));
        ci.getUpdateManager().getClientUpdateManager().setComponentProperty(component,
                                                                            ColorSelect.COLOR_CHANGED_PROPERTY,
                                                                            new Color(r, g, b));
    }

    /**
     * @see nextapp.echo2.webcontainer.ComponentSynchronizePeer#renderUpdate(nextapp.echo2.webcontainer.RenderContext,
     *      nextapp.echo2.app.update.ServerComponentUpdate, java.lang.String)
     */
    public boolean renderUpdate(RenderContext rc, ServerComponentUpdate update, String targetId) {
        // Determine if fully replacing the component is required.
        if (partialUpdateManager.canProcess(rc, update)) {
            partialUpdateManager.process(rc, update);
        } else {
            // Perform full update.
            DomUpdate.renderElementRemove(rc.getServerMessage(), ContainerInstance.getElementId(update.getParent()));
            renderAdd(rc, update, targetId, update.getParent());
        }

        return true;
    }
}