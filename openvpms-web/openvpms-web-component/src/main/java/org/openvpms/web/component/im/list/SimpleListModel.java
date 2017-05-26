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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.list;

import java.util.ArrayList;
import java.util.List;


/**
 * List model that optionally contains items for 'All' or 'None', backed by a list.
 *
 * @author Tim Anderson
 */
public class SimpleListModel<T> extends AllNoneListModel {

    /**
     * The objects.
     */
    private List<T> objects;

    /**
     * If {@code true}, add a localised "All".
     */
    private final boolean all;

    /**
     * If {@code true}, add a localised "None".
     */
    private final boolean none;

    /**
     * Constructs an empty {@link SimpleListModel}.
     */
    public SimpleListModel() {
        this(false, false);
    }

    /**
     * Constructs a {@link SimpleListModel}.
     *
     * @param all  if {@code true}, add a localised "All"
     * @param none if {@code true}, add a localised "None"
     */
    public SimpleListModel(boolean all, boolean none) {
        this(new ArrayList<T>(), all, none);
    }

    /**
     * Constructs a {@link SimpleListModel}.
     *
     * @param objects the objects to populate the list with.
     * @param all     if {@code true}, add a localised "All"
     * @param none    if {@code true}, add a localised "None"
     */
    public SimpleListModel(List<? extends T> objects, boolean all, boolean none) {
        this.all = all;
        this.none = none;
        setObjects(objects);
    }

    /**
     * Returns the object at the specified index.
     *
     * @param index the index
     * @return the object, or {@code null} if the index represents 'All' or
     * 'None'
     */
    public T getObject(int index) {
        return objects.get(index);
    }

    /**
     * Returns the index of the specified object.
     *
     * @param object the object
     * @return the index of {@code object}, or {@code -1} if it doesn't exist
     */
    public int indexOf(T object) {
        return objects.indexOf(object);
    }

    /**
     * Returns the objects in the list.
     * <p/>
     * Any index representing 'All' or 'None' will be {@code null}.
     *
     * @return the objects
     */
    public List<T> getObjects() {
        return objects;
    }

    /**
     * Sets the objects.
     *
     * @param objects the objects to populate the list with
     */
    public void setObjects(List<? extends T> objects) {
        initObjects(objects);
        fireContentsChanged(0, objects.size());
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    @Override
    public int size() {
        return objects.size();
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the value
     */
    @Override
    public Object get(int index) {
        return getObject(index);
    }

    /**
     * Initialises the objects.
     *
     * @param objects the objects to populate the list with.
     */
    protected void initObjects(List<? extends T> objects) {
        int index = 0;
        this.objects = new ArrayList<>();
        if (all) {
            this.objects.add(null);
            setAll(index++);
        }
        if (none) {
            this.objects.add(null);
            setNone(index);
        }

        this.objects.addAll(objects);
    }

}
