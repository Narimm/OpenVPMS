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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.echo.colour;

import nextapp.echo2.app.Color;


/**
 * An extension to the Echo ColorSelect class that:
 * <ul>
 * <li>allows the colour code to be edited</li>
 * <li>works around a bug in the ColorSelect javascript implementation.<br/>
 * This exists to enable {@link ColorSelectPeer} to be used to specify a corrected javascript file,
 * <em>org/openvpms/web/resource/js/ColorSelect.js</em>.<br/>
 * The binding is specified in <em>META-INF/nextapp/echo2/SynchronizePeerBindings.properties</em>
 * </li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class ColorSelect extends nextapp.echo2.extras.app.ColorSelect {

    /**
     * Creates a new <tt>ColorSelect</tt> with an initially selected color of <tt>Color.WHITE</tt>.
     */
    public ColorSelect() {
        super();
    }

    /**
     * Creates a new <tt>ColorSelect</tt> with the specified color initially selected.
     *
     * @param color the initially selected color
     */
    public ColorSelect(Color color) {
        super(color);
    }
}
