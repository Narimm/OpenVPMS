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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.tabpane.TabPaneModel;

/**
 * A {@code TabPaneModel} used for rendering {@code IMObjects}.Each tab represents a complex node.
 *
 * @author Tim Anderson
 */
public class IMObjectTabPaneModel extends TabPaneModel {

    /**
     * Associates a property (i.e a complex node) with a tab.
     */
    class PropertyTabEntry extends TabEntry {

        /**
         * The property.
         */
        private Property property;

        /**
         * Constructs a {@link PropertyTabEntry}.
         *
         * @param tabComponent the tab
         * @param tabContent   the content
         */
        public PropertyTabEntry(Component tabComponent, Component tabContent) {
            super(tabComponent, tabContent);
        }

        /**
         * Sets the property that the tab renders.
         *
         * @param property the property. May be {@code null}
         */
        public void setProperty(Property property) {
            this.property = property;
        }

        /**
         * Returns the property that the tab renders.
         *
         * @return the property
         */
        public Property getProperty() {
            return property;
        }
    }

    /**
     * Constructs an {@link IMObjectTabPaneModel} that enables shortcut support if a container is specified.
     *
     * @param container the container. May be {@code null}
     */
    public IMObjectTabPaneModel(Component container) {
        super(container);
    }

    /**
     * Adds a new tab with the specified title.
     *
     * @param property the property that the tab represents
     * @param title    the title of the new tab
     * @param content  the component to use as the tab content
     */
    public void addTab(Property property, String title, Component content) {
        int index = size();
        addTab(title, content);
        setProperty(property, index);
    }

    /**
     * Sets the property for the tab at the specified index.
     *
     * @param property the property. May be {@code null}
     * @param index    the tab index
     */
    public void setProperty(Property property, int index) {
        PropertyTabEntry entry = (PropertyTabEntry) getTabEntryList().get(index);
        entry.setProperty(property);

    }

    /**
     * Returns the property for the tab at the specified index.
     *
     * @param index the tab index
     * @return the corresponding property. May be {@code null}
     */
    public Property getProperty(int index) {
        PropertyTabEntry entry = (PropertyTabEntry) getTabEntryList().get(index);
        return entry.getProperty();
    }

    /**
     * Called to insert a new tab at the specified index. It will grow the
     * collection of tabs to fit the new tab.
     *
     * @param index        the index to insert at, growing the collection as required
     * @param tabComponent the component to use as the tab for the given {@code index}
     * @param tabContent   the content to use for the given {@code index}
     * @throws IndexOutOfBoundsException if the index is out of range (@code index < 0 || index > size()).
     */
    @Override
    @SuppressWarnings("unchecked")
    public void insertTab(int index, Component tabComponent, Component tabContent) {
        TabEntry entry = new PropertyTabEntry(tabComponent, tabContent);
        getTabEntryList().add(index, entry);
        fireStateChanged();
    }
}
