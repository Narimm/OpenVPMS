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
package org.openvpms.web.component.im.list;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.list.StyledListCell;


/**
 * A <tt>StyledListCell</tt> that can be styled from a style sheet.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractStyledListCell implements StyledListCell {

    /**
     * The cell value.
     */
    private final String value;

    /**
     * The background.
     */
    private Color background;

    /**
     * The foreground.
     */
    private Color foreground;

    /**
     * The font.
     */
    private Font font;


    /**
     * Constructs an <tt>AbstractStyledListCell</tt>.
     *
     * @param value the cell value
     */
    public AbstractStyledListCell(String value) {
        this.value = value;
    }

    /**
     * Constructs an <tt>AbstractStlyedListCell</tt> with the specified style.
     *
     * @param value      the cell value
     * @param background the background colour. May be <tt>null</tt>
     * @param foreground the foreground colour. May be <tt>null</tt>
     * @param font       the font. May be <tt>null</tt>
     */
    public AbstractStyledListCell(String value, Color background, Color foreground, Font font) {
        this.value = value;
        this.background = background;
        this.foreground = foreground;
        this.font = font;
    }

    /**
     * Constructs an <tt>AbstractStlyedListCell</tt> that gets its style from a label style.
     *
     * @param value     the cell value
     * @param styleName the style name
     */
    public AbstractStyledListCell(String value, String styleName) {
        this.value = value;
        setStyle(styleName);
    }

    /**
     * Sets the background of the list item.
     *
     * @param background the background. May be <tt>null</tt>
     */
    public void setBackground(Color background) {
        this.background = background;
    }

    /**
     * Returns the background of the list item.
     *
     * @return the background. May be <tt>null</tt>
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Sets the foreground of the list item.
     *
     * @param foreground the foreground. Nay be <tt>null</tt>
     */
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    /**
     * Returns the foreground of the list item.
     *
     * @return the foreground
     */
    public Color getForeground() {
        return foreground;
    }

    /**
     * Sets the font of the list item.
     *
     * @param font the font. May be <tt>null</tt>
     */
    public void setFont(Font font) {
        this.font = font;
    }


    /**
     * Returns the font of the list item.
     *
     * @return the font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Returns the cell value.
     *
     * @return the cell value
     */
    public String toString() {
        return value;
    }

    /**
     * Sets the background, foreground, and font from the specified label style.
     *
     * @param styleName the label style name
     */
    protected void setStyle(String styleName) {
        Style style = ApplicationInstance.getActive().getStyle(Label.class, styleName);
        if (style != null) {
            background = (Color) style.getProperty(Component.PROPERTY_BACKGROUND);
            foreground = (Color) style.getProperty(Component.PROPERTY_FOREGROUND);
            font = (Font) style.getProperty(Component.PROPERTY_FONT);
        }
    }

}
