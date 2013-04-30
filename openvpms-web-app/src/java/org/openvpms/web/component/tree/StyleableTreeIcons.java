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
package org.openvpms.web.component.tree;

import echopointng.tree.DefaultTreeIcons;
import echopointng.tree.TreeIcons;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.MutableStyle;
import nextapp.echo2.app.Style;


/**
 * <code>TreeIcons</code> implementation that can be used in a stylesheet.
 * Where no icon is styled, the corresponding icon from
 * <code>DefaultTreeIcons</code> will be used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StyleableTreeIcons implements TreeIcons {

    /**
     * The local style.
     */
    private MutableStyle _localStyle;

    /**
     * The style name.
     */
    private String _styleName;

    /**
     * Default icons.
     */
    private static final TreeIcons DEFAULT_ICONS = new DefaultTreeIcons();


    /**
     * Sets the style name.
     *
     * @param name the style name
     */
    public void setStyleName(String name) {
        _styleName = name;
    }

    /**
     * Returns the style name.
     *
     * @return the style name, or <code>null</code> if none is set
     */
    public String getStyleName() {
        return _styleName;
    }

    /**
     * Returns an icon with the given name.
     *
     * @param name the icon name
     * @return the icon or <code>null</code> if none exists
     */
    public ImageReference getIcon(String name) {
        ImageReference icon = null;
        if (_localStyle != null) {
            icon = (ImageReference) _localStyle.getProperty(name);
        }
        if (icon == null && _styleName != null) {
            ApplicationInstance app = ApplicationInstance.getActive();
            if (app != null) {
                Style style = app.getStyle(StyleableTreeIcons.class, _styleName);
                if (style != null) {
                    icon = (ImageReference) style.getProperty(name);
                }
            }
        }
        if (icon == null) {
            icon = DEFAULT_ICONS.getIcon(name);
        }
        return icon;
    }

    /**
     * Adds an icon.
     *
     * @param name the icon name
     * @param icon the icon
     */
    public void setIcon(String name, ImageReference icon) {
        if (_localStyle == null) {
            _localStyle = new MutableStyle();
        }
        _localStyle.setProperty(name, icon);
    }

}
