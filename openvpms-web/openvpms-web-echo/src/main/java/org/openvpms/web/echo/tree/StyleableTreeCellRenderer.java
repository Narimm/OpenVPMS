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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.echo.tree;

import echopointng.tree.DefaultTreeCellRenderer;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Style;


/**
 * <code>TreeCellRenderer</code> that can be used in a stylesheet.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StyleableTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Construct a new <code>StyleableTreeCellRenderer</code>.
     */
    public StyleableTreeCellRenderer() {
        // remove hard-coded values. This will be picked up from the stylesheet
        setFont(null);
        setSelectedBackground(null);
        setSelectedForeground(null);
        setSelectedFont(null);
    }

    /**
     * Sets the name of the name to use.
     * This sets the {@link #setFont}, {@link #setSelectedBackground},
     * {@link #setSelectedForeground} and {@link #setSelectedFont} with any
     * values specified in the stylesheet.
     *
     * @param name the new name name
     */
    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);
        ApplicationInstance app = ApplicationInstance.getActive();
        Style style = app.getStyle(StyleableTreeCellRenderer.class, name);
        if (style != null) {
            update(style, PROPERTY_FONT);
            update(style, PROPERTY_SELECTED_BACKGROUND);
            update(style, PROPERTY_SELECTED_FOREGROUND);
            update(style, PROPERTY_SELECTED_FONT);
        }
    }

    /**
     * Updates a property from the stylesheet, if one is specified.
     *
     * @param style    the style
     * @param property the property
     */
    private void update(Style style, String property) {
        Object value = style.getProperty(property);
        if (value != null) {
            setProperty(property, value);
        }
    }

}
