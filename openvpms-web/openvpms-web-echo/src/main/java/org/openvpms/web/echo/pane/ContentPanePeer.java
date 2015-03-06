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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.pane;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.update.ClientUpdateManager;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.PropertyUpdateProcessor;
import org.openvpms.web.echo.util.ExtentHelper;
import org.w3c.dom.Element;

/**
 * A peer for ContentPane that handles floating point values for horizontalScroll and verticalScroll
 * (i.e. scrollLeft and scrollTop) submitted by Google Chrome.
 *
 * @author Tim Anderson
 */
public class ContentPanePeer extends nextapp.echo2.webcontainer.syncpeer.ContentPanePeer {

    /**
     * @see nextapp.echo2.webcontainer.PropertyUpdateProcessor#processPropertyUpdate(
     *nextapp.echo2.webcontainer.ContainerInstance,
     *      nextapp.echo2.app.Component, org.w3c.dom.Element)
     */
    public void processPropertyUpdate(ContainerInstance ci, Component component, Element propertyElement) {
        String attribute = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_NAME);
        String value = propertyElement.getAttribute(PropertyUpdateProcessor.PROPERTY_VALUE);
        ClientUpdateManager manager = ci.getUpdateManager().getClientUpdateManager();
        if ("horizontalScroll".equals(attribute)) {
            Extent newValue = ExtentHelper.toExtent(value);
            manager.setComponentProperty(component, ContentPane.PROPERTY_HORIZONTAL_SCROLL, newValue);
        } else if ("verticalScroll".equals(attribute)) {
            Extent newValue = ExtentHelper.toExtent(value);
            manager.setComponentProperty(component, ContentPane.PROPERTY_VERTICAL_SCROLL, newValue);
        }
    }

}
