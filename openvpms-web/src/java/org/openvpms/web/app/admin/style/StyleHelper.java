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
package org.openvpms.web.app.admin.style;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TextField;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.style.StyleSheets;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;

import java.awt.Dimension;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;


/**
 * Style workspace helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StyleHelper {

    /**
     * Indicates that the properties apply to all screen resolutions.
     */
    public static final Dimension ANY_RESOLUTION = new Dimension(-1, -1);

    /**
     * The default screen resolution.
     */
    public static final Dimension DEFAULT_RESOLUTION = new Dimension(1024, 768);

    /**
     * Returns properties for the specified resolution.
     *
     * @param stylesheets the style sheets
     * @param size        the resolution to return properties for. If {@link #ANY_RESOLUTION}, returns the default
     *                    properties
     * @param evaluate    if <tt>true</tt> evaluate properties
     * @return the properties for the resolution, ordered on name
     */
    public static Map<String, String> getProperties(StyleSheets stylesheets, Dimension size, boolean evaluate) {
        Map<String, String> properties = Collections.emptyMap();
        if (size != null) {
            if (evaluate || !ANY_RESOLUTION.equals(size)) {
                properties = stylesheets.getProperties(size.width, size.height, evaluate);
            } else {
                properties = stylesheets.getDefaultProperties();
            }
            properties = new TreeMap<String, String>(properties);
        }
        return properties;
    }

    /**
     * Helper to add a label and text field for a property to a grid.
     *
     * @param grid     the grid
     * @param property the property to add
     */
    public static void addProperty(Grid grid, Property property) {
        Label name = LabelFactory.create();
        name.setText(property.getDisplayName());
        int length = property.getMaxLength() != -1 ? property.getMaxLength() : 40;
        TextField field = TextComponentFactory.create(property, length);
        grid.add(name);
        grid.add(field);
    }


}
