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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
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
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.list.StyledListCell;
import org.openvpms.web.resource.util.Styles;


/**
 * List cell renderer that renders any 'All' and 'None' items in bold.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see AllNoneListModel
 */
public abstract class AllNoneListCellRenderer<T>
        extends AbstractListCellRenderer<T> {

    /**
     * Constructs a new <tt>AbstractListCellRenderer</tt>.
     *
     * @param type the type that this can render
     */
    public AllNoneListCellRenderer(Class<T> type) {
        super(type);
    }

    /**
     * Determines if an object represents 'All'.
     *
     * @param list   the list component
     * @param object the object. May be <tt>null</tt>
     * @param index  the object index
     * @return <code>true</code> if the object represents 'All'.
     */
    protected boolean isAll(Component list, T object, int index) {
        AbstractListComponent component = ((AbstractListComponent) list);
        ListModel model = component.getModel();
        if (model instanceof AllNoneListModel) {
            return ((AllNoneListModel) model).isAll(index);
        }
        return false;
    }

    /**
     * Determines if an object represents 'None'.
     *
     * @param list   the list component
     * @param object the object. May be <tt>null</tt>
     * @param index  the object index
     * @return <code>true</code> if the object represents 'None'.
     */
    protected boolean isNone(Component list, T object, int index) {
        AbstractListComponent component = ((AbstractListComponent) list);
        ListModel model = component.getModel();
        if (model instanceof AllNoneListModel) {
            return ((AllNoneListModel) model).isNone(index);
        }
        return false;
    }

    /**
     * Helper to render a cell in a bold font. Uses the "bold" label style.
     */
    protected static class BoldListCell implements StyledListCell {

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
