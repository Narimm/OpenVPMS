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

package org.openvpms.web.echo.tabpane;

import nextapp.echo2.app.Component;

/**
 * A {@code TabPaneModel} that associates an object with each tab.
 *
 * @author Tim Anderson
 */
public class ObjectTabPaneModel<T> extends TabPaneModel {

    /**
     * Associates an object with a tab.
     */
    class ObjectTabEntry extends TabEntry {

        /**
         * The object.
         */
        private T object;

        /**
         * Constructs a {@link ObjectTabEntry}.
         *
         * @param tabComponent the tab
         * @param tabContent   the content
         */
        public ObjectTabEntry(Component tabComponent, Component tabContent) {
            super(tabComponent, tabContent);
        }

        /**
         * Sets the tab object.
         *
         * @param object the object. May be {@code null}
         */
        public void setObject(T object) {
            this.object = object;
        }

        /**
         * Returns the object.
         *
         * @return the object. May be {@code null}
         */
        public T getObject() {
            return object;
        }
    }

    /**
     * Constructs an {@link ObjectTabPaneModel} that enables shortcut support if a container is specified.
     *
     * @param container the container. May be {@code null}
     */
    public ObjectTabPaneModel(Component container) {
        super(container);
    }

    /**
     * Adds a new tab with the specified title.
     *
     * @param object  the object to associate with the tab
     * @param title   the title of the new tab
     * @param content the component to use as the tab content
     */
    public void addTab(T object, String title, Component content) {
        int index = size();
        addTab(title, content);
        setObject(object, index);
    }

    /**
     * Sets the object for the tab at the specified index.
     *
     * @param object the object. May be {@code null}
     * @param index  the tab index
     */
    @SuppressWarnings("unchecked")
    public void setObject(T object, int index) {
        ObjectTabEntry entry = (ObjectTabEntry) getTabEntryList().get(index);
        entry.setObject(object);
    }

    /**
     * Returns the object for the tab at the specified index.
     *
     * @param index the tab index
     * @return the corresponding property. May be {@code null}
     */
    @SuppressWarnings("unchecked")
    public T getObject(int index) {
        if (index >= 0 && index < getTabEntryList().size()) {
            ObjectTabEntry entry = (ObjectTabEntry) getTabEntryList().get(index);
            return entry.getObject();
        }
        return null;
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
        TabEntry entry = new ObjectTabEntry(tabComponent, tabContent);
        getTabEntryList().add(index, entry);
        fireStateChanged();
    }
}
