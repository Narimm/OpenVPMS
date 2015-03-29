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

/**
 * An extension to the EPNG {@code LightBox} that supports setting the z-index of the light-box.
 * <p/>
 * This is to overcome a limitation of the original that requires the dialog to be modal on the client in order
 * to determine the z-index. However, when a modal dialog is processed by ClientEngine.js, the code to update
 * EchoModalManager.modalElementId is done after the light box is processed, which is too late.
 *
 * @author Tim Anderson
 */
public class LightBox extends echopointng.LightBox {

    /**
     * The z-index property name.
     */
    public static final String PROPERTY_Z_INDEX = "zIndex";

    /**
     * Sets the z-index of the light box.
     *
     * @param zIndex the z-index
     */
    public void setZIndex(int zIndex) {
        setProperty(PROPERTY_Z_INDEX, zIndex);
    }

    /**
     * Returns the z-index of the light box.
     *
     * @return the z-index of the light box, or {@code -1} if none has been set
     */
    public int getZIndex() {
        return getProperty(PROPERTY_Z_INDEX, -1);
    }
}
