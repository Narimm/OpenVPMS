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

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.StyledListCell;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.resource.util.Styles;


/**
 * List cell renderer that display's an {@link IMObject}'s name.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectListCellRenderer implements ListCellRenderer {

    /**
     * Localised display name for "all".
     */
    private final String ALL = Messages.get("list.all");

    /**
     * Localised display name for "none".
     */
    private final String NONE = Messages.get("list.none");


    /**
     * Renders an item in a list.
     *
     * @param list  the list component
     * @param value the item value
     * @param index the item index
     * @return the rendered form of the list cell
     */
    public Object getListCellRendererComponent(Component list, Object value,
                                               int index) {
        Object result = null;
        if (value instanceof IMObject) {
            IMObject object = (IMObject) value;
            String name = object.getName();
            if (object.getArchetypeId() == null) {
                // dummy object.
                if (IMObjectListModel.ALL.equals(name)) {
                    result = new BoldListCell(ALL);
                } else if (IMObjectListModel.NONE.equals(name)) {
                    result = new BoldListCell(NONE);
                }
            } else {
                result = name;
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Helper to render a cell in a bold font. Uses the "bold" label style.
     */
    private static class BoldListCell implements StyledListCell {

        /**
         * The cell value.
         */
        private final String value;

        /**
         * The foreground.
         */
        private Color background;

        /**
         * The font.
         */
        private Font font;

        /**
         * The background.
         */
        private Color foreground;

        /**
         * Creates a new <code>BoldListCell</code>.
         *
         * @param value the cell value
         */
        public BoldListCell(String value) {
            this.value = value;
            String styleName = Styles.getStyle(Label.class, "bold");
            if (styleName != null) {
                Style style = ApplicationInstance.getActive().getStyle(
                        Label.class,
                        styleName);
                background = (Color) style.getProperty(
                        Component.PROPERTY_BACKGROUND);
                foreground = (Color) style.getProperty(
                        Component.PROPERTY_FOREGROUND);
                font = (Font) style.getProperty(Component.PROPERTY_FONT);
            }
        }

        /**
         * Returns the background of the list item.
         *
         * @return the background
         */
        public Color getBackground() {
            return background;
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
         * Returns the foreground of the list item.
         *
         * @return the foreground
         */
        public Color getForeground() {
            return foreground;
        }

        /**
         * Returns the cell value.
         *
         * @return the cell value
         */
        public String toString() {
            return value;
        }
    }
}

